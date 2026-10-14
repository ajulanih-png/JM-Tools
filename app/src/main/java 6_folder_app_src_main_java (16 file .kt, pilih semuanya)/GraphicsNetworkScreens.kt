package com.JM.Optimizer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.JM.Optimizer.ui.theme.*
import com.JM.Optimizer.util.ShellExecutor
import kotlinx.coroutines.launch

@Composable
private fun TweakCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Red)
            Text(subtitle, color = TextGray, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun GraphicsTweakScreen() {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready") }

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TweakCard("GRAPHICS TWEAK", "Animation scale & rendering options") {
            Text("Status: $status", color = Green, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                scope.launch {
                    status = "Applying..."
                    ShellExecutor.putSetting("global", "window_animation_scale", "0.5")
                    ShellExecutor.putSetting("global", "transition_animation_scale", "0.5")
                    ShellExecutor.putSetting("global", "animator_duration_scale", "0.5")
                    status = "Animations 0.5x"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier.fillMaxWidth()
        ) { Text("FAST ANIMATIONS (0.5x)", fontWeight = FontWeight.Bold) }

        Button(
            onClick = {
                scope.launch {
                    status = "Applying..."
                    ShellExecutor.putSetting("global", "window_animation_scale", "0")
                    ShellExecutor.putSetting("global", "transition_animation_scale", "0")
                    ShellExecutor.putSetting("global", "animator_duration_scale", "0")
                    status = "Animations off"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier.fillMaxWidth()
        ) { Text("DISABLE ANIMATIONS", fontWeight = FontWeight.Bold) }

        Button(
            onClick = {
                scope.launch {
                    status = "Applying..."
                    ShellExecutor.setProp("debug.hwui.renderer", "skiagl")
                    ShellExecutor.setProp("debug.sf.disable_backpressure", "1")
                    status = "Render props set"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBg3),
            modifier = Modifier.fillMaxWidth()
        ) { Text("RENDER PROPS (needs root)", color = TextWhite) }

        Button(
            onClick = {
                scope.launch {
                    ShellExecutor.putSetting("global", "window_animation_scale", "1")
                    ShellExecutor.putSetting("global", "transition_animation_scale", "1")
                    ShellExecutor.putSetting("global", "animator_duration_scale", "1")
                    status = "Reset to default"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBg2),
            modifier = Modifier.fillMaxWidth()
        ) { Text("RESET", color = TextWhite) }
    }
}

@Composable
fun NetworkTweakScreen() {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready") }

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TweakCard("NETWORK TWEAK", "Lower latency while gaming") {
            Text("Status: $status", color = Green, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                scope.launch {
                    status = "Applying..."
                    ShellExecutor.putSetting("global", "wifi_sleep_policy", "2")
                    ShellExecutor.putSetting("global", "wifi_power_save", "0")
                    ShellExecutor.putSetting("global", "mobile_data_always_on", "1")
                    status = "Wi-Fi always awake"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier.fillMaxWidth()
        ) { Text("KEEP WI-FI AWAKE", fontWeight = FontWeight.Bold) }

        Button(
            onClick = {
                scope.launch {
                    status = "Applying..."
                    ShellExecutor.setProp("net.tcp.buffersize.default", "4096,87380,256960,4096,16384,256960")
                    ShellExecutor.setProp("net.tcp.buffersize.wifi", "4096,87380,256960,4096,16384,256960")
                    status = "TCP buffers set"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBg3),
            modifier = Modifier.fillMaxWidth()
        ) { Text("TCP BUFFERS (needs root)", color = TextWhite) }

        Button(
            onClick = {
                scope.launch {
                    ShellExecutor.putSetting("global", "wifi_sleep_policy", "0")
                    ShellExecutor.putSetting("global", "wifi_power_save", "1")
                    status = "Reset to default"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkBg2),
            modifier = Modifier.fillMaxWidth()
        ) { Text("RESET", color = TextWhite) }
    }
}
