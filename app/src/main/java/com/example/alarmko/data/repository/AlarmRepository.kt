package com.example.alarmko.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.alarmko.data.db.AppDatabase
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.model.AlarmLog
import com.example.alarmko.data.model.BedtimeSettings

class AlarmRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val alarmDao = db.alarmDao()
    private val alarmLogDao = db.alarmLogDao()
    private val bedtimeDao = db.bedtimeDao()

    // Аларми
    val allAlarms: LiveData<List<Alarm>> = alarmDao.getAll()

    suspend fun insertAlarm(alarm: Alarm): Long {
        return alarmDao.insert(alarm)
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.update(alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.delete(alarm)
    }

    suspend fun getAlarmById(id: Int): Alarm? {
        return alarmDao.getById(id)
    }

    suspend fun getActiveAlarms(): List<Alarm> {
        return alarmDao.getActiveAlarms()
    }

    // История
    val allLogs: LiveData<List<AlarmLog>> = alarmLogDao.getAll()

    suspend fun insertLog(alarmLog: AlarmLog) {
        alarmLogDao.insert(alarmLog)
    }

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
}