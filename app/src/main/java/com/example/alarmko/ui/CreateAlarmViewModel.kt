package com.example.alarmko.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alarmko.alarm.AlarmScheduler
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.repository.AlarmRepository
import kotlinx.coroutines.launch

class CreateAlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AlarmRepository(application)
    private val _alarm = MutableLiveData<Alarm?>()
    val alarm: LiveData<Alarm?> = _alarm
    private val _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> = _saveResult

    fun loadAlarm(alarmId: Int) {
        viewModelScope.launch {
            _alarm.postValue(repository.getAlarmById(alarmId))
        }
    }

    fun saveAlarm(alarm: Alarm, editingAlarmId: Int) {
        viewModelScope.launch {
            try {
                if (editingAlarmId == -1) {
                    val id = repository.insertAlarm(alarm)
                    val savedAlarm = alarm.copy(id = id.toInt())
                    AlarmScheduler(getApplication()).schedule(savedAlarm)
                } else {
                    val updatedAlarm = alarm.copy(id = editingAlarmId)
                    repository.updateAlarm(updatedAlarm)
                    AlarmScheduler(getApplication()).cancel(updatedAlarm)
                    AlarmScheduler(getApplication()).schedule(updatedAlarm)
                }
                _saveResult.postValue(SaveResult.Success)
            } catch (e: Exception) {
                _saveResult.postValue(SaveResult.Error)
            }
        }
    }

    sealed class SaveResult {
        object Success : SaveResult()
        object Error : SaveResult()
    }
}