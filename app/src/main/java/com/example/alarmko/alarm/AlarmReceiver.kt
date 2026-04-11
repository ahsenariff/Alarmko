package com.example.alarmko.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarmko.data.repository.AlarmRepository
import com.example.alarmko.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            // Стартираме AlarmService
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("ALARM_ID", alarmId)
            }
            context.startForegroundService(serviceIntent)

            // Пренасрочваме ако е повтаряща се аларма
            val scheduler = AlarmScheduler(context)
            val repository = AlarmRepository(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarm = repository.getAlarmById(alarmId) ?: return@launch
                    // Пренасрочваме само ако има избрани дни за повторение
                    if (alarm.repeatDays.isNotEmpty()) {
                        scheduler.schedule(alarm)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}