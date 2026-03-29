package com.example.alarmko.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camera_settings")
data class CameraSettings(
    @PrimaryKey
    val objectName: String,  // името на PhotoObject enum стойността
    val isEnabled: Boolean = false
)