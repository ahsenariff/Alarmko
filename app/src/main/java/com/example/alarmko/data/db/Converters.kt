package com.example.alarmko.data.db

import androidx.room.TypeConverter
import com.example.alarmko.data.model.MissionType

class Converters {

    @TypeConverter
    fun fromMissionType(value: MissionType): String {
        return value.name
    }

    @TypeConverter
    fun toMissionType(value: String): MissionType {
        return MissionType.valueOf(value)
    }
}