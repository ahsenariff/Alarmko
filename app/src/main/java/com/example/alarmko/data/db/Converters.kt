package com.example.alarmko.data.db

import androidx.room.TypeConverter
import com.example.alarmko.data.model.MissionType
import com.example.alarmko.data.model.PhotoCategory

class Converters {

    @TypeConverter
    fun fromMissionType(value: MissionType): String {
        return value.name
    }

    @TypeConverter
    fun toMissionType(value: String): MissionType {
        return MissionType.valueOf(value)
    }

    @TypeConverter
    fun fromPhotoCategory(value: PhotoCategory?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPhotoCategory(value: String?): PhotoCategory? {
        return value?.let { PhotoCategory.valueOf(it) }
    }
}