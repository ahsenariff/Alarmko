package com.example.alarmko.missions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.alarmko.R
import com.example.alarmko.exceptions.MissionQrException
import com.example.alarmko.exceptions.ErrorCode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class QrMissionFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var tvQrStatus: TextView
    private lateinit var tvQrInstruction: TextView

    private var expectedQrCode: String? = null
    private var isScanning = true
    private var onMissionSuccess: (() -> Unit)? = null

    fun setExpectedQrCode(code: String) {
        expectedQrCode = code
    }

    fun setOnMissionSuccessListener(listener: () -> Unit) {
        onMissionSuccess = listener
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            tvQrStatus.text = getString(R.string.camera_permission_denied)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mission_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewView)
        tvQrStatus = view.findViewById(R.id.tvQrStatus)
        tvQrInstruction = view.findViewById(R.id.tvQrInstruction)

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                            analyzeImage(imageProxy)
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                throw MissionQrException(ErrorCode.MISSION_QR_NOT_FOUND, e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun analyzeImage(imageProxy: androidx.camera.core.ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val value = barcode.rawValue ?: continue

                    if (expectedQrCode == null || value == expectedQrCode) {
                        isScanning = false
                        requireActivity().runOnUiThread {
                            tvQrStatus.text = getString(R.string.qr_success)
                            onMissionSuccess?.invoke()
                        }
                        break
                    } else {
                        requireActivity().runOnUiThread {
                            tvQrStatus.text = getString(R.string.qr_wrong_code)
                        }
                    }
                }
            }
            .addOnFailureListener {
                throw MissionQrException(ErrorCode.MISSION_QR_NOT_FOUND, it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
    }
