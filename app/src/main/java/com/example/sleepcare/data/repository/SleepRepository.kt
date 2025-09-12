package com.example.sleepcare.data.repository

import android.content.Context
import com.example.sleepcare.data.local.AppDatabase
import com.example.sleepcare.data.local.entity.AnalysisReportEntity
import com.example.sleepcare.data.local.entity.SensorDataEntity
import com.example.sleepcare.data.local.entity.SleepSessionEntity
import com.example.sleepcare.data.local.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.util.*

class SleepRepository private constructor(
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) {
    val enableSoundMonitoring = userPreferences.enableSoundMonitoring
    val enableMovementMonitoring = userPreferences.enableMovementMonitoring
    val enableLightMonitoring = userPreferences.enableLightMonitoring
    val enableTemperatureMonitoring = userPreferences.enableTemperatureMonitoring
    val soundThreshold = userPreferences.soundThreshold
    val movementThreshold = userPreferences.movementThreshold
    val lightThreshold = userPreferences.lightThreshold
    val temperatureThresholdHigh = userPreferences.temperatureThresholdHigh
    val temperatureThresholdLow = userPreferences.temperatureThresholdLow
    val enableSnoreAlerts = userPreferences.enableSnoreAlerts
    val enableMovementAlerts = userPreferences.enableMovementAlerts
    val enableSleepReminders = userPreferences.enableSleepReminders
    val enableWakeUpAlerts = userPreferences.enableWakeUpAlerts
    val bedtimeReminderTime = userPreferences.bedtimeReminderTime
    val wakeUpTime = userPreferences.wakeUpTime
    val temperatureUnit = userPreferences.temperatureUnit
    val theme = userPreferences.theme
    val isFirstLaunch = userPreferences.isFirstLaunch
    val lastSyncTime = userPreferences.lastSyncTime

    suspend fun setEnableSoundMonitoring(enable: Boolean) =
        userPreferences.setEnableSoundMonitoring(enable)
    
    suspend fun setEnableMovementMonitoring(enable: Boolean) = 
        userPreferences.setEnableMovementMonitoring(enable)
    
    suspend fun setEnableLightMonitoring(enable: Boolean) = 
        userPreferences.setEnableLightMonitoring(enable)
    
    suspend fun setEnableTemperatureMonitoring(enable: Boolean) = 
        userPreferences.setEnableTemperatureMonitoring(enable)
    
    suspend fun setSoundThreshold(threshold: Float) = 
        userPreferences.setSoundThreshold(threshold)
    
    suspend fun setMovementThreshold(threshold: Float) = 
        userPreferences.setMovementThreshold(threshold)
    
    suspend fun setLightThreshold(threshold: Float) = 
        userPreferences.setLightThreshold(threshold)
    
    suspend fun setTemperatureThresholds(high: Float, low: Float) = 
        userPreferences.setTemperatureThresholds(high, low)
    
    suspend fun setEnableSnoreAlerts(enable: Boolean) = 
        userPreferences.setEnableSnoreAlerts(enable)
    
    suspend fun setEnableMovementAlerts(enable: Boolean) = 
        userPreferences.setEnableMovementAlerts(enable)
    
    suspend fun setEnableSleepReminders(enable: Boolean) = 
        userPreferences.setEnableSleepReminders(enable)
    
    suspend fun setEnableWakeUpAlerts(enable: Boolean) = 
        userPreferences.setEnableWakeUpAlerts(enable)
    
    suspend fun setBedtimeReminderTime(timeInMillis: Long) = 
        userPreferences.setBedtimeReminderTime(timeInMillis)
    
    suspend fun setWakeUpTime(timeInMillis: Long) = 
        userPreferences.setWakeUpTime(timeInMillis)
    
    suspend fun setTemperatureUnit(unit: String) = 
        userPreferences.setTemperatureUnit(unit)
    
    suspend fun setTheme(theme: String) = 
        userPreferences.setTheme(theme)
    
    suspend fun setFirstLaunch(isFirstLaunch: Boolean) = 
        userPreferences.setFirstLaunch(isFirstLaunch)
    
    suspend fun setLastSyncTime(timestamp: Long) = 
        userPreferences.setLastSyncTime(timestamp)

    // Operaciones de sesión de sueño
    suspend fun insertSession(session: SleepSessionEntity): Long =
        database.sleepSessionDao().insertSession(session)
    
    suspend fun updateSession(session: SleepSessionEntity) =
        database.sleepSessionDao().updateSession(session)
    
    suspend fun deleteSession(session: SleepSessionEntity) =
        database.sleepSessionDao().deleteSession(session)
    
    suspend fun getSessionById(id: Long): SleepSessionEntity? =
        database.sleepSessionDao().getSessionById(id)
    
    fun getAllSessions(): Flow<List<SleepSessionEntity>> =
        database.sleepSessionDao().getAllSessions()
    
    fun getSessionsBetweenDates(startDate: Date, endDate: Date): Flow<List<SleepSessionEntity>> =
        database.sleepSessionDao().getSessionsBetweenDates(startDate, endDate)
    
    suspend fun getActiveSession(): SleepSessionEntity? =
        database.sleepSessionDao().getActiveSession()
    
    // Operaciones de datos de sensores
    suspend fun insertSensorData(data: SensorDataEntity): Long =
        database.sensorDataDao().insertSensorData(data)
    
    suspend fun insertMultipleSensorData(data: List<SensorDataEntity>) =
        database.sensorDataDao().insertMultipleSensorData(data)
    
    fun getSensorDataForSession(sessionId: Long): Flow<List<SensorDataEntity>> =
        database.sensorDataDao().getSensorDataForSession(sessionId)
    
    fun getSensorDataForSessionInTimeRange(
        sessionId: Long,
        startTime: Date,
        endTime: Date
    ): Flow<List<SensorDataEntity>> =
        database.sensorDataDao().getSensorDataForSessionInTimeRange(sessionId, startTime, endTime)
    
    // Operaciones de informes de análisis
    suspend fun insertReport(report: AnalysisReportEntity) =
        database.analysisReportDao().insertReport(report)
    
    suspend fun getReportForSession(sessionId: Long): AnalysisReportEntity? =
        database.analysisReportDao().getReportForSession(sessionId)
    
    suspend fun getReportsForSessions(sessionIds: List<Long>): List<AnalysisReportEntity> =
        database.analysisReportDao().getReportsForSessions(sessionIds)
    
    // Métodos de utilidad
    suspend fun getAverageSleepScoreSince(startDate: Date): Float? =
        database.sleepSessionDao().getAverageSleepScoreSince(startDate)
    
    suspend fun getAverageSleepDurationSince(startDate: Date): Float? =
        database.sleepSessionDao().getAverageSleepDurationSince(startDate)
    
    suspend fun getAverageSleepEfficiencySince(startDate: Date): Float? =
        database.analysisReportDao().getAverageSleepEfficiencySince(startDate)
    
    companion object {
        @Volatile
        private var INSTANCE: SleepRepository? = null
        
        fun getInstance(context: Context): SleepRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getDatabase(context)
                val userPreferences = UserPreferences(context)
                val instance = SleepRepository(database, userPreferences)
                INSTANCE = instance
                instance
            }
        }
    }
}
