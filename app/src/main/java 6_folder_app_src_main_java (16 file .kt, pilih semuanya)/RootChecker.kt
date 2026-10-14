// util/RootChecker.kt
package com.JM.Optimizer.util

import java.io.File

object RootChecker {
    
    private val ROOT_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/magisk/.core/bin/su"
    )
    
    fun isRooted(): Boolean {
        return checkRootPaths() || checkSuBinary() || checkRootAccess()
    }
    
    private fun checkRootPaths(): Boolean {
        return ROOT_PATHS.any { File(it).exists() }
    }
    
    private fun checkSuBinary(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }
}