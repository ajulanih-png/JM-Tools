package com.JM.Optimizer

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

class JMApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                File(filesDir, "crash.txt").writeText(android.util.Log.getStackTraceString(throwable))
            } catch (_: Exception) {
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}

object CrashLogger {
    fun read(context: Context): String? = try {
        val f = File(context.filesDir, "crash.txt")
        if (f.exists()) f.readText().take(6000) else null
    } catch (_: Exception) {
        null
    }

    fun clear(context: Context) {
        try {
            File(context.filesDir, "crash.txt").delete()
        } catch (_: Exception) {
        }
    }
}

@Composable
fun CrashScreen(trace: String, onContinue: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Aplikasi sempat crash", color = Color(0xFFFF2A4D), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Tekan dan tahan teks di bawah untuk menyalin, atau screenshot, lalu kirim ke Claude.", color = Color.White, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        SelectionContainer {
            Text(trace, color = Color(0xFFCCCCCC), fontSize = 10.sp)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onContinue) { Text("Lanjut ke aplikasi") }
    }
}
