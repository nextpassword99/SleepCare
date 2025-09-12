package com.example.sleepcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "sensor_data",
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SensorDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Date,
    val soundLevel: Float? = null,
    val movementX: Float? = null,
    val movementY: Float? = null,
    val movementZ: Float? = null,
    val lightLevel: Float? = null,
    val temperature: Float? = null,
    val heartRate: Int? = null,
    val spo2: Int? = null,
    val isProcessed: Boolean = false
)
