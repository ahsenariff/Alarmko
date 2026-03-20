package com.example.alarmko.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.alarmko.data.model.AlarmLog

@Dao
interface AlarmLogDao {

    @Insert
    suspend fun insert(alarmLog: AlarmLog)

    @Query("SELECT * FROM alarm_log ORDER BY triggeredAt DESC")
    fun getAll(): LiveData<List<AlarmLog>>

    @Query("SELECT * FROM alarm_log WHERE alarmId = :alarmId ORDER BY triggeredAt DESC")
    fun getByAlarmId(alarmId: Int): LiveData<List<AlarmLog>>

    @Query("SELECT COUNT(*) FROM alarm_log WHERE missionSuccess = 1 AND triggeredAt >= :fromDate")
    suspend fun getSuccessCountSince(fromDate: Long): Int

    @Query("DELETE FROM alarm_log WHERE alarmId = :alarmId")
    suspend fun deleteByAlarmId(alarmId: Int)
}