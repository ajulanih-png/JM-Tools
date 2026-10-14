// util/DeviceInfoProvider.kt
package com.JM.Optimizer.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.io.RandomAccessFile

data class DeviceInfo(
    val brand: String,
    val model: String,
    val device: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val cpu: String,
    val cpuCores: Int,
    val cpuArchitecture: String,
    val gpu: String,
    val totalRam: Long,
    val availableRam: Long,
    val usedRam: Long,
    val ramPercentage: Float,
    val totalStorage: Long,
    val availableStorage: Long,
    val usedStorage: Long,
    val storagePercentage: Float,
    val batteryLevel: Int,
    val batteryStatus: String,
    val batteryTemperature: Float,
    val batteryHealth: String,
    val uptime: Long
)

object DeviceInfoProvider {
    
    fun getDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo(
            brand = Build.BRAND,
            model = Build.MODEL,
            device = Build.DEVICE,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            cpu = getCPUInfo(),
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuArchitecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            gpu = getGPUInfo(),
            totalRam = getTotalRam(context),
            availableRam = getAvailableRam(context),
            usedRam = getUsedRam(context),
            ramPercentage = getRamPercentage(context),
            totalStorage = getTotalStorage(),
            availableStorage = getAvailableStorage(),
            usedStorage = getUsedStorage(),
            storagePercentage = getStoragePercentage(),
            batteryLevel = getBatteryLevel(context),
            batteryStatus = getBatteryStatus(context),
            batteryTemperature = getBatteryTemperature(context),
            batteryHealth = getBatteryHealth(context),
            uptime = android.os.SystemClock.elapsedRealtime()
        )
    }
    
    private fun getCPUInfo(): String {
        return try {
            val file = File("/proc/cpuinfo")
            val lines = file.readLines()
            lines.firstOrNull { it.startsWith("Hardware") }?.substringAfter(":")?.trim()
                ?: lines.firstOrNull { it.startsWith("model name") }?.substringAfter(":")?.trim()
                ?: Build.HARDWARE
        } catch (e: Exception) {
            Build.HARDWARE
        }
    }
    
    private fun getGPUInfo(): String {
        return try {
            val file = File("/sys/class/kgsl/kgsl-3d0/gpu_model")
            if (file.exists()) file.readText().trim() else Build.HARDWARE
        } catch (e: Exception) {
            Build.HARDWARE
        }
    }
    
    private fun getTotalRam(context: Context): Long {
        val mi = ActivityManager.MemoryInfo()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.getMemoryInfo(mi)
        return mi.totalMem
    }
    
    private fun getAvailableRam(context: Context): Long {
        val mi = ActivityManager.MemoryInfo()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.getMemoryInfo(mi)
        return mi.availMem
    }
    
    private fun getUsedRam(context: Context): Long {
        return getTotalRam(context) - getAvailableRam(context)
    }
    
    private fun getRamPercentage(context: Context): Float {
        val total = getTotalRam(context).toFloat()
        val used = getUsedRam(context).toFloat()
        return (used / total) * 100f
    }
    
    private fun getTotalStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        return stat.blockCountLong * stat.blockSizeLong
    }
    
    private fun getAvailableStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }
    
    private fun getUsedStorage(): Long {
        return getTotalStorage() - getAvailableStorage()
    }
    
    private fun getStoragePercentage(): Float {
        val total = getTotalStorage().toFloat()
        val used = getUsedStorage().toFloat()
        return (used / total) * 100f
    }
    
    private fun getBatteryLevel(context: Context): Int {
        return try {
            val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) (level * 100 / scale) else 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getBatteryStatus(context: Context): String {
        return try {
            val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            when (intent?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)) {
                android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
                android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getBatteryTemperature(context: Context): Float {
        return try {
            val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val temp = intent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            temp / 10f
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun getBatteryHealth(context: Context): String {
        return try {
            val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            when (intent?.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1)) {
                android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                android.os.BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}