package com.JM.Optimizer.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.JM.Optimizer.ui.theme.*
import com.JM.Optimizer.util.DeviceInfo
import com.JM.Optimizer.util.DeviceInfoProvider
import com.JM.Optimizer.util.ShellExecutor
import kotlinx.coroutines.launch

// ui/screens/MouseSpeedScreen.kt
@Composable
fun MouseSpeedScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("JM", Context.MODE_PRIVATE)
    var speed by remember { mutableIntStateOf(prefs.getInt("mouse_speed", 0)) }
    
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = DarkCard)) {
            Column(Modifier.padding(16.dp)) {
                Text("MOUSE SPEED", fontWeight = FontWeight.Bold, color = Red)
                Text("Range: -7 to +7", color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text("Value: $speed", color = TextWhite, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Slider(
            value = speed.toFloat(),
            onValueChange = { speed = it.toInt().coerceIn(-7, 7) },
            valueRange = -7f..7f,
            steps = 13,
            colors = SliderDefaults.colors(thumbColor = Red, activeTrackColor = Red)
        )
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("-7 (Slow)", color = TextGray, style = MaterialTheme.typography.labelSmall)
            Text("0", color = TextGray, style = MaterialTheme.typography.labelSmall)
            Text("+7 (Fast)", color = TextGray, style = MaterialTheme.typography.labelSmall)
        }
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = {
                scope.launch {
                    prefs.edit().putInt("mouse_speed", speed).apply()
                    ShellExecutor.putSetting("system", "pointer_speed", speed.toString())
                    ShellExecutor.putSetting("system", "mouse_speed", speed.toString())
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("APPLY", fontWeight = FontWeight.Bold)
        }
    }
}

// ui/screens/PointerSpeedScreen.kt
@Composable
fun PointerSpeedScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("JM", Context.MODE_PRIVATE)
    var speed by remember { mutableIntStateOf(prefs.getInt("pointer_speed", 7)) }
    
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = DarkCard)) {
            Column(Modifier.padding(16.dp)) {
                Text("POINTER SPEED", fontWeight = FontWeight.Bold, color = Red)
                Text("Touch sensitivity (0-10)", color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text("Value: $speed", color = TextWhite, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Slider(
            value = speed.toFloat(),
            onValueChange = { speed = it.toInt().coerceIn(0, 10) },
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(thumbColor = Red, activeTrackColor = Red)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = {
                scope.launch {
                    prefs.edit().putInt("pointer_speed", speed).apply()
                    ShellExecutor.putSetting("system", "pointer_speed", speed.toString())
                    ShellExecutor.putSetting("system", "touch_size", "3")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("APPLY", fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(12.dp))
        
        Button(
            onClick = {
                scope.launch {
                    speed = 0
                    ShellExecutor.putSetting("system", "pointer_speed", "0")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBg2),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RESET", color = TextWhite)
        }
    }
}

// ui/screens/BloatCleanerScreen.kt
@Composable
fun BloatCleanerScreen() {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready") }
    
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = DarkCard)) {
            Column(Modifier.padding(16.dp)) {
                Text("BLOAT CLEANER", fontWeight = FontWeight.Bold, color = Red)
                Text("Auto clean cache on game launch", color = TextGray, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Status: $status", color = Green, style = MaterialTheme.typography.bodySmall)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = {
                scope.launch {
                    status = "Cleaning..."
                    ShellExecutor.killBackgroundApps()
                    ShellExecutor.trimCaches()
                    ShellExecutor.dropCaches()
                    ShellExecutor.fstrim()
                    status = "Cleaned!"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("MANUAL CLEAN NOW", fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(12.dp))
        
        Card(colors = CardDefaults.cardColors(containerColor = DarkCard)) {
            Column(Modifier.padding(16.dp)) {
                Text("What gets cleaned:", color = TextWhite, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                listOf(
                    "• Background apps (kill-all)",
                    "• App caches (pm trim-caches)",
                    "• System caches (drop_caches)",
                    "• Partition trim (fstrim)"
                ).forEach {
                    Text(it, color = TextGray, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ui/screens/DeviceInfoScreen.kt
@Composable
fun DeviceInfoScreen() {
    val context = LocalContext.current
    var info by remember { mutableStateOf<DeviceInfo?>(null) }
    
    LaunchedEffect(Unit) {
        info = DeviceInfoProvider.getDeviceInfo(context)
    }
    
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        info?.let { d ->
            InfoCard("DEVICE", listOf(
                "Brand" to d.brand,
                "Model" to d.model,
                "Device" to d.device,
                "Android" to "${d.androidVersion} (SDK ${d.sdkVersion})",
                "CPU" to d.cpu,
                "Cores" to d.cpuCores.toString(),
                "Arch" to d.cpuArchitecture,
                "GPU" to d.gpu
            ))
            
            Spacer(Modifier.height(12.dp))
            
            InfoCard("MEMORY", listOf(
                "Total RAM" to formatBytes(d.totalRam),
                "Available RAM" to formatBytes(d.availableRam),
                "Used RAM" to formatBytes(d.usedRam),
                "RAM Usage" to "${"%.1f".format(d.ramPercentage)}%"
            ))
            
            Spacer(Modifier.height(12.dp))
            
            InfoCard("STORAGE", listOf(
                "Total" to formatBytes(d.totalStorage),
                "Available" to formatBytes(d.availableStorage),
                "Used" to formatBytes(d.usedStorage),
                "Usage" to "${"%.1f".format(d.storagePercentage)}%"
            ))
            
            Spacer(Modifier.height(12.dp))
            
            InfoCard("BATTERY", listOf(
                "Level" to "${d.batteryLevel}%",
                "Status" to d.batteryStatus,
                "Temperature" to "${"%.1f".format(d.batteryTemperature)}°C",
                "Health" to d.batteryHealth
            ))
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Red)
        }
    }
}

@Composable
fun InfoCard(title: String, items: List<Pair<String, String>>) {
    Card(colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Red, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            items.forEach { (key, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(key, color = TextGray, modifier = Modifier.weight(1f))
                    Text(value, color = TextWhite, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "%.2f GB".format(gb)
        mb >= 1 -> "%.0f MB".format(mb)
        else -> "%.0f KB".format(kb)
    }
}