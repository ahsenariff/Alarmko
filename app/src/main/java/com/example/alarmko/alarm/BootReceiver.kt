package com.example.alarmko.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarmko.data.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.alarmko.exceptions.AlarmSchedulingException
import com.example.alarmko.exceptions.AlarmPermissionException
import com.example.alarmko.exceptions.DatabaseException

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val repository = AlarmRepository(context)
        val scheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeAlarms = repository.getActiveAlarms()
                activeAlarms.forEach { alarm ->
                    try {
                        scheduler.schedule(alarm)
                    } catch (e: AlarmSchedulingException) {
                        e.printStackTrace()
                    } catch (e: AlarmPermissionException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: DatabaseException) {
                e.printStackTrace()
            }
        }
    }
}