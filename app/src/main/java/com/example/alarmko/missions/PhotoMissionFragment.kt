package com.example.alarmko.missions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.alarmko.R
import com.example.alarmko.data.model.PhotoCategory
import com.example.alarmko.data.model.PhotoObject
import com.example.alarmko.data.repository.AlarmRepository
import com.example.alarmko.exceptions.MissionPhotoException
import com.example.alarmko.exceptions.ErrorCode
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoMissionFragment : Fragment() {
    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var tvPhotoStatus: TextView
    private lateinit var tvDetectedObject: TextView
    private lateinit var tvTargetObject: TextView
    private lateinit var tvAttempts: TextView
    private lateinit var btnTakePhoto: MaterialButton
    private var imageCapture: ImageCapture? = null
    private var onMissionSuccess: (() -> Unit)? = null
    private var photoCategory: String? = null
    private var repository: AlarmRepository? = null
    private var attempts = 0
    private var currentObject: PhotoObject? = null
    private var availableObjects: List<PhotoObject> = listOf()

    fun setPhotoCategory(category: String?) {
        photoCategory = category
    }

    fun setRepository(repo: AlarmRepository) {
        repository = repo
    }

    fun setOnMissionSuccessListener(listener: () -> Unit) {
        onMissionSuccess = listener
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else tvPhotoStatus.text = getString(R.string.camera_permission_denied)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mission_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewViewPhoto)
        tvPhotoStatus = view.findViewById(R.id.tvPhotoStatus)
        tvDetectedObject = view.findViewById(R.id.tvDetectedObject)
        tvTargetObject = view.findViewById(R.id.tvTargetObject)
        tvAttempts = view.findViewById(R.id.tvAttempts)

        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
        btnTakePhoto.setOnClickListener { takePhoto() }

        loadTargetObject()
        checkCameraPermission()
    }

    private fun loadTargetObject() {
        val category = photoCategory?.let {
            try { PhotoCategory.valueOf(it) } catch (e: Exception) { null }
        } ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val enabled = repository?.getEnabledObjectsForCategory(category) ?: listOf()

                requireActivity().runOnUiThread {
                    if (enabled.isEmpty()) {
                        availableObjects = PhotoObject.getByCategory(category)
                    } else {
                        availableObjects = enabled
                    }
                    if (availableObjects.isNotEmpty()) {
                        currentObject = availableObjects.random()
                        showTargetObject()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    tvPhotoStatus.text = getString(R.string.photo_error)
                }
                e.printStackTrace()
            }
        }
    }

    private fun showTargetObject() {
        val obj = currentObject ?: return
        val name = getString(obj.stringRes)
        tvTargetObject.text = "${obj.emoji} $name"
        tvAttempts.text = ""
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )

                tvPhotoStatus.text = getString(R.string.photo_ready)

            } catch (e: Exception) {
                tvPhotoStatus.text = getString(R.string.photo_error)
                throw MissionPhotoException(ErrorCode.MISSION_PHOTO_ERROR, e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        btnTakePhoto.isEnabled = false
        tvPhotoStatus.text = getString(R.string.photo_analyzing)

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {

                @ExperimentalGetImage
                override fun onCaptureSuccess(image: ImageProxy) {
                    val mediaImage = image.image
                    if (mediaImage == null) {
                        image.close()
                        return
                    }
                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        image.imageInfo.rotationDegrees
                    )
                    analyzeImage(inputImage, image)
                }

                override fun onError(exception: ImageCaptureException) {
                    requireActivity().runOnUiThread {
                        btnTakePhoto.isEnabled = true
                        tvPhotoStatus.text = getString(R.string.photo_error)
                    }
                }
            }
        )
    }

    @ExperimentalGetImage
    private fun analyzeImage(inputImage: InputImage, imageProxy: ImageProxy) {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.65f)
            .build()

        val labeler = ImageLabeling.getClient(options)

        labeler.process(inputImage)
            .addOnSuccessListener { labels ->
                imageProxy.close()

                val labelTexts = labels.map { it.text }
                val topLabels = labels.take(3).joinToString(", ") {
                    "${it.text} (${(it.confidence * 100).toInt()}%)"
                }

                requireActivity().runOnUiThread {
                    tvDetectedObject.text = getString(R.string.detected_objects, topLabels)

                    val target = currentObject
                    if (target != null && PhotoObject.verifyLabels(labelTexts, target)) {
                        tvPhotoStatus.text = getString(R.string.photo_success)
                        onMissionSuccess?.invoke()
                    } else {
                        onPhotoFailed()
                    }
                }
            }
            .addOnFailureListener {
                imageProxy.close()
                requireActivity().runOnUiThread {
                    btnTakePhoto.isEnabled = true
                    tvPhotoStatus.text = getString(R.string.photo_error)
                }
                throw MissionPhotoException(ErrorCode.MISSION_PHOTO_ERROR, it)
            }
    }

    private fun onPhotoFailed() {
        attempts++
        btnTakePhoto.isEnabled = true

        tvAttempts.text = getString(R.string.attempt_count, attempts)

        when {
            attempts <= 2 -> {
                tvPhotoStatus.text = getString(R.string.hint_try_angle)
            }
            attempts <= 5 -> {
                tvPhotoStatus.text = getString(R.string.hint_better_light)
            }
            else -> {
                suggestNewObject()
            }
        }
    }

    private fun suggestNewObject() {
        val remaining = availableObjects.filter { it != currentObject }

        if (remaining.isEmpty()) {
            attempts = 0
            tvPhotoStatus.text = getString(R.string.hint_try_angle)
            return
        }

        currentObject = remaining.random()
        attempts = 0
        showTargetObject()

        tvPhotoStatus.text = getString(
            R.string.new_object_suggested,
            "${currentObject?.emoji} ${currentObject?.let { getString(it.stringRes) }}"
        )
    }
}

