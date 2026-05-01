package com.example.alarmko.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmko.R

class MissionSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_success)

        val tvEmoji = findViewById<TextView>(R.id.tvSuccessEmoji)
        val tvTitle = findViewById<TextView>(R.id.tvSuccessTitle)
        val tvMessage = findViewById<TextView>(R.id.tvSuccessMessage)

        val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce)
        tvEmoji.startAnimation(bounceAnim)

        val slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        tvTitle.startAnimation(slideUpAnim)
        tvMessage.startAnimation(slideUpAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2500)
    }
}