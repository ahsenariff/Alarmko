package com.example.alarmko.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarmko.notifications.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val isNotification = intent.getBooleanExtra("IS_NOTIFICATION", false)

        if (alarmId == -1) return

        if (isNotification) {
            val minutesBefore = intent.getIntExtra("NOTIFY_BEFORE_MINUTES", 15)
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showPreAlarmNotification(alarmId, minutesBefore)
        } else {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("ALARM_ID", alarmId)
            }
            context.startForegroundService(serviceIntent)
        }
    }
}