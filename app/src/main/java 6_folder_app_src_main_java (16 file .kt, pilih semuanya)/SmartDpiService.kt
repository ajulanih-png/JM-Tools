// service/SmartDpiService.kt
package com.JM.Optimizer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.JM.Optimizer.MainActivity
import com.JM.Optimizer.R
import com.JM.Optimizer.util.ShellExecutor
import kotlinx.coroutines.*

/**
 * Smart DPI Service - Auto change DPI when game launched
 */
class SmartDpiService : Service() {
    
    companion object {
        const val CHANNEL_ID = "smart_dpi_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "START_SMART_DPI"
        const val ACTION_STOP = "STOP_SMART_DPI"
        
        var isRunning = false
            private set
    }
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitorJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
        }
        return START_STICKY
    }
    
    private fun startMonitoring() {
        if (isRunning) return
        isRunning = true
        
        startForeground(NOTIFICATION_ID, createNotification("Smart DPI Active"))
        
        monitorJob = scope.launch {
            while (isActive) {
                checkForegroundApp()
                delay(2000) // Check every 2 seconds
            }
        }
    }
    
    private fun stopMonitoring() {
        isRunning = false
        monitorJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private suspend fun checkForegroundApp() {
        try {
            val result = ShellExecutor.exec(
                "dumpsys activity activities | grep mResumedActivity"
            )
            
            if (!result.success) return
            
            val line = result.output
            val currentPackage = extractPackageName(line) ?: return
            
            val prefs = getSharedPreferences("smart_dpi", Context.MODE_PRIVATE)
            val gameProfiles = prefs.all
            
            for ((pkg, dpi) in gameProfiles) {
                if (pkg == currentPackage && dpi is Int) {
                    applyDpi(dpi)
                    return
                }
            }
            
            // Not a game, reset DPI
            ShellExecutor.resetDensity()
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private fun extractPackageName(line: String): String? {
        return try {
            val regex = Regex("""([a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)+)/""")
            regex.find(line)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun applyDpi(dpi: Int) {
        ShellExecutor.setDensity(dpi)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Smart DPI Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Auto change DPI when game launches"
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JM Game Optimizer")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        isRunning = false
    }
}