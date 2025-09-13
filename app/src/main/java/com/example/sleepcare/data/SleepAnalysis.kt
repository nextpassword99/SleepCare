package com.example.sleepcare.data

import java.util.Date

/**
 * Clase que contiene el análisis de una sesión de sueño.
 * @property sessionId ID de la sesión de sueño analizada.
 * @property timestamp Marca de tiempo del análisis.
 * @property totalSleepTime Tiempo total de sueño en minutos.
 * @property deepSleepTime Tiempo de sueño profundo en minutos.
 * @property lightSleepTime Tiempo de sueño ligero en minutos.
 * @property remSleepTime Tiempo de sueño REM en minutos.
 * @property awakeTime Tiempo despierto en minutos.
 * @property sleepScore Puntuación de calidad del sueño (0-100).
 * @property snoringEvents Número de eventos de ronquidos detectados.
 * @property movementEvents Número de eventos de movimiento detectados.
 * @property averageHeartRate Frecuencia cardíaca promedio durante el sueño.
 * @property sleepStages Mapa de las etapas del sueño con su duración en minutos.
 */
data class SleepAnalysis(
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val totalSleepTime: Int = 0,
    val deepSleepTime: Int = 0,
    val lightSleepTime: Int = 0,
    val remSleepTime: Int = 0,
    val awakeTime: Int = 0,
    val sleepScore: Int = 0,
    val snoringEvents: Int = 0,
    val movementEvents: Int = 0,
    val averageHeartRate: Float = 0f,
    val sleepStages: Map<SleepStage, Int> = emptyMap()
) {
    

    /**
     * Calcula el porcentaje de tiempo pasado en una etapa específica del sueño.
     * @param stage La etapa del sueño a calcular.
     * @return El porcentaje de tiempo (0-100) pasado en la etapa especificada.
     */
    fun getSleepStagePercentage(stage: SleepStage): Float {
        if (totalSleepTime == 0) return 0f
        
        return when (stage) {
            SleepStage.AWAKE -> (awakeTime * 100f) / totalSleepTime
            SleepStage.LIGHT -> (lightSleepTime * 100f) / totalSleepTime
            SleepStage.DEEP -> (deepSleepTime * 100f) / totalSleepTime
            SleepStage.REM -> (remSleepTime * 100f) / totalSleepTime
        }
    }

    /**
     * Obtiene un resumen del análisis en formato de texto.
     */
    fun getSummary(): String {
        return """
            Puntuación del sueño: $sleepScore/100
            Tiempo total de sueño: ${totalSleepTime / 60}h ${totalSleepTime % 60}m
            Sueño profundo: ${getSleepStagePercentage(SleepStage.DEEP).toInt()}%
            Sueño ligero: ${getSleepStagePercentage(SleepStage.LIGHT).toInt()}%
            Sueño REM: ${getSleepStagePercentage(SleepStage.REM).toInt()}%
            Tiempo despierto: $awakeTime m
            Ronquidos: $snoringEvents eventos
            Movimientos: $movementEvents eventos
        """.trimIndent()
    }
}

/**
 * Clase que contiene los parámetros de configuración para el análisis del sueño.
 */
data class SleepAnalysisConfig(
    val snoringThreshold: Float = 60f, // Nivel de sonido en dB para detectar ronquidos
    val movementThreshold: Float = 2.5f, // Umbral de aceleración para detectar movimiento
    val lightThreshold: Float = 5f, // Umbral de luz para considerar oscuridad
    val minSleepDuration: Int = 30, // Duración mínima en minutos para considerar una sesión de sueño
    val analysisInterval: Long = 5 * 60 * 1000 // Intervalo de análisis en milisegundos (5 minutos)
)
