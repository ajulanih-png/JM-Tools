// util/ShizukuHelper.kt
package com.JM.Optimizer.util

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuHelper {
    
    private const val PERMISSION_REQUEST_CODE = 1001
    
    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && 
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
    
    fun requestPermission() {
        try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
            } else {
                Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    suspend fun exec(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            // Shizuku.newProcess is private since API 13 but still works via reflection
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            val exitCode = process.waitFor()
            
            ShellResult(
                success = exitCode == 0,
                output = output.toString().trim(),
                error = null
            )
        } catch (e: Exception) {
            ShellResult(false, "", e.message)
        }
    }
}