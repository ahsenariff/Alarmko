package com.example.alarmko.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alarmko.R
import com.example.alarmko.alarm.AlarmScheduler
import com.example.alarmko.alarm.AlarmService
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.model.AlarmLog
import com.example.alarmko.data.model.MissionType
import com.example.alarmko.data.repository.AlarmRepository
import com.example.alarmko.missions.MathMissionFragment
import com.example.alarmko.missions.PhotoMissionFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRingActivity : AppCompatActivity() {

    private var alarmId: Int = -1
    private lateinit var repository: AlarmRepository
    private var currentAlarm: Alarm? = null  // ← запазваме алармата като поле

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowFlags()
        setContentView(R.layout.activity_alarm_ring)

        repository = AlarmRepository(this)
        alarmId = intent.getIntExtra("ALARM_ID", -1)

        if (alarmId == -1) {
            finish()
            return
        }

        setupUI()
        setupBackButton()
    }

    private fun setupUI() {
        lifecycleScope.launch {
            val alarm = repository.getAlarmById(alarmId) ?: run {
                finish()
                return@launch
            }

            currentAlarm = alarm  // ← запазваме за по-късно

            runOnUiThread {
                val tvTime = findViewById<TextView>(R.id.tvTime)
                val tvDate = findViewById<TextView>(R.id.tvDate)
                val tvTaskDescription = findViewById<TextView>(R.id.tvTaskDescription)
                val btnStartMission = findViewById<MaterialButton>(R.id.btnStartMission)
                val btnSnooze = findViewById<MaterialButton>(R.id.btnSnooze)

                tvTime.text = String.format("%02d:%02d", alarm.hour, alarm.minute)

                val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
                tvDate.text = dateFormat.format(Date())

                tvTaskDescription.text = alarm.title

                btnSnooze.setOnClickListener {
                    snoozeAlarm()
                }

                btnStartMission.setOnClickListener {
                    startMission(alarm)  // ← подаваме цялата аларма
                }
            }
        }
    }

    private fun snoozeAlarm() {
        try {
            val scheduler = AlarmScheduler(this)
            val snoozeTimeMillis = System.currentTimeMillis() + (5 * 60 * 1000L)

            // Създаваме временна аларма за след 5 минути
            val snoozeAlarm = currentAlarm?.copy(
                id = alarmId,
                hour = getHourFromMillis(snoozeTimeMillis),
                minute = getMinuteFromMillis(snoozeTimeMillis)
            )

            snoozeAlarm?.let {
                scheduler.schedule(it)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        stopService(Intent(this, AlarmService::class.java))
        finish()
    }

    private fun getHourFromMillis(millis: Long): Int {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = millis
        return calendar.get(java.util.Calendar.HOUR_OF_DAY)
    }

    private fun getMinuteFromMillis(millis: Long): Int {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = millis
        return calendar.get(java.util.Calendar.MINUTE)
    }

    private fun startMission(alarm: Alarm) {
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardTask).visibility = View.GONE
        findViewById<TextView>(R.id.tvMissionHint).visibility = View.GONE
        findViewById<MaterialButton>(R.id.btnStartMission).visibility = View.GONE

        val fragment = when (alarm.missionType) {
            MissionType.MATH -> MathMissionFragment().also { mission ->
                mission.setOnMissionSuccessListener {
                    onMissionSuccess()
                }
            }
            MissionType.PHOTO -> PhotoMissionFragment().also { mission ->
                // Подаваме категорията и repository-то
                mission.setPhotoCategory(alarm.photoCategory)
                mission.setRepository(repository)
                mission.setOnMissionSuccessListener {
                    onMissionSuccess()
                }
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.missionContainer, fragment)
            .commit()
    }

    private fun onMissionSuccess() {
        lifecycleScope.launch {
            try {
                val log = AlarmLog(
                    alarmId = alarmId,
                    triggeredAt = System.currentTimeMillis(),
                    completedAt = System.currentTimeMillis(),
                    missionSuccess = true,
                    snoozedCount = 0
                )
                repository.insertLog(log)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        stopService(Intent(this, AlarmService::class.java))

        // Показваме успех екрана
        startActivity(Intent(this, MissionSuccessActivity::class.java))
        finish()
    }

    private fun setupWindowFlags() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // За Android 8+ — отключи keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(KEYGUARD_SERVICE)
                    as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // блокираме back бутона
                }
            }
        )
    }
}