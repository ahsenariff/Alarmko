package com.example.alarmko.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmko.alarm.AlarmService
import com.example.alarmko.R

class AlarmRingActivity : AppCompatActivity() {

    private var alarmId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindowFlags()
        setContentView(R.layout.activity_alarm_ring)

        alarmId = intent.getIntExtra("ALARM_ID", -1)
        if (alarmId == -1) {
            finish()
            return
        }

        // Блокираме back бутона
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // не правим нищо — не може да се излезе без мисия
            }
        })
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
    }

    fun dismissAlarm() {
        stopService(Intent(this, AlarmService::class.java))
        finish()
    }
}