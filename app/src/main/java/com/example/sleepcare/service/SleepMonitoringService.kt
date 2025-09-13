package com.example.sleepcare.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sleepcare.MainActivity
import com.example.sleepcare.R
import com.example.sleepcare.SleepCareApplication
import com.example.sleepcare.data.SleepStage
import com.example.sleepcare.data.local.entity.SensorDataEntity
import com.example.sleepcare.data.local.entity.SleepSessionEntity
import com.example.sleepcare.data.repository.SleepRepository
import com.example.sleepcare.sensor.LightSensor
import com.example.sleepcare.sensor.MovementSensor
import com.example.sleepcare.sensor.SoundMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class SleepMonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var monitoringJob: Job? = null

    private lateinit var repository: SleepRepository
    private lateinit var soundMonitor: SoundMonitor
    private lateinit var movementSensor: MovementSensor
    private lateinit var lightSensor: LightSensor

    private var currentSessionId: Long? = null

    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()
        try {
            repository = (application as SleepCareApplication).repository
            soundMonitor = SoundMonitor(this)
            movementSensor = MovementSensor(this)
            lightSensor = LightSensor(this)

            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SleepCare::WakeLock")

            createNotificationChannel()
            Log.d(TAG, "Service onCreate successful.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during service onCreate: ${e.message}", e)
            stopSelf() // Stop the service if initialization fails
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand received action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
            else -> Log.w(TAG, "Unknown action received: ${intent?.action}")
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (isMonitoring.value) return

        try {
            isMonitoring.value = true
            startForeground(NOTIFICATION_ID, createNotification(getString(R.string.notification_title))) // Use string resource
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

            soundMonitor.start()
            movementSensor.start()
            lightSensor.start()

            // Add checks to see if sensors actually started
            if (!soundMonitor.isMonitoring()) {
                Log.w(TAG, "Sound monitoring failed to start.")
                // Potentially inform the user or disable sound-related features
            }
            if (!movementSensor.isMonitoring()) {
                Log.w(TAG, "Movement monitoring failed to start.")
                // Potentially inform the user or disable movement-related features
            }
            if (!lightSensor.isMonitoring()) {
                Log.w(TAG, "Light monitoring failed to start.")
                // Potentially inform the user or disable light-related features
            }

            monitoringJob = serviceScope.launch {
                try {
                    val session = SleepSessionEntity(startTime = Date())
                    currentSessionId = repository.insertSession(session)
                    Log.d(TAG, "New sleep session started with ID: $currentSessionId")

                    while (isMonitoring.value) {
                        collectSensorData()
                        analyzeSleepStage()
                        delay(5000)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during monitoring job: ${e.message}", e)
                    stopMonitoring() // Stop monitoring on error
                }
            }
            Log.d(TAG, "Monitoring started.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting monitoring: ${e.message}", e)
            isMonitoring.value = false
            stopSelf() // Stop the service if starting fails
        }
    }

    private fun stopMonitoring() {
        if (!isMonitoring.value) return

        try {
            isMonitoring.value = false
            monitoringJob?.cancel()

            soundMonitor.stop()
            movementSensor.stop()
            lightSensor.stop()

            if (wakeLock.isHeld) {
                wakeLock.release()
            }

            serviceScope.launch {
                try {
                    currentSessionId?.let {
                        repository.getSessionById(it)?.let {
                            val updatedSession = it.copy(endTime = Date())
                            repository.updateSession(updatedSession)
                            Log.d(TAG, "Sleep session $it updated with end time.")
                        } ?: Log.w(TAG, "Session with ID $it not found for update.")
                    } ?: Log.w(TAG, "currentSessionId is null when trying to stop monitoring.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating session on stop: ${e.message}", e)
                }
            }

            stopForeground(true)
            stopSelf()
            Log.d(TAG, "Monitoring stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping monitoring: ${e.message}", e)
        }
    }

    private suspend fun collectSensorData() {
        try {
            val soundLevel = soundMonitor.getCurrentSoundLevel()
            val movementLevel = movementSensor.getCurrentMovement()
            val lightLevel = lightSensor.getCurrentLightLevel()

            val data = SensorDataEntity(
                sessionId = currentSessionId ?: 0,
                timestamp = Date(),
                soundLevel = soundLevel,
                movementX = movementLevel, // This should be separated into X, Y, Z
                lightLevel = lightLevel
            )
            currentData.value = data
            repository.insertSensorData(data)
            Log.d(TAG, "Sensor data collected: sound=$soundLevel, movement=$movementLevel, light=$lightLevel")
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting sensor data: ${e.message}", e)
        }
    }

    private suspend fun analyzeSleepStage() {
        try {
            val data = currentData.value ?: return
            val thresholds = try {
                repository.run {
                    object {
                        val movement = movementThreshold.first()
                        val sound = soundThreshold.first()
                        val light = lightThreshold.first()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving thresholds: ${e.message}", e)
                return // Exit if thresholds cannot be retrieved
            }

            val newStage = when {
                (data.movementX ?: 0f) > thresholds.movement -> SleepStage.AWAKE
                (data.soundLevel ?: 0f) > thresholds.sound -> SleepStage.REM
                (data.lightLevel ?: 0f) > thresholds.light -> SleepStage.LIGHT
                else -> SleepStage.DEEP
            }

            if (currentStage.value != newStage) {
                currentStage.value = newStage
                Log.d(TAG, "Sleep stage changed to: $newStage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing sleep stage: ${e.message}", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        Log.d(TAG, "Service onDestroy.")
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "SleepMonitoringChannel"
        private const val TAG = "SleepMonitoringService"

        const val ACTION_START_MONITORING = "com.example.sleepcare.service.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.example.sleepcare.service.STOP_MONITORING"

        val isMonitoring = MutableStateFlow(false)
        val currentData = MutableStateFlow<SensorDataEntity?>(null)
        val currentStage = MutableStateFlow(SleepStage.AWAKE)
    }
}