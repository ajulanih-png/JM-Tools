// util/ShellExecutor.kt
package com.JM.Optimizer.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Shell Executor - Multi-method (Root / Shizuku / Direct)
 */
object ShellExecutor {
    
    enum class ExecMethod {
        ROOT,
        SHIZUKU,
        DIRECT
    }
    
    var currentMethod: ExecMethod = ExecMethod.DIRECT
        private set
    
    /**
     * Initialize best method available
     */
    fun init(context: Context) {
        currentMethod = when {
            RootChecker.isRooted() -> ExecMethod.ROOT
            ShizukuHelper.isAvailable() -> ExecMethod.SHIZUKU
            else -> ExecMethod.DIRECT
        }
    }
    
    /**
     * Execute shell command
     */
    suspend fun exec(command: String): ShellResult = withContext(Dispatchers.IO) {
        when (currentMethod) {
            ExecMethod.ROOT -> execWithRoot(command)
            ExecMethod.SHIZUKU -> execWithShizuku(command)
            ExecMethod.DIRECT -> execDirect(command)
        }
    }
    
    /**
     * Execute with root
     */
    private fun execWithRoot(command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            
            outputStream.writeBytes("$command\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            process.waitFor()
            
            ShellResult(
                success = process.exitValue() == 0,
                output = output.toString().trim(),
                error = null
            )
        } catch (e: Exception) {
            ShellResult(false, "", e.message)
        }
    }
    
    /**
     * Execute with Shizuku
     */
    private suspend fun execWithShizuku(command: String): ShellResult {
        return try {
            val result = ShizukuHelper.exec(command)
            ShellResult(result.success, result.output, result.error)
        } catch (e: Exception) {
            ShellResult(false, "", e.message)
        }
    }
    
    /**
     * Execute direct (no root, limited)
     */
    private fun execDirect(command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            process.waitFor()
            
            ShellResult(
                success = process.exitValue() == 0,
                output = output.toString().trim(),
                error = null
            )
        } catch (e: Exception) {
            ShellResult(false, "", e.message)
        }
    }
    
    /**
     * Set system property
     */
    suspend fun setProp(key: String, value: String): ShellResult {
        val command = when (currentMethod) {
            ExecMethod.ROOT -> "setprop $key $value"
            ExecMethod.SHIZUKU -> "setprop $key $value"
            ExecMethod.DIRECT -> return ShellResult(
                false, "", "Need root or Shizuku"
            )
        }
        return exec(command)
    }
    
    /**
     * Put system setting
     */
    suspend fun putSetting(type: String, key: String, value: String): ShellResult {
        return exec("settings put $type $key $value")
    }
    
    /**
     * Get system setting
     */
    suspend fun getSetting(type: String, key: String): String {
        val result = exec("settings get $type $key")
        return if (result.success) result.output else ""
    }
    
    /**
     * Change DPI
     */
    suspend fun setDensity(density: Int): ShellResult {
        return exec("wm density $density")
    }
    
    /**
     * Reset DPI
     */
    suspend fun resetDensity(): ShellResult {
        return exec("wm density reset")
    }
    
    /**
     * Change resolution
     */
    suspend fun setSize(width: Int, height: Int): ShellResult {
        return exec("wm size ${width}x${height}")
    }
    
    /**
     * Reset resolution
     */
    suspend fun resetSize(): ShellResult {
        return exec("wm size reset")
    }
    
    /**
     * Force stop app
     */
    suspend fun forceStop(packageName: String): ShellResult {
        return exec("am force-stop $packageName")
    }
    
    /**
     * Clear app cache
     */
    suspend fun clearCache(packageName: String): ShellResult {
        return exec("pm clear $packageName")
    }
    
    /**
     * Drop system cache
     */
    suspend fun dropCaches(): ShellResult {
        return exec("sync; echo 3 > /proc/sys/vm/drop_caches")
    }
    
    /**
     * Kill background apps
     */
    suspend fun killBackgroundApps(): ShellResult {
        return exec("am kill-all")
    }
    
    /**
     * Trim app caches
     */
    suspend fun trimCaches(): ShellResult {
        return exec("pm trim-caches 999G")
    }
    
    /**
     * Fstrim all partitions
     */
    suspend fun fstrim(): ShellResult {
        val partitions = listOf("system", "vendor", "data", "cache", "metadata", "odm", "system_ext", "product")
        val commands = partitions.joinToString("; ") { "sm fstrim /$it" }
        return exec(commands)
    }
}

data class ShellResult(
    val success: Boolean,
    val output: String,
    val error: String?
)