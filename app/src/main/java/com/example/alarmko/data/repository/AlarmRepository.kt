package com.example.alarmko.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.alarmko.data.db.AppDatabase
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.model.AlarmLog
import com.example.alarmko.data.model.BedtimeSettings
import com.example.alarmko.data.model.CameraSettings
import com.example.alarmko.exceptions.DatabaseException
import com.example.alarmko.exceptions.DatabaseInsertException
import com.example.alarmko.exceptions.DatabaseUpdateException
import com.example.alarmko.exceptions.DatabaseDeleteException
import com.example.alarmko.exceptions.ErrorCode

class AlarmRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val alarmDao = db.alarmDao()
    private val alarmLogDao = db.alarmLogDao()
    private val bedtimeDao = db.bedtimeDao()

    // Аларми
    val allAlarms: LiveData<List<Alarm>> = alarmDao.getAll()

    suspend fun insertAlarm(alarm: Alarm): Long {
        return try {
            alarmDao.insert(alarm)
        } catch (e: Exception) {
            throw DatabaseInsertException(ErrorCode.DATABASE_INSERT_FAILED, e)
        }
    }

    suspend fun updateAlarm(alarm: Alarm) {
        try {
            alarmDao.update(alarm)
        } catch (e: Exception) {
            throw DatabaseUpdateException(ErrorCode.DATABASE_UPDATE_FAILED, e)
        }
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        try {
            alarmDao.delete(alarm)
        } catch (e: Exception) {
            throw DatabaseDeleteException(ErrorCode.DATABASE_DELETE_FAILED, e)
        }
    }

    suspend fun insertLog(alarmLog: AlarmLog) {
        try {
            alarmLogDao.insert(alarmLog)
        } catch (e: Exception) {
            throw DatabaseInsertException(ErrorCode.DATABASE_INSERT_FAILED, e)
        }
    }

    suspend fun getAlarmById(id: Int): Alarm? {
        return alarmDao.getById(id)
    }

    suspend fun getActiveAlarms(): List<Alarm> {
        return alarmDao.getActiveAlarms()
    }

    // История
    val allLogs: LiveData<List<AlarmLog>> = alarmLogDao.getAll()


    suspend fun getSuccessCountSince(fromDate: Long): Int {
        return alarmLogDao.getSuccessCountSince(fromDate)
    }

    // Bedtime
    val bedtimeSettings: LiveData<BedtimeSettings?> = bedtimeDao.get()

    suspend fun saveBedtimeSettings(settings: BedtimeSettings) {
        bedtimeDao.insert(settings)
    }

    suspend fun updateBedtimeSettings(settings: BedtimeSettings) {
        bedtimeDao.update(settings)
    }

    suspend fun getBedtimeSettingsOnce(): BedtimeSettings? {
        return bedtimeDao.getOnce()
    }
    private val cameraSettingsDao = db.cameraSettingsDao()

    // Camera Settings
    val allCameraSettings: LiveData<List<CameraSettings>> =
        cameraSettingsDao.getAll()

    suspend fun saveCameraSettings(settings: CameraSettings) {
        try {
            cameraSettingsDao.insert(settings)
        } catch (e: Exception) {
            throw DatabaseInsertException(ErrorCode.DATABASE_INSERT_FAILED, e)
        }
    }

    suspend fun getEnabledObjects(): List<CameraSettings> {
        return try {
            cameraSettingsDao.getEnabledObjects()
        } catch (e: Exception) {
            throw DatabaseException(ErrorCode.DATABASE_ERROR, e)
        }
    }

    suspend fun getEnabledObjectsForCategory(
        category: com.example.alarmko.data.model.PhotoCategory
    ): List<com.example.alarmko.data.model.PhotoObject> {
        return try {
            val enabled = cameraSettingsDao.getEnabledObjects()
            enabled.mapNotNull { settings ->
                try {
                    val obj = com.example.alarmko.data.model.PhotoObject
                        .valueOf(settings.objectName)
                    if (obj.category == category) obj else null
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        } catch (e: Exception) {
            throw DatabaseException(ErrorCode.DATABASE_ERROR, e)
        }
    }
}