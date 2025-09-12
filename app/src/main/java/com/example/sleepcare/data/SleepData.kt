package com.example.sleepcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Clase que representa los datos en tiempo real recopilados de los sensores durante una sesión de sueño.
 * @property id Identificador único generado automáticamente.
 * @property sessionId ID de la sesión de sueño a la que pertenecen estos datos.
 * @property timestamp Marca de tiempo de cuando se recopilaron los datos.
 * @property soundLevel Nivel de sonido en decibelios.
 * @property movementLevel Nivel de movimiento detectado por el acelerómetro.
 * @property lightLevel Nivel de luz ambiental.
 */
@Entity(tableName = "sleep_data")
data class SleepData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val soundLevel: Float = 0f,
    val movementLevel: Float = 0f,
    val lightLevel: Float = 0f
) {
    /**
     * Crea una copia de los datos de sueño con la marca de tiempo actual.
     */
    fun withCurrentTimestamp(): SleepData {
        return this.copy(timestamp = System.currentTimeMillis())
    }
}

/**
 * Clase que representa un valor de sensor con su marca de tiempo.
 */
data class SensorValue(
    val value: Float,
    val timestamp: Long = System.currentTimeMillis()
)
