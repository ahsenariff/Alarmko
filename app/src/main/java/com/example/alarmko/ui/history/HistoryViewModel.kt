package com.example.alarmko.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.alarmko.data.model.AlarmLog
import com.example.alarmko.data.repository.AlarmRepository

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AlarmRepository(application)

    val allLogs: LiveData<List<AlarmLog>> = repository.allLogs.map { logs ->
        logs.sortedByDescending { it.triggeredAt }
    }
}