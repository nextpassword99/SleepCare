package com.example.sleepcare.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sleepcare.service.SleepMonitoringService
import com.example.sleepcare.service.SleepMonitoringService.Companion.ACTION_START_MONITORING

/**
 * Receptor que se activa cuando el dispositivo termina de arrancar.
 * Se utiliza para reiniciar el monitoreo del sueño si estaba activo antes del reinicio.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || 
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d("BootCompletedReceiver", "Dispositivo arrancado, verificando si hay que reiniciar el monitoreo")
            
            // Verificar en las preferencias si el monitoreo estaba activo
            // antes del reinicio y reactivarlo si es necesario
            val prefs = context.getSharedPreferences("sleep_monitoring_prefs", Context.MODE_PRIVATE)
            val wasMonitoring = prefs.getBoolean("is_monitoring", false)
            
            if (wasMonitoring) {
                Log.d("BootCompletedReceiver", "Reiniciando monitoreo de sueño...")
                val serviceIntent = Intent(context, SleepMonitoringService::class.java).apply {
                    action = ACTION_START_MONITORING
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
