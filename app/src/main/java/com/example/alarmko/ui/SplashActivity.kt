package com.example.alarmko.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmko.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivLogo = findViewById<ImageView>(R.id.ivSplashLogo)
        val tvTagline = findViewById<TextView>(R.id.tvAppName)

        // Анимация на иконата — подскача
        val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce)
        ivLogo.startAnimation(bounceAnim)

        // Анимация на текста — изплува отдолу
        val slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        tvTagline.startAnimation(slideUpAnim)

        // След 2.5 секунди отваря MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2500)
    }
}