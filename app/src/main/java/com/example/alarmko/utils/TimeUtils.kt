package com.example.alarmko.utils

import android.content.Context
import com.example.alarmko.R
import java.util.Calendar

object TimeUtils {

    fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun getNextAlarmMillis(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    fun getTimeUntilAlarm(hour: Int, minute: Int): String {
        val alarmMillis = getNextAlarmMillis(hour, minute)
        val diff = alarmMillis - System.currentTimeMillis()

        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)

        return when {
            hours > 0 -> "${hours}ч ${minutes}мин"
            else -> "${minutes}мин"
        }
    }

    // Добавихме context за да четем strings вместо hardcoded текст
    fun formatDaysShort(repeatDays: String, context: Context): String {
        if (repeatDays.isEmpty()) return context.getString(R.string.repeat_once)
        if (repeatDays == "1234567") return context.getString(R.string.repeat_daily)
        if (repeatDays == "12345") return context.getString(R.string.repeat_weekdays)

        val dayNames = mapOf(
            '1' to context.getString(R.string.monday),
            '2' to context.getString(R.string.tuesday),
            '3' to context.getString(R.string.wednesday),
            '4' to context.getString(R.string.thursday),
            '5' to context.getString(R.string.friday),
            '6' to context.getString(R.string.saturday),
            '7' to context.getString(R.string.sunday)
        )
        return repeatDays.map { dayNames[it] }.joinToString(" ")
    }

    fun getTodayDayNumber(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "1"
            Calendar.TUESDAY -> "2"
            Calendar.WEDNESDAY -> "3"
            Calendar.THURSDAY -> "4"
            Calendar.FRIDAY -> "5"
            Calendar.SATURDAY -> "6"
            Calendar.SUNDAY -> "7"
            else -> "1"
        }
    }
}