package com.example.alarmko.alarm


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.alarmko.data.model.Alarm
import java.util.Calendar
import com.example.alarmko.exceptions.AlarmPermissionException
import com.example.alarmko.exceptions.AlarmSchedulingException
import com.example.alarmko.exceptions.ErrorCode
class AlarmScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarm.id)
                putExtra("ALARM_TITLE", alarm.title)
                putExtra("MISSION_TYPE", alarm.missionType.name)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )

            if (alarm.notifyBeforeMinutes > 0) {
                scheduleNotification(alarm, calendar.timeInMillis)
            }
        } catch (e: SecurityException) {
            throw AlarmPermissionException(ErrorCode.ALARM_PERMISSION_DENIED)
        } catch (e: Exception) {
            throw AlarmSchedulingException(ErrorCode.ALARM_SCHEDULING_FAILED, e)
        }
    }

    fun cancel(alarm: Alarm) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            throw AlarmSchedulingException(ErrorCode.ALARM_SCHEDULING_FAILED, e)
        }
    }

    private fun scheduleNotification(alarm: Alarm, alarmTimeMillis: Long) {
        val notifyIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("IS_NOTIFICATION", true)
            putExtra("NOTIFY_BEFORE_MINUTES", alarm.notifyBeforeMinutes) // ← добавяме
        }

        val notifyPendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id + 10000,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifyTime = alarmTimeMillis - (alarm.notifyBeforeMinutes * 60 * 1000L)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notifyTime,
            notifyPendingIntent
        )
    }
}