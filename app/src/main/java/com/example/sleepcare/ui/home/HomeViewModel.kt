package com.example.sleepcare.ui.home

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepcare.service.SleepMonitoringService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _isMonitoring = MutableLiveData<Boolean>()
    val isMonitoring: LiveData<Boolean> = _isMonitoring

    init {
        // Observe the isMonitoring StateFlow from the service
        viewModelScope.launch {
            SleepMonitoringService.isMonitoring.collect {
                _isMonitoring.postValue(it)
            }
        }
    }

    fun toggleMonitoring() {
        val context = getApplication<Application>()
        val intent = Intent(context, SleepMonitoringService::class.java)
        
        try {
            if (_isMonitoring.value == true) {
                // Stop monitoring
                intent.action = SleepMonitoringService.ACTION_STOP_MONITORING
                Log.d(TAG, "Attempting to stop monitoring service...")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "Stop monitoring service request sent.")
            } else {
                // Start monitoring
                intent.action = SleepMonitoringService.ACTION_START_MONITORING
                Log.d(TAG, "Attempting to start monitoring service...")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "Start monitoring service request sent.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling monitoring service: ${e.message}", e)
            // Update UI to reflect the error state
            _isMonitoring.postValue(false)
            
            // Show error to user (you might want to use a LiveData or event to show this in the UI)
            Toast.makeText(
                context,
                "Error: ${e.message ?: "Failed to toggle monitoring"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    companion object {
        private const val TAG = "HomeViewModel"
    }
}