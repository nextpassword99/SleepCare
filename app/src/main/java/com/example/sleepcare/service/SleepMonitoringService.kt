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
        repository = (application as SleepCareApplication).repository
        soundMonitor = SoundMonitor(this)
        movementSensor = MovementSensor(this)
        lightSensor = LightSensor(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SleepCare::WakeLock")

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (isMonitoring.value) return

        isMonitoring.value = true
        startForeground(NOTIFICATION_ID, createNotification("Monitoring Sleep"))
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

        soundMonitor.start()
        movementSensor.start()
        lightSensor.start()

        monitoringJob = serviceScope.launch {
            val session = SleepSessionEntity(startTime = Date())
            currentSessionId = repository.insertSession(session)

            while (isMonitoring.value) {
                collectSensorData()
                analyzeSleepStage()
                delay(5000)
            }
        }
    }

    private fun stopMonitoring() {
        if (!isMonitoring.value) return

        isMonitoring.value = false
        monitoringJob?.cancel()

        soundMonitor.stop()
        movementSensor.stop()
        lightSensor.stop()

        if (wakeLock.isHeld) {
            wakeLock.release()
        }

        serviceScope.launch {
            currentSessionId?.let {
                repository.getSessionById(it)?.let {
                    val updatedSession = it.copy(endTime = Date())
                    repository.updateSession(updatedSession)
                }
            }
        }

        stopForeground(true)
        stopSelf()
    }

    private suspend fun collectSensorData() {
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
    }

    private suspend fun analyzeSleepStage() {
        val data = currentData.value ?: return
        val thresholds = repository.run {
            object {
                val movement = movementThreshold.first()
                val sound = soundThreshold.first()
                val light = lightThreshold.first()
            }
        }

        val newStage = when {
            (data.movementX ?: 0f) > thresholds.movement -> SleepStage.AWAKE
            (data.soundLevel ?: 0f) > thresholds.sound -> SleepStage.REM
            (data.lightLevel ?: 0f) > thresholds.light -> SleepStage.LIGHT
            else -> SleepStage.DEEP
        }

        if (currentStage.value != newStage) {
            currentStage.value = newStage
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
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "SleepMonitoringChannel"

        const val ACTION_START_MONITORING = "com.example.sleepcare.service.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.example.sleepcare.service.STOP_MONITORING"

        val isMonitoring = MutableStateFlow(false)
        val currentData = MutableStateFlow<SensorDataEntity?>(null)
        val currentStage = MutableStateFlow(SleepStage.AWAKE)
    }
}