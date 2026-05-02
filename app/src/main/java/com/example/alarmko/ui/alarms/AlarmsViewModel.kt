package com.example.alarmko.ui.alarms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.alarmko.alarm.AlarmScheduler
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.repository.AlarmRepository
import kotlinx.coroutines.launch

class AlarmsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmRepository(application)

    val allAlarms: LiveData<List<Alarm>> = repository.allAlarms

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                repository.deleteAlarm(alarm)
                AlarmScheduler(getApplication()).cancel(alarm)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}