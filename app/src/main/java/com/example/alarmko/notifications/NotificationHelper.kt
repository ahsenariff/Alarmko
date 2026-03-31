package com.example.alarmko.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.alarmko.R
import com.example.alarmko.exceptions.AlarmPermissionException
import com.example.alarmko.exceptions.NotificationException
import com.example.alarmko.exceptions.NotificationPermissionException
import com.example.alarmko.exceptions.ErrorCode
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "alarmko_notification_channel"
        const val NOTIFICATION_ID_OFFSET = 20000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_title),
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    fun showPreAlarmNotification(alarmId: Int, minutesBefore: Int) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text, minutesBefore))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID_OFFSET + alarmId, notification)
        } catch (e: SecurityException) {
            throw NotificationPermissionException(ErrorCode.NOTIFICATION_PERMISSION_DENIED)
        } catch (e: Exception) {
            throw NotificationException(ErrorCode.NOTIFICATION_ERROR, e)
        }
    }

    fun showBedtimeNotification(wakeUpTime: String) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(context.getString(R.string.bedtime_notification_title))
                .setContentText(context.getString(R.string.bedtime_notification_text, wakeUpTime))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID_OFFSET + 1, notification)
        } catch (e: SecurityException) {
            throw NotificationPermissionException(ErrorCode.NOTIFICATION_PERMISSION_DENIED)
        } catch (e: Exception) {
            throw NotificationException(ErrorCode.NOTIFICATION_ERROR, e)
        }
    }
}