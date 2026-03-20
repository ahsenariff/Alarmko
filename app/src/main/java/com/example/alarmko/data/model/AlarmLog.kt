package com.example.alarmko.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarm_log",
    foreignKeys = [ForeignKey(
        entity = Alarm::class,
        parentColumns = ["id"],
        childColumns = ["alarmId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AlarmLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val alarmId: Int,
    val triggeredAt: Long,
    val completedAt: Long? = null,
    val missionSuccess: Boolean = false,
    val snoozedCount: Int = 0
)