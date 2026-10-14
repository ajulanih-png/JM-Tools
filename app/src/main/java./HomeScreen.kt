// ui/screens/HomeScreen.kt
package com.JM.Optimizer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.JM.Optimizer.ui.theme.*
import com.JM.Optimizer.util.RootChecker
import com.JM.Optimizer.util.ShizukuHelper
import com.JM.Optimizer.util.ShellExecutor

@Composable
fun HomeScreen(navController: NavHostController) {
    var isRooted by remember { mutableStateOf(false) }
    var isShizuku by remember { mutableStateOf(false) }
    var method by remember { mutableStateOf(ShellExecutor.currentMethod) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val r = try { RootChecker.isRooted() } catch (e: Throwable) { false }
            val z = try { ShizukuHelper.isAvailable() } catch (e: Throwable) { false }
            isRooted = r
            isShizuku = z
            method = ShellExecutor.currentMethod
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Rocket,
                        contentDescription = null,
                        tint = Red,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "JM",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Red
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Game Optimizer",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextGray
                )
                Spacer(Modifier.height(16.dp))
                
                Row {
                    StatusChip(
                        text = if (isRooted) "ROOT ✓" else "NO ROOT",
                        color = if (isRooted) Green else Red
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusChip(
                        text = if (isShizuku) "SHIZUKU ✓" else "NO SHIZUKU",
                        color = if (isShizuku) Green else TextGray
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    "Method: ${method.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Quick Actions
        Text(
            "QUICK ACTIONS",
            style = MaterialTheme.typography.labelMedium,
            color = Red,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(12.dp))
        
        FeatureCard(
            icon = Icons.Default.VideogameAsset,
            title = "Pilih Game",
            desc = "Pilih game untuk di-optimize",
            onClick = { navController.navigate("game_picker") }
        )
        
        FeatureCard(
            icon = Icons.Default.ScreenRotation,
            title = "Smart DPI Switch",
            desc = "Auto change DPI for selected games",
            onClick = { navController.navigate("smart_dpi") }
        )
        
        FeatureCard(
            icon = Icons.Default.TouchApp,
            title = "Pointer Speed",
            desc = "Adjust touch sensitivity",
            onClick = { navController.navigate("pointer") }
        )
        
        FeatureCard(
            icon = Icons.Default.Memory,
            title = "Graphics Tweaks",
            desc = "Boost GPU & rendering",
            onClick = { navController.navigate("graphics") }
        )
        
        FeatureCard(
            icon = Icons.Default.Wifi,
            title = "Network Tweaks",
            desc = "Reduce ping & jitter",
            onClick = { navController.navigate("network") }
        )
        
        FeatureCard(
            icon = Icons.Default.CleaningServices,
            title = "Bloat Cleaner",
            desc = "Auto clean cache on launch",
            onClick = { navController.navigate("bloat") }
        )
        
        FeatureCard(
            icon = Icons.Default.Mouse,
            title = "Mouse Speed",
            desc = "Adjust mouse pointer (-7 to +7)",
            onClick = { navController.navigate("mouse") }
        )
        
        FeatureCard(
            icon = Icons.Default.Info,
            title = "Device Info",
            desc = "View RAM, storage, CPU, battery",
            onClick = { navController.navigate("info") }
        )
    }
}

@Composable
fun StatusChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Red.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Red)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = TextGray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGray)
        }
    }
}