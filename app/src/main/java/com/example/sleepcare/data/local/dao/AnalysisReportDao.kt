package com.example.sleepcare.data.local.dao

import androidx.room.*
import com.example.sleepcare.data.local.entity.AnalysisReportEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface AnalysisReportDao {
    @Query("SELECT * FROM analysis_reports WHERE sessionId = :sessionId")
    suspend fun getReportForSession(sessionId: Long): AnalysisReportEntity?

    @Query("SELECT * FROM analysis_reports WHERE sessionId IN (:sessionIds)")
    suspend fun getReportsForSessions(sessionIds: List<Long>): List<AnalysisReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: AnalysisReportEntity)

    @Update
    suspend fun updateReport(report: AnalysisReportEntity)

    @Delete
    suspend fun deleteReport(report: AnalysisReportEntity)

    @Query("DELETE FROM analysis_reports WHERE sessionId = :sessionId")
    suspend fun deleteReportForSession(sessionId: Long)

    @Query(
        """
        SELECT AVG(sleepEfficiency) 
        FROM analysis_reports 
        WHERE createdAt >= :startDate
    """
    )
    suspend fun getAverageSleepEfficiencySince(startDate: Date): Float?

    @Query(
        """
        SELECT COUNT(*) 
        FROM analysis_reports 
        WHERE createdAt >= :startDate 
        AND sleepScore >= :minScore
    """
    )
    suspend fun countGoodSleepsSince(startDate: Date, minScore: Int = 80): Int

    @Query(
        """
        SELECT COUNT(*) 
        FROM analysis_reports 
        WHERE createdAt >= :startDate 
        AND sleepScore < :maxScore
    """
    )
    suspend fun countPoorSleepsSince(startDate: Date, maxScore: Int = 60): Int

    @Query(
        """
        SELECT AVG(sleepLatency) 
        FROM analysis_reports 
        WHERE createdAt >= :startDate
    """
    )
    suspend fun getAverageSleepLatencySince(startDate: Date): Float?

    @Query(
        """
        SELECT AVG(waso) 
        FROM analysis_reports 
        WHERE createdAt >= :startDate
    """
    )
    suspend fun getAverageWASOSince(startDate: Date): Float?
}