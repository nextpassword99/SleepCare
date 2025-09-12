package com.example.sleepcare

import android.app.Application
import com.example.sleepcare.data.repository.SleepRepository

class SleepCareApplication : Application() {
    val repository: SleepRepository by lazy { SleepRepository.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
