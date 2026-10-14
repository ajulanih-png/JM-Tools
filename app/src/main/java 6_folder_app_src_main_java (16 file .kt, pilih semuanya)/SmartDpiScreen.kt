// ui/screens/SmartDpiScreen.kt
package com.JM.Optimizer.ui.screens

import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.JM.Optimizer.service.SmartDpiService
import com.JM.Optimizer.ui.theme.*
import com.JM.Optimizer.util.ShellExecutor

data class GameProfileItem(
    val packageName: String,
    val appName: String,
    val dpi: Int,
    val enabled: Boolean
)

@Composable
fun SmartDpiScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("smart_dpi", android.content.Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()
    
    var serviceEnabled by remember { mutableStateOf(SmartDpiService.isRunning) }
    var defaultDpi by remember { mutableStateOf(580) }
    var games by remember { mutableStateOf<List<GameProfileItem>>(emptyList()) }
    
    // Load installed games
     LaunchedEffect(Unit) {
          val prefs = context.getSharedPreferences("smart_dpi", android.content.Context.MODE_PRIVATE)
          val pm = context.packageManager
    
          games = prefs.all.mapNotNull { (pkg, dpiValue) ->
                try {
                   val appInfo = pm.getApplicationInfo(pkg, 0)
                   val appName = pm.getApplicationLabel(appInfo).toString()
                   val dpi = dpiValue as? Int ?: 580
                   GameProfileItem(pkg, appName, dpi, true)
                } catch (e: Exception) {
                   null
                }
          }.sortedBy { it.appName }
     }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "SMART DPI SWITCH",
                        fontWeight = FontWeight.Bold,
                        color = Red
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Auto change DPI when selected game launches",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Service", color = TextWhite, modifier = Modifier.weight(1f))
                        Switch(
                            checked = serviceEnabled,
                            onCheckedChange = { enabled ->
                                serviceEnabled = enabled
                                val intent = Intent(context, SmartDpiService::class.java)
                                if (enabled) {
                                    intent.action = SmartDpiService.ACTION_START
                                    context.startForegroundService(intent)
                                } else {
                                    intent.action = SmartDpiService.ACTION_STOP
                                    context.startService(intent)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Red,
                                checkedTrackColor = Red.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "SELECTED GAMES",
                style = MaterialTheme.typography.labelMedium,
                color = Red,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(games) { game ->
            GameProfileCard(
                game = game,
                onToggle = { enabled ->
                    if (enabled) {
                        prefs.edit().putInt(game.packageName, game.dpi).apply()
                    } else {
                        prefs.edit().remove(game.packageName).apply()
                    }
                    games = games.map { 
                        if (it.packageName == game.packageName) it.copy(enabled = enabled) else it 
                    }
                },
                onDpiChange = { newDpi ->
                    prefs.edit().putInt(game.packageName, newDpi).apply()
                    games = games.map { 
                        if (it.packageName == game.packageName) it.copy(dpi = newDpi) else it 
                    }
                }
            )
        }
        
        item {
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Default DPI", color = TextWhite, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = defaultDpi.toFloat(),
                        onValueChange = { defaultDpi = it.toInt() },
                        valueRange = 320f..800f,
                        onValueChangeFinished = {
                            scope.launch {
                                ShellExecutor.setDensity(defaultDpi)
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Red,
                            activeTrackColor = Red
                        )
                    )
                    Text("$defaultDpi dpi", color = TextGray)
                    
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                ShellExecutor.resetDensity()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBg2)
                    ) {
                        Text("Reset DPI", color = TextWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun GameProfileCard(
    game: GameProfileItem,
    onToggle: (Boolean) -> Unit,
    onDpiChange: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(game.appName, color = TextWhite, fontWeight = FontWeight.Bold)
                    Text(game.packageName, color = TextGray, style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = game.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Red,
                        checkedTrackColor = Red.copy(alpha = 0.3f)
                    )
                )
            }
            
            if (game.enabled) {
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = game.dpi.toFloat(),
                    onValueChange = { onDpiChange(it.toInt()) },
                    valueRange = 320f..800f,
                    colors = SliderDefaults.colors(
                        thumbColor = Red,
                        activeTrackColor = Red
                    )
                )
                Text("DPI: ${game.dpi}", color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}