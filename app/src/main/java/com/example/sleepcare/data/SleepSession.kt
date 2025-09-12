package com.example.sleepcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Representa una sesión de sueño del usuario.
 * @property id Identificador único generado automáticamente.
 * @property startTime Marca de tiempo de inicio de la sesión de sueño.
 * @property endTime Marca de tiempo de finalización de la sesión de sueño (puede ser nulo si la sesión está en curso).
 * @property qualityScore Puntuación de calidad del sueño (0-100).
 * @property notes Notas adicionales sobre la sesión de sueño.
 */
@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    var qualityScore: Int = 0,
    var notes: String = "",
    var isActive: Boolean = true
) {
    /**
     * Finaliza la sesión de sueño estableciendo la hora de finalización actual.
     */
    fun endSession() {
        endTime = System.currentTimeMillis()
        isActive = false
    }

    /**
     * Calcula la duración de la sesión de sueño en milisegundos.
     * @return Duración en milisegundos, o 0 si la sesión aún está en curso.
     */
    fun getDurationMs(): Long {
        return (endTime ?: return 0) - startTime
    }

    /**
     * Obtiene la duración formateada como una cadena (HH:MM).
     */
    fun getFormattedDuration(): String {
        val durationMs = getDurationMs()
        if (durationMs <= 0) return "00:00"
        
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60)) % 24
        
        return String.format("%02d:%02d", hours * 60 + minutes, seconds)
    }
}
