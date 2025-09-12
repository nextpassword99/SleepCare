package com.example.sleepcare.sensor

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.IOException

/**
 * Clase que monitorea el nivel de sonido ambiental utilizando el micrófono del dispositivo.
 * Utiliza MediaRecorder para medir la amplitud del sonido en decibelios.
 */
class SoundMonitor(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var isMonitoring = false
    private var lastAmplitude = 0f
    
    /**
     * Inicia el monitoreo del sonido.
     */
    fun start() {
        if (isMonitoring) return
        
        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null")
                
                try {
                    prepare()
                    start()
                    isMonitoring = true
                    Log.d("SoundMonitor", "Monitoreo de sonido iniciado")
                } catch (e: IOException) {
                    Log.e("SoundMonitor", "Error al iniciar el grabador de audio", e)
                    release()
                } catch (e: IllegalStateException) {
                    Log.e("SoundMonitor", "Error de estado ilegal al iniciar el grabador", e)
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e("SoundMonitor", "Error al configurar el grabador de audio", e)
            release()
        }
    }
    
    /**
     * Detiene el monitoreo del sonido y libera los recursos.
     */
    fun stop() {
        if (!isMonitoring) return
        
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: IllegalStateException) {
                    Log.e("SoundMonitor", "Error al detener el grabador de audio", e)
                }
                release()
            }
        } finally {
            mediaRecorder = null
            isMonitoring = false
            Log.d("SoundMonitor", "Monitoreo de sonido detenido")
        }
    }
    
    /**
     * Obtiene el nivel actual de sonido en decibelios.
     * @return Nivel de sonido en dB, o 0f si no se está monitoreando.
     */
    fun getCurrentSoundLevel(): Float {
        return if (isMonitoring && mediaRecorder != null) {
            try {
                // La amplitud máxima es 32767 (para 16 bits)
                val amplitude = mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
                // Convertir a dB (rango aproximado: 0-90 dB)
                lastAmplitude = 20 * kotlin.math.log10(amplitude / 32767f + 1e-6f).toFloat() + 90f
                lastAmplitude = lastAmplitude.coerceIn(0f, 90f)
            } catch (e: Exception) {
                Log.e("SoundMonitor", "Error al obtener el nivel de sonido", e)
                lastAmplitude = 0f
            }
            lastAmplitude
        } else {
            0f
        }
    }
    
    /**
     * Libera los recursos utilizados por el monitor de sonido.
     */
    private fun release() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("SoundMonitor", "Error al liberar recursos", e)
        } finally {
            mediaRecorder = null
            isMonitoring = false
        }
    }
    
    /**
     * Verifica si el monitor de sonido está activo.
     */
    fun isMonitoring(): Boolean = isMonitoring
}
