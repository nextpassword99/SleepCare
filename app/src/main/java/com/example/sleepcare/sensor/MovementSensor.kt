package com.example.sleepcare.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * Clase que monitorea el movimiento utilizando el acelerómetro del dispositivo.
 * Detecta movimientos bruscos basados en la aceleración lineal.
 */
class MovementSensor(private val context: Context) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var isMonitoring = false
    private var lastMovement = 0f
    private val gravity = FloatArray(3) { 0f }
    private val linearAcceleration = FloatArray(3) { 0f }
    
    /**
     * Inicia el monitoreo del movimiento.
     */
    fun start() {
        if (isMonitoring) return
        
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        if (accelerometer == null) {
            Log.e("MovementSensor", "No se encontró el acelerómetro en el dispositivo")
            return
        }
        
        // Registrar el listener con una tasa de actualización rápida
        val success = sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_FASTEST
        ) ?: false
        
        if (success) {
            isMonitoring = true
            Log.d("MovementSensor", "Monitoreo de movimiento iniciado")
        } else {
            Log.e("MovementSensor", "No se pudo registrar el listener del acelerómetro")
        }
    }
    
    /**
     * Detiene el monitoreo del movimiento.
     */
    fun stop() {
        if (!isMonitoring) return
        
        sensorManager?.unregisterListener(this)
        isMonitoring = false
        lastMovement = 0f
        
        // Limpiar los arrays
        for (i in 0..2) {
            gravity[i] = 0f
            linearAcceleration[i] = 0f
        }
        
        Log.d("MovementSensor", "Monitoreo de movimiento detenido")
    }
    
    /**
     * Obtiene el nivel actual de movimiento.
     * @return Magnitud del vector de aceleración lineal en m/s².
     */
    fun getCurrentMovement(): Float {
        return if (isMonitoring) {
            lastMovement
        } else {
            0f
        }
    }
    
    /**
     * Maneja los cambios en los valores del sensor.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        
        // Filtro de paso bajo para separar la gravedad
        val alpha = 0.8f
        
        // Aislar la fuerza de gravedad con el filtro de paso bajo
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
        
        // Restar la gravedad para obtener la aceleración lineal
        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]
        
        // Calcular la magnitud del vector de aceleración lineal
        lastMovement = sqrt(
            linearAcceleration[0] * linearAcceleration[0] +
            linearAcceleration[1] * linearAcceleration[1] +
            linearAcceleration[2] * linearAcceleration[2]
        )
    }
    
    /**
     * Maneja cambios en la precisión del sensor.
     */
    override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
        // No es necesario hacer nada aquí para esta aplicación
    }
    
    /**
     * Verifica si el sensor de movimiento está activo.
     */
    fun isMonitoring(): Boolean = isMonitoring
}
