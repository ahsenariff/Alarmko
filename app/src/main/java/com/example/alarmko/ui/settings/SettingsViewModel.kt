package com.example.alarmko.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.alarmko.data.model.CameraSettings
import com.example.alarmko.data.repository.AlarmRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmRepository(application)

    val allCameraSettings: LiveData<List<CameraSettings>> = repository.allCameraSettings

    fun saveCameraSettings(settings: CameraSettings) {
        viewModelScope.launch {
            try {
                repository.saveCameraSettings(settings)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}