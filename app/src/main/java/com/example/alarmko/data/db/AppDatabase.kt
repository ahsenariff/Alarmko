package com.example.alarmko.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.model.AlarmLog
import com.example.alarmko.data.model.BedtimeSettings
import com.example.alarmko.data.model.CameraSettings

@Database(
    entities = [Alarm::class, AlarmLog::class, BedtimeSettings::class, CameraSettings::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun alarmLogDao(): AlarmLogDao
    abstract fun bedtimeDao(): BedtimeDao

    abstract fun cameraSettingsDao(): CameraSettingsDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS camera_settings (
                    objectName TEXT NOT NULL PRIMARY KEY,
                    isEnabled INTEGER NOT NULL DEFAULT 0
                )"""
                )
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN photoCategory TEXT"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "alarmko_database"
                    )
                        .addMigrations(MIGRATION_1_2,  MIGRATION_2_3)
                        .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}