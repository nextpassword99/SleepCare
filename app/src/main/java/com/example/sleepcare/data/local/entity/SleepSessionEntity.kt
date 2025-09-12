package com.example.sleepcare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Date,
    val endTime: Date? = null,
    val durationMinutes: Int = 0,
    val sleepScore: Int = 0,
    val deepSleepMinutes: Int = 0,
    val lightSleepMinutes: Int = 0,
    val remSleepMinutes: Int = 0,
    val awakeMinutes: Int = 0,
    val snoringEvents: Int = 0,
    val movementEvents: Int = 0,
    val notes: String? = null,
    val isSynced: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
