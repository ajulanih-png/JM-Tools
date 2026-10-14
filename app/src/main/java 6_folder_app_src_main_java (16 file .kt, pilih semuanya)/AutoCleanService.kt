// service/AutoCleanService.kt
package com.JM.Optimizer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.JM.Optimizer.R
import com.JM.Optimizer.util.ShellExecutor
import kotlinx.coroutines.*

/**
 * Auto Clean Service - Clear cache when game launched
 */
class AutoCleanService : Service() {
    
    companion object {
        const val CHANNEL_ID = "auto_clean_channel"
        const val NOTIFICATION_ID = 1002
        
        const val ACTION_START = "START_AUTO_CLEAN"
        const val ACTION_STOP = "STOP_AUTO_CLEAN"
        
        var isRunning = false
            private set
    }
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitorJob: Job? = null
    private var lastCleanedPackage: String? = null
    
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
        
        startForeground(NOTIFICATION_ID, createNotification("Auto Clean Active"))
        
        monitorJob = scope.launch {
            while (isActive) {
                checkAndClean()
                delay(3000) // Check every 3 seconds
            }
        }
    }
    
    private fun stopMonitoring() {
        isRunning = false
        monitorJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private suspend fun checkAndClean() {
        try {
            val result = ShellExecutor.exec(
                "dumpsys activity activities | grep mResumedActivity"
            )
            
            if (!result.success) return
            
            val line = result.output
            val currentPackage = extractPackageName(line) ?: return
            
            // Check if this is a registered game
            val prefs = getSharedPreferences("smart_dpi", Context.MODE_PRIVATE)
            if (!prefs.contains(currentPackage)) return
            
            // Skip if we already cleaned this game
            if (currentPackage == lastCleanedPackage) return
            
            // Trigger clean
            notify("Cleaning cache for $currentPackage...")
            performClean()
            lastCleanedPackage = currentPackage
            
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private suspend fun performClean() {
        // 1. Kill background apps
        ShellExecutor.killBackgroundApps()
        
        // 2. Trim caches
        ShellExecutor.trimCaches()
        
        // 3. Drop caches
        ShellExecutor.dropCaches()
        
        // 4. Fstrim
        ShellExecutor.fstrim()
        
        notify("Cache cleaned!")
    }
    
    private fun extractPackageName(line: String): String? {
        return try {
            val regex = Regex("""([a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)+)/""")
            regex.find(line)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Auto Clean Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Auto clean cache when game launches"
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JM Game Optimizer")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun notify(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(content))
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        isRunning = false
    }
}