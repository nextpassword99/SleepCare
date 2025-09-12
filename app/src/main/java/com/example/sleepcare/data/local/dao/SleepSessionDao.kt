package com.example.sleepcare.data.local.dao

import androidx.room.*
import com.example.sleepcare.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface SleepSessionDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SleepSessionEntity>>
    
    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SleepSessionEntity?
    
    @Query("SELECT * FROM sleep_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsBetweenDates(startDate: Date, endDate: Date): Flow<List<SleepSessionEntity>>
    
    @Query("SELECT * FROM sleep_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): SleepSessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSessionEntity): Long
    
    @Update
    suspend fun updateSession(session: SleepSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: SleepSessionEntity)
    
    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
    
    @Query("SELECT * FROM sleep_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<SleepSessionEntity>
    
    @Query("SELECT AVG(sleepScore) FROM sleep_sessions WHERE startTime >= :startDate")
    suspend fun getAverageSleepScoreSince(startDate: Date): Float?
    
    @Query("SELECT AVG(durationMinutes) FROM sleep_sessions WHERE startTime >= :startDate")
    suspend fun getAverageSleepDurationSince(startDate: Date): Float?
}
