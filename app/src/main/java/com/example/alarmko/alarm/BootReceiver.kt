package com.example.alarmko.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarmko.data.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val repository = AlarmRepository(context)
        val scheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            val activeAlarms = repository.getActiveAlarms()
            activeAlarms.forEach { alarm ->
                scheduler.schedule(alarm)
            }
        }
    }
}