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

    // Регистрираме launcher за POST_NOTIFICATIONS разрешението.
    // ActivityResultLauncher трябва да се регистрира ПРЕДИ onCreate,
    // затова е на ниво клас, не вътре в метод.
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Независимо дали е разрешено или не — продължаваме към следващата стъпка
        checkBatteryOptimization()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        prefs = getSharedPreferences("alarmko_prefs", MODE_PRIVATE)

        // Проверяваме само при първо стартиране
        if (!prefs.getBoolean("permissions_requested", false)) {
            showInitialPermissionDialog()
        }
        // Прилагаме запазената тема при стартиране
        val prefs = getSharedPreferences("alarmko_prefs", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_theme", false)
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    // Показва един единствен диалог — без технически термини
    private fun showInitialPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.permission_dialog_title))
            .setMessage(getString(R.string.permission_dialog_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.permission_dialog_button)) { _, _ ->
                // Запазваме че вече сме питали — няма да питаме пак
                prefs.edit().putBoolean("permissions_requested", true).apply()
                startPermissionFlow()
            }
            .show()
    }

    // Стартира веригата от разрешения — едно след друго
    private fun startPermissionFlow() {
        // Стъпка 1: Нотификации (само Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(this)) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
                return // Спираме тук — продължаваме в callback-а на launcher-а
            }
        }
        // Ако нотификациите са наред — минаваме директно към следващата стъпка
        checkBatteryOptimization()
    }

    // Стъпка 2: Battery optimization
    private fun checkBatteryOptimization() {
        if (!PermissionHelper.isBatteryOptimizationIgnored(this)) {
            PermissionHelper.openBatteryOptimizationSettings(this)
        }
        // Продължаваме независимо — не можем да чакаме резултат от Settings
        checkExactAlarm()
    }

    // Стъпка 3: Exact alarm (само Android 12+)
    private fun checkExactAlarm() {
        if (!PermissionHelper.hasExactAlarmPermission(this)) {
            PermissionHelper.openExactAlarmSettings(this)
        }
        // Стъпка 4: MIUI Autostart (само на Xiaomi/Redmi/POCO)
        checkMiuiAutoStart()
    }

    // Стъпка 4: MIUI Autostart
    private fun checkMiuiAutoStart() {
        if (PermissionHelper.isMiui()) {
            PermissionHelper.openMiuiAutoStart(this)
        }
    }
}