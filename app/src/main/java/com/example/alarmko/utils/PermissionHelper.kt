package com.example.alarmko.utils

import android.Manifest
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    // Проверява дали battery optimization е изключен за нашето приложение.
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    // Отваря системния диалог за изключване на battery optimization.
    fun openBatteryOptimizationSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    // Проверява дали телефонът е Xiaomi/Redmi/POCO
    fun isMiui(): Boolean {
        return !getSystemProperty("ro.miui.ui.version.name").isNullOrBlank()
    }

    // Отваря MIUI Autostart настройките
    fun openMiuiAutoStart(context: Context) {
        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val miuiAutoStartComponents = listOf(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            ),
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.securitycenter.MainActivity"
            )
        )

        var opened = false
        for (component in miuiAutoStartComponents) {
            try {
                intent.component = component
                context.startActivity(intent)
                opened = true
                break
            } catch (e: Exception) {
                continue
            }
        }

        if (!opened) {
            openAppSettings(context)
        }
    }

    // Чете системен property
    private fun getSystemProperty(key: String): String? {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val get = clazz.getMethod("get", String::class.java)
            get.invoke(null, key) as? String
        } catch (e: Exception) {
            null
        }
    }
    // Проверява дали има разрешение за разпознаване на физическа активност (Android 10+)
    fun hasActivityRecognitionPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // На Android 9 и по-стари не е нужно разрешение
        }
    }

    // Отваря настройките на приложението за ръчно даване на разрешение
    fun openActivityRecognitionSettings(context: Context) {
        openAppSettings(context)
    }
}