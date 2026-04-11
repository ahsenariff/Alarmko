package com.example.alarmko.ui

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.alarmko.R
import com.example.alarmko.utils.PermissionHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // След нотификации → Activity Recognition
        checkActivityRecognition()
    }

    private val activityRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // След Activity Recognition → Battery Optimization
        checkBatteryOptimization()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Прилагаме темата ПРЕДИ setContentView
        val prefs = getSharedPreferences("alarmko_prefs", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_theme", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        this.prefs = prefs

        if (!prefs.getBoolean("permissions_requested", false)) {
            showInitialPermissionDialog()
        }
    }

    private fun showInitialPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.permission_dialog_title))
            .setMessage(getString(R.string.permission_dialog_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.permission_dialog_button)) { _, _ ->
                prefs.edit().putBoolean("permissions_requested", true).apply()
                startPermissionFlow()
            }
            .show()
    }

    // Верига: Нотификации → Activity Recognition → Battery → Exact Alarm → MIUI
    private fun startPermissionFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(this)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        checkActivityRecognition()
    }

    private fun checkActivityRecognition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!PermissionHelper.hasActivityRecognitionPermission(this)) {
                activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                return
            }
        }
        checkBatteryOptimization()
    }

    private fun checkBatteryOptimization() {
        if (!PermissionHelper.isBatteryOptimizationIgnored(this)) {
            PermissionHelper.openBatteryOptimizationSettings(this)
        }
        checkExactAlarm()
    }

    private fun checkExactAlarm() {
        if (!PermissionHelper.hasExactAlarmPermission(this)) {
            PermissionHelper.openExactAlarmSettings(this)
        }
        checkMiuiAutoStart()
    }

    private fun checkMiuiAutoStart() {
        if (PermissionHelper.isMiui()) {
            PermissionHelper.openMiuiAutoStart(this)
        }
    }
}