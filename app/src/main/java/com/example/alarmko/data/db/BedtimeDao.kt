package com.example.alarmko.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.alarmko.data.model.BedtimeSettings

@Dao
interface BedtimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bedtimeSettings: BedtimeSettings)

    @Update
    suspend fun update(bedtimeSettings: BedtimeSettings)

    @Query("SELECT * FROM bedtime_settings WHERE id = 1")
    fun get(): LiveData<BedtimeSettings?>

    @Query("SELECT * FROM bedtime_settings WHERE id = 1")
    suspend fun getOnce(): BedtimeSettings?
}
