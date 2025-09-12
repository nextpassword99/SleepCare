package com.example.sleepcare.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    private object PreferencesKeys {
        val ENABLE_SOUND_MONITORING = booleanPreferencesKey("enable_sound_monitoring")
        val ENABLE_MOVEMENT_MONITORING = booleanPreferencesKey("enable_movement_monitoring")
        val ENABLE_LIGHT_MONITORING = booleanPreferencesKey("enable_light_monitoring")
        val ENABLE_TEMPERATURE_MONITORING = booleanPreferencesKey("enable_temperature_monitoring")

        val SOUND_THRESHOLD = floatPreferencesKey("sound_threshold")
        val MOVEMENT_THRESHOLD = floatPreferencesKey("movement_threshold")
        val LIGHT_THRESHOLD = floatPreferencesKey("light_threshold")
        val TEMPERATURE_THRESHOLD_HIGH = floatPreferencesKey("temperature_threshold_high")
        val TEMPERATURE_THRESHOLD_LOW = floatPreferencesKey("temperature_threshold_low")

        val ENABLE_SNORE_ALERTS = booleanPreferencesKey("enable_snore_alerts")
        val ENABLE_MOVEMENT_ALERTS = booleanPreferencesKey("enable_movement_alerts")
        val ENABLE_SLEEP_REMINDERS = booleanPreferencesKey("enable_sleep_reminders")
        val ENABLE_WAKE_UP_ALERTS = booleanPreferencesKey("enable_wake_up_alerts")

        val BEDTIME_REMINDER_TIME = longPreferencesKey("bedtime_reminder_time")
        val WAKE_UP_TIME = longPreferencesKey("wake_up_time")

        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit") // "C" o "F"

        val THEME = stringPreferencesKey("theme") // "light", "dark", "system"
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    companion object {
        const val DEFAULT_ENABLE_SOUND_MONITORING = true
        const val DEFAULT_ENABLE_MOVEMENT_MONITORING = true
        const val DEFAULT_ENABLE_LIGHT_MONITORING = true
        const val DEFAULT_ENABLE_TEMPERATURE_MONITORING = false

        const val DEFAULT_SOUND_THRESHOLD = 60.0f // dB
        const val DEFAULT_MOVEMENT_THRESHOLD = 1.5f // m/s²
        const val DEFAULT_LIGHT_THRESHOLD = 100.0f // lux
        const val DEFAULT_TEMPERATURE_THRESHOLD_HIGH = 25.0f // °C
        const val DEFAULT_TEMPERATURE_THRESHOLD_LOW = 18.0f // °C

        const val DEFAULT_ENABLE_SNORE_ALERTS = true
        const val DEFAULT_ENABLE_MOVEMENT_ALERTS = true
        const val DEFAULT_ENABLE_SLEEP_REMINDERS = true
        const val DEFAULT_ENABLE_WAKE_UP_ALERTS = true

        val DEFAULT_BEDTIME_REMINDER_TIME = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val DEFAULT_WAKE_UP_TIME = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // Mañana a las 6:00
        }.timeInMillis

        const val DEFAULT_TEMPERATURE_UNIT = "C"
        const val DEFAULT_THEME = "system"
        const val DEFAULT_FIRST_LAUNCH = true
        const val DEFAULT_LAST_SYNC_TIME = 0L
    }

    val enableSoundMonitoring: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_SOUND_MONITORING] ?: DEFAULT_ENABLE_SOUND_MONITORING
        }

    val enableMovementMonitoring: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_MOVEMENT_MONITORING]
                ?: DEFAULT_ENABLE_MOVEMENT_MONITORING
        }

    val enableLightMonitoring: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_LIGHT_MONITORING] ?: DEFAULT_ENABLE_LIGHT_MONITORING
        }

    val enableTemperatureMonitoring: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_TEMPERATURE_MONITORING]
                ?: DEFAULT_ENABLE_TEMPERATURE_MONITORING
        }

    val soundThreshold: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SOUND_THRESHOLD] ?: DEFAULT_SOUND_THRESHOLD
        }

    val movementThreshold: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.MOVEMENT_THRESHOLD] ?: DEFAULT_MOVEMENT_THRESHOLD
        }

    val lightThreshold: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LIGHT_THRESHOLD] ?: DEFAULT_LIGHT_THRESHOLD
        }

    val temperatureThresholdHigh: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_THRESHOLD_HIGH]
                ?: DEFAULT_TEMPERATURE_THRESHOLD_HIGH
        }

    val temperatureThresholdLow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_THRESHOLD_LOW]
                ?: DEFAULT_TEMPERATURE_THRESHOLD_LOW
        }

    val enableSnoreAlerts: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_SNORE_ALERTS] ?: DEFAULT_ENABLE_SNORE_ALERTS
        }

    val enableMovementAlerts: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_MOVEMENT_ALERTS] ?: DEFAULT_ENABLE_MOVEMENT_ALERTS
        }

    val enableSleepReminders: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_SLEEP_REMINDERS] ?: DEFAULT_ENABLE_SLEEP_REMINDERS
        }

    val enableWakeUpAlerts: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_WAKE_UP_ALERTS] ?: DEFAULT_ENABLE_WAKE_UP_ALERTS
        }

    val bedtimeReminderTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BEDTIME_REMINDER_TIME] ?: DEFAULT_BEDTIME_REMINDER_TIME
        }

    val wakeUpTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.WAKE_UP_TIME] ?: DEFAULT_WAKE_UP_TIME
        }

    val temperatureUnit: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_UNIT] ?: DEFAULT_TEMPERATURE_UNIT
        }

    val theme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME] ?: DEFAULT_THEME
        }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] ?: DEFAULT_FIRST_LAUNCH
        }

    val lastSyncTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] ?: DEFAULT_LAST_SYNC_TIME
        }

    suspend fun setEnableSoundMonitoring(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_SOUND_MONITORING] = enable
        }
    }

    suspend fun setEnableMovementMonitoring(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_MOVEMENT_MONITORING] = enable
        }
    }

    suspend fun setEnableLightMonitoring(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_LIGHT_MONITORING] = enable
        }
    }

    suspend fun setEnableTemperatureMonitoring(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_TEMPERATURE_MONITORING] = enable
        }
    }

    suspend fun setSoundThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_THRESHOLD] = threshold
        }
    }

    suspend fun setMovementThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOVEMENT_THRESHOLD] = threshold
        }
    }

    suspend fun setLightThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LIGHT_THRESHOLD] = threshold
        }
    }

    suspend fun setTemperatureThresholds(high: Float, low: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_THRESHOLD_HIGH] = high
            preferences[PreferencesKeys.TEMPERATURE_THRESHOLD_LOW] = low
        }
    }

    suspend fun setEnableSnoreAlerts(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_SNORE_ALERTS] = enable
        }
    }

    suspend fun setEnableMovementAlerts(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_MOVEMENT_ALERTS] = enable
        }
    }

    suspend fun setEnableSleepReminders(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_SLEEP_REMINDERS] = enable
        }
    }

    suspend fun setEnableWakeUpAlerts(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_WAKE_UP_ALERTS] = enable
        }
    }

    suspend fun setBedtimeReminderTime(timeInMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BEDTIME_REMINDER_TIME] = timeInMillis
        }
    }

    suspend fun setWakeUpTime(timeInMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WAKE_UP_TIME] = timeInMillis
        }
    }

    suspend fun setTemperatureUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_UNIT] = unit
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = isFirstLaunch
        }
    }

    suspend fun setLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = timestamp
        }
    }
}
