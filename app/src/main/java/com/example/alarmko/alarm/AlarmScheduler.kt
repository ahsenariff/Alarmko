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

            val nextAlarmTime = getNextAlarmTime(alarm)

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(nextAlarmTime, pendingIntent),
                pendingIntent
            )

            if (alarm.notifyBeforeMinutes > 0) {
                scheduleNotification(alarm, nextAlarmTime)
            }
        } catch (e: SecurityException) {
            throw AlarmPermissionException(ErrorCode.ALARM_PERMISSION_DENIED)
        } catch (e: Exception) {
            throw AlarmSchedulingException(ErrorCode.ALARM_SCHEDULING_FAILED, e)
        }
    }

    private fun getNextAlarmTime(alarm: Alarm): Long {
        val calendar = Calendar.getInstance()

        // Задаваме часа и минутата
        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
        calendar.set(Calendar.MINUTE, alarm.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Ако няма избрани дни — еднократна аларма
        if (alarm.repeatDays.isEmpty()) {
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }

        // Намираме следващия ден от избраните
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val alarmDays = alarm.repeatDays.map { it.digitToInt() }

        // Конвертираме нашите дни (1=Пн...7=Нд) към Calendar дни
        fun ourDayToCalendar(day: Int): Int = when(day) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }

        // Търсим следващия ден
        for (daysAhead in 0..7) {
            val checkCalendar = Calendar.getInstance()
            checkCalendar.add(Calendar.DAY_OF_YEAR, daysAhead)
            checkCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
            checkCalendar.set(Calendar.MINUTE, alarm.minute)
            checkCalendar.set(Calendar.SECOND, 0)
            checkCalendar.set(Calendar.MILLISECOND, 0)

            val checkDay = checkCalendar.get(Calendar.DAY_OF_WEEK)

            val isSelectedDay = alarmDays.any { ourDayToCalendar(it) == checkDay }

            if (isSelectedDay && checkCalendar.timeInMillis > System.currentTimeMillis()) {
                return checkCalendar.timeInMillis
            }
        }

        // Fallback — следващия ден
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
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