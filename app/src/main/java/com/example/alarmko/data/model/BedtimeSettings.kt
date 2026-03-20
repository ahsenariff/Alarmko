package com.example.alarmko.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bedtime_settings")
data class BedtimeSettings(
    @PrimaryKey
    val id: Int = 1,
    val sleepGoalHours: Int = 8,
    val bedtimeHour: Int = 22,
    val bedtimeMinute: Int = 0,
    val isEnabled: Boolean = false
)