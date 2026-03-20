package com.example.alarmko.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.model.AlarmLog
import com.example.alarmko.data.model.BedtimeSettings

@Database(
    entities = [Alarm::class, AlarmLog::class, BedtimeSettings::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun alarmLogDao(): AlarmLogDao
    abstract fun bedtimeDao(): BedtimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alarmko_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}