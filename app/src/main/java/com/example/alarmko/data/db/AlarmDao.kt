package com.example.alarmko.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.alarmko.data.model.Alarm

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm): Long

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAll(): LiveData<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getById(id: Int): Alarm?

    @Query("SELECT * FROM alarms WHERE isActive = 1")
    suspend fun getActiveAlarms(): List<Alarm>
}