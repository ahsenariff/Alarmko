package com.example.alarmko.utils

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

    fun formatDaysShort(repeatDays: String): String {
        if (repeatDays.isEmpty()) return "Еднократно"
        if (repeatDays == "1234567") return "Всеки ден"
        if (repeatDays == "12345") return "Пон - Пет"

        val dayNames = mapOf(
            '1' to "Пн", '2' to "Вт", '3' to "Ср",
            '4' to "Чт", '5' to "Пт", '6' to "Сб", '7' to "Нд"
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