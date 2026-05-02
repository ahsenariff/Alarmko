package com.example.alarmko.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.model.AlarmLog
import com.example.alarmko.data.repository.AlarmRepository
import com.example.alarmko.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmRepository(application)

    val allAlarms: LiveData<List<Alarm>> = repository.allAlarms
    val allLogs: LiveData<List<AlarmLog>> = repository.allLogs

    fun getNextAlarm(alarms: List<Alarm>): Alarm? {
        return alarms
            .filter { it.isActive }
            .sortedBy { TimeUtils.getNextAlarmMillis(it.hour, it.minute) }
            .firstOrNull()
    }

    fun calculateStreak(logs: List<AlarmLog>): Int {
        if (logs.isEmpty()) return 0

        val sorted = logs.sortedByDescending { it.triggeredAt }
        var streak = 0
        var lastDate = ""

        for (log in sorted) {
            if (!log.missionSuccess) break
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.format(Date(log.triggeredAt))
            if (lastDate.isEmpty() || date != lastDate) {
                streak++
                lastDate = date
            }
        }
        return streak
    }
}