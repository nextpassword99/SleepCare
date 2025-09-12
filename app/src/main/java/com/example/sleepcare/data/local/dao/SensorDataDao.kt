package com.example.sleepcare.data.local.dao

import androidx.room.*
import com.example.sleepcare.data.local.entity.SensorDataEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface SensorDataDao {
    @Query("SELECT * FROM sensor_data WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getSensorDataForSession(sessionId: Long): Flow<List<SensorDataEntity>>
    
    @Query("SELECT * FROM sensor_data WHERE sessionId = :sessionId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getSensorDataForSessionInTimeRange(
        sessionId: Long,
        startTime: Date,
        endTime: Date
    ): Flow<List<SensorDataEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorData(data: SensorDataEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleSensorData(data: List<SensorDataEntity>)
    
    @Update
    suspend fun updateSensorData(data: SensorDataEntity)
    
    @Query("DELETE FROM sensor_data WHERE sessionId = :sessionId")
    suspend fun deleteSensorDataForSession(sessionId: Long)
    
    @Query("SELECT * FROM sensor_data WHERE isProcessed = 0 AND sessionId = :sessionId")
    suspend fun getUnprocessedSensorData(sessionId: Long): List<SensorDataEntity>
    
    @Query("""
        SELECT AVG(soundLevel) 
        FROM sensor_data 
        WHERE sessionId = :sessionId 
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun getAverageSoundLevel(sessionId: Long, startTime: Date, endTime: Date): Float?
    
    @Query("""
        SELECT COUNT(*) 
        FROM sensor_data 
        WHERE sessionId = :sessionId 
        AND soundLevel > :threshold 
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun countSoundEventsAboveThreshold(
        sessionId: Long,
        threshold: Float,
        startTime: Date,
        endTime: Date
    ): Int
    
    @Query("""
        SELECT COUNT(*) 
        FROM sensor_data 
        WHERE sessionId = :sessionId 
        AND (ABS(movementX) > :threshold OR ABS(movementY) > :threshold OR ABS(movementZ) > :threshold)
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun countMovementEventsAboveThreshold(
        sessionId: Long,
        threshold: Float,
        startTime: Date,
        endTime: Date
    ): Int
