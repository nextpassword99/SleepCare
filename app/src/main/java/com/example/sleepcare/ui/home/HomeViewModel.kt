package com.example.sleepcare.ui.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepcare.service.SleepMonitoringService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.util.Log

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
        val intent = Intent(getApplication(), SleepMonitoringService::class.java)
        if (_isMonitoring.value == true) {
            intent.action = SleepMonitoringService.ACTION_STOP_MONITORING
            Log.d("HomeViewModel", "Attempting to stop monitoring service.")
            getApplication<Application>().startService(intent)
        } else {
            intent.action = SleepMonitoringService.ACTION_START_MONITORING
            Log.d("HomeViewModel", "Attempting to start monitoring service.")
            getApplication<Application>().startService(intent)
        }
    }
}