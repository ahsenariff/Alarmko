package com.example.alarmko.missions

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.alarmko.R
import com.example.alarmko.exceptions.ErrorCode
import com.example.alarmko.exceptions.MissionSensorException

class StepMissionFragment : Fragment(), SensorEventListener {
    private var stepTarget: Int = 20
    private var stepCount: Int = 0
    private var onMissionSuccess: (() -> Unit)? = null
    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null
    private lateinit var tvStepCount: TextView
    private lateinit var tvStepTarget: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSensorUnavailable: TextView

    fun setStepTarget(target: Int) {
        stepTarget = target
    }
    fun setOnMissionSuccessListener(listener: () -> Unit) {
        onMissionSuccess = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mission_steps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvStepCount = view.findViewById(R.id.tvStepCount)
        tvStepTarget = view.findViewById(R.id.tvStepTarget)
        progressBar = view.findViewById(R.id.progressSteps)
        tvSensorUnavailable = view.findViewById(R.id.tvSensorUnavailable)

        progressBar.max = stepTarget
        tvStepTarget.text = getString(R.string.step_target, stepTarget)
        tvStepCount.text = "0"

        sensorManager = requireContext()
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager

        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepDetector == null) {
            tvSensorUnavailable.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        } else {
            try {
                sensorManager.registerListener(
                    this,
                    stepDetector,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            } catch (e: Exception) {
                throw MissionSensorException(ErrorCode.MISSION_FAILED, e)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        try {
            if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
                stepCount++

                requireActivity().runOnUiThread {
                    tvStepCount.text = stepCount.toString()
                    progressBar.progress = stepCount

                    if (stepCount >= stepTarget) {
                        sensorManager.unregisterListener(this)
                        onMissionSuccess?.invoke()
                    }
                }
            }
        } catch (e: Exception) {
            throw MissionSensorException(ErrorCode.MISSION_SENSOR_ERROR, e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(this)
    }
}