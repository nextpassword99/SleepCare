package com.example.sleepcare.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sleepcare.MainActivity
import com.example.sleepcare.R
import com.example.sleepcare.data.SleepData
import com.example.sleepcare.data.SleepSession
import com.example.sleepcare.data.SleepStage
import com.example.sleepcare.sensor.LightSensor
import com.example.sleepcare.sensor.MovementSensor
import com.example.sleepcare.sensor.SoundMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.math.sqrt

/**
 * Servicio en primer plano que monitorea el sueño utilizando los sensores del dispositivo.
 * Recolecta datos de sonido, movimiento y luz para analizar la calidad del sueño.
 */
class SleepMonitoringService : Service() {
    // Constantes
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "SleepCareMonitoringChannel"
        private const val ACTION_START_MONITORING = "ACTION_START_MONITORING"
        private const val ACTION_STOP_MONITORING = "ACTION_STOP_MONITORING"
        
        // Umbrales
        private const val SNORING_THRESHOLD = 60f // dB
        private const val MOVEMENT_THRESHOLD = 2.5f // m/s²
        private const val LIGHT_THRESHOLD = 5f // lux
        
        // Estado del servicio
        private val _isMonitoring = MutableStateFlow(false)
        val isMonitoring: StateFlow<Boolean> = _isMonitoring
        
        // Últimos datos de sensores
        private val _currentData = MutableStateFlow(SleepData())
        val currentData: StateFlow<SleepData> = _currentData
        
        // Estado actual del sueño
        private val _currentStage = MutableStateFlow(SleepStage.AWAKE)
        val currentStage: StateFlow<SleepStage> = _currentStage
    }
    
    // Sensores
    private lateinit var soundMonitor: SoundMonitor
    private lateinit var movementSensor: MovementSensor
    private lateinit var lightSensor: LightSensor
    
    // Gestión de energía
    private lateinit var wakeLock: PowerManager.WakeLock
    private var monitoringJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    
    // Sesión actual
    private var currentSession: SleepSession? = null
    private val dataPoints = mutableListOf<SleepData>()
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        initializeSensors()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
        }
        return START_STICKY
    }
    
    /**
     * Inicializa los sensores necesarios para el monitoreo.
     */
    private fun initializeSensors() {
        soundMonitor = SoundMonitor(this)
        movementSensor = MovementSensor(this)
        lightSensor = LightSensor(this)
        
        // Inicializar wake lock para mantener el dispositivo despierto
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SleepCare::WakeLock"
        )
    }
    
    /**
     * Inicia el monitoreo del sueño.
     */
    private fun startMonitoring() {
        if (_isMonitoring.value) return
        
        _isMonitoring.value = true
        startForeground(NOTIFICATION_ID, createNotification("Monitoreo activo"))
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        
        // Iniciar sensores
        soundMonitor.start()
        movementSensor.start()
        lightSensor.start()
        
        // Crear nueva sesión de sueño
        currentSession = SleepSession()
        dataPoints.clear()
        
        // Iniciar recolección de datos
        monitoringJob = serviceScope.launch {
            while (_isMonitoring.value) {
                collectSensorData()
                analyzeSleepStage()
                delay(5000) // Recolectar datos cada 5 segundos
            }
        }
        
        Log.d("SleepMonitoring", "Monitoreo de sueño iniciado")
    }
    
    /**
     * Detiene el monitoreo del sueño.
     */
    private fun stopMonitoring() {
        if (!_isMonitoring.value) return
        
        _isMonitoring.value = false
        monitoringJob?.cancel()
        
        // Detener sensores
        soundMonitor.stop()
        movementSensor.stop()
        lightSensor.stop()
        
        // Liberar wake lock
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        
        // Finalizar sesión
        currentSession?.endSession()
        currentSession?.let { session ->
            // Aquí podrías guardar la sesión en la base de datos
            Log.d("SleepMonitoring", "Sesión finalizada: ${session.id}")
        }
        
        stopForeground(true)
        stopSelf()
        Log.d("SleepMonitoring", "Monitoreo de sueño detenido")
    }
    
    /**
     * Recolecta datos de todos los sensores.
     */
    private suspend fun collectSensorData() {
        val currentTime = System.currentTimeMillis()
        
        // Obtener datos de los sensores
        val soundLevel = soundMonitor.getCurrentSoundLevel()
        val movementLevel = movementSensor.getCurrentMovement()
        val lightLevel = lightSensor.getCurrentLightLevel()
        
        // Crear nuevo punto de datos
        val dataPoint = SleepData(
            sessionId = currentSession?.id ?: 0,
            timestamp = currentTime,
            soundLevel = soundLevel,
            movementLevel = movementLevel,
            lightLevel = lightLevel
        )
        
        // Actualizar estado actual
        _currentData.value = dataPoint
        dataPoints.add(dataPoint)
        
        Log.d("SensorData", "Sonido: ${"%.1f".format(soundLevel)} dB, " +
                "Movimiento: ${"%.2f".format(movementLevel)} m/s², " +
                "Luz: ${"%.1f".format(lightLevel)} lux")
    }
    
    /**
     * Analiza la etapa actual del sueño basándose en los datos de los sensores.
     */
    private fun analyzeSleepStage() {
        val data = _currentData.value
        
        // Lógica simple de análisis (puede mejorarse con algoritmos más avanzados)
        val newStage = when {
            data.movementLevel > MOVEMENT_THRESHOLD -> SleepStage.AWAKE
            data.soundLevel > SNORING_THRESHOLD -> SleepStage.REM
            data.lightLevel > LIGHT_THRESHOLD -> SleepStage.LIGHT
            else -> SleepStage.DEEP
        }
        
        if (_currentStage.value != newStage) {
            _currentStage.value = newStage
            Log.d("SleepStage", "Nueva etapa del sueño: $newStage")
        }
    }
    
    /**
     * Crea el canal de notificación para Android O y superiores.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Crea una notificación para el servicio en primer plano.
     */
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }
}

/**
 * Acción para iniciar el monitoreo del sueño.
 */
fun Context.startSleepMonitoring() {
    val intent = Intent(this, SleepMonitoringService::class.java).apply {
        action = SleepMonitoringService.ACTION_START_MONITORING
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

/**
 * Acción para detener el monitoreo del sueño.
 */
fun Context.stopSleepMonitoring() {
    val intent = Intent(this, SleepMonitoringService::class.java).apply {
        action = SleepMonitoringService.ACTION_STOP_MONITORING
    }
    stopService(intent)
}
