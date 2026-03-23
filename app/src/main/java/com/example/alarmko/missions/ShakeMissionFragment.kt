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
import com.example.alarmko.exceptions.MissionSensorException
import com.example.alarmko.exceptions.ErrorCode
import kotlin.math.sqrt

class ShakeMissionFragment : Fragment(), SensorEventListener {

    private lateinit var tvShakeCount: TextView
    private lateinit var tvShakeTarget: TextView
    private lateinit var progressBar: ProgressBar

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var shakeCount = 0
    private val targetShakes = 10
    private var lastShakeTime = 0L
    private val shakeThreshold = 15f
    private val shakeInterval = 500L

    private var onMissionSuccess: (() -> Unit)? = null

    fun setOnMissionSuccessListener(listener: () -> Unit) {
        onMissionSuccess = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mission_shake, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvShakeCount = view.findViewById(R.id.tvShakeCount)
        tvShakeTarget = view.findViewById(R.id.tvShakeTarget)
        progressBar = view.findViewById(R.id.progressBarShake)

        progressBar.max = targetShakes
        tvShakeTarget.text = getString(R.string.shake_target, targetShakes)

        try {
            sensorManager = requireContext()
                .getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (accelerometer == null) {
                throw MissionSensorException(ErrorCode.MISSION_SHAKE_SENSOR_UNAVAILABLE)
            }
        } catch (e: MissionSensorException) {
            tvShakeCount.text = getString(R.string.error_mission_shake_sensor)
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

            val currentTime = System.currentTimeMillis()
            if (acceleration > shakeThreshold &&
                currentTime - lastShakeTime > shakeInterval) {

                lastShakeTime = currentTime
                shakeCount++

                tvShakeCount.text = shakeCount.toString()
                progressBar.progress = shakeCount

                if (shakeCount >= targetShakes) {
                    onMissionSuccess?.invoke()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}