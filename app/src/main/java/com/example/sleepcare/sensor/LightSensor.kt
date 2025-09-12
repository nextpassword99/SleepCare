package com.example.sleepcare.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Clase que monitorea el nivel de luz ambiental utilizando el sensor de luz del dispositivo.
 * Mide la iluminación en lux.
 */
class LightSensor(private val context: Context) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var isMonitoring = false
    private var lastLightLevel = 0f
    
    /**
     * Inicia el monitoreo de la luz ambiental.
     */
    fun start() {
        if (isMonitoring) return
        
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        
        if (lightSensor == null) {
            Log.e("LightSensor", "No se encontró el sensor de luz en el dispositivo")
            return
        }
        
        // Registrar el listener con una tasa de actualización normal
        val success = sensorManager?.registerListener(
            this,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        ) ?: false
        
        if (success) {
            isMonitoring = true
            Log.d("LightSensor", "Monitoreo de luz ambiental iniciado")
        } else {
            Log.e("LightSensor", "No se pudo registrar el listener del sensor de luz")
        }
    }
    
    /**
     * Detiene el monitoreo de la luz ambiental.
     */
    fun stop() {
        if (!isMonitoring) return
        
        sensorManager?.unregisterListener(this)
        isMonitoring = false
        lastLightLevel = 0f
        Log.d("LightSensor", "Monitoreo de luz ambiental detenido")
    }
    
    /**
     * Obtiene el nivel actual de luz ambiental.
     * @return Nivel de luz en lux.
     */
    fun getCurrentLightLevel(): Float {
        return if (isMonitoring) {
            lastLightLevel
        } else {
            0f
        }
    }
    
    /**
     * Maneja los cambios en los valores del sensor.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            // El valor del sensor de luz está en lux
            lastLightLevel = event.values[0]
            
            // Opcional: Registrar cambios significativos en el nivel de luz
            // Log.d("LightSensor", "Nivel de luz: ${lastLightLevel} lux")
        }
    }
    
    /**
     * Maneja cambios en la precisión del sensor.
     */
    override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
        // No es necesario hacer nada aquí para esta aplicación
    }
    
    /**
     * Verifica si el sensor de luz está activo.
     */
    fun isMonitoring(): Boolean = isMonitoring
    
    /**
     * Verifica si el entorno está lo suficientemente oscuro para considerarse apto para dormir.
     * @param threshold Umbral de luz en lux (por defecto 5 lux).
     * @return true si el entorno está oscuro, false en caso contrario.
     */
    fun isEnvironmentDark(threshold: Float = 5f): Boolean {
        return lastLightLevel <= threshold
    }
}
