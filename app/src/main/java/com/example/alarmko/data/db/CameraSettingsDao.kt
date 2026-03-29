package com.example.alarmko.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.alarmko.data.model.CameraSettings

@Dao
interface CameraSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: CameraSettings)

    @Query("SELECT * FROM camera_settings")
    fun getAll(): LiveData<List<CameraSettings>>

    @Query("SELECT * FROM camera_settings WHERE isEnabled = 1")
    suspend fun getEnabledObjects(): List<CameraSettings>

    @Query("SELECT * FROM camera_settings WHERE objectName = :name")
    suspend fun getByName(name: String): CameraSettings?
}