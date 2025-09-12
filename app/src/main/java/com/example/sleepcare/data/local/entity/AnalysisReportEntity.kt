package com.example.sleepcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "analysis_reports",
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AnalysisReportEntity(
    @PrimaryKey
    val sessionId: Long,
    val sleepEfficiency: Float,
    val sleepLatency: Int,
    val remLatency: Int,
    val waso: Int,
    val sleepStages: Map<String, Int>,
    val sleepDisruptions: Int,
    val averageHeartRate: Float,
    val minHeartRate: Int,
    val maxHeartRate: Int,
    val averageSpO2: Float,
    val minSpO2: Int,
    val snoringEpisodes: Int,
    val movementEpisodes: Int,
    val sleepScore: Int,
    val recommendations: List<String>,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
