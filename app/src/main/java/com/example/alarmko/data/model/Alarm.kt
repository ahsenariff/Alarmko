package com.example.alarmko.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val hour: Int,
    val minute: Int,
    val repeatDays: String,
    val missionType: MissionType,
    val missionDifficulty: Int = 1,
    val notifyBeforeMinutes: Int = 0,
    val isActive: Boolean = true
) {
}