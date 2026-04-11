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

        // TYPE_STEP_DETECTOR засича всяка отделна стъпка
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepDetector == null) {
            // Сензорът не е наличен на устройството
            tvSensorUnavailable.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        } else {
            sensorManager.registerListener(
                this,
                stepDetector,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            // TYPE_STEP_DETECTOR връща 1.0 за всяка засечена стъпка
            stepCount++

            requireActivity().runOnUiThread {
                tvStepCount.text = stepCount.toString()
                progressBar.progress = stepCount

                if (stepCount >= stepTarget) {
                    // Спираме listener-а веднага след успех
                    sensorManager.unregisterListener(this)
                    onMissionSuccess?.invoke()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Не е нужно за стъпкомера
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Задължително — спираме сензора когато fragment-ът се унищожава
        sensorManager.unregisterListener(this)
    }
}