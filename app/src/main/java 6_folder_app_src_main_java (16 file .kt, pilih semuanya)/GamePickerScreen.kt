// ui/screens/GamePickerScreen.kt
package com.JM.Optimizer.ui.screens

import kotlinx.coroutines.launch
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import com.JM.Optimizer.ui.theme.*

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.Bitmap?,
    val isSystem: Boolean
)

@Composable
fun GamePickerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val pm = context.packageManager
    
    var searchQuery by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var allApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load apps
    LaunchedEffect(showSystemApps) {
        isLoading = true
        allApps = loadInstalledApps(context, showSystemApps)
        isLoading = false
    }
    
    // Load saved selection
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("smart_dpi", android.content.Context.MODE_PRIVATE)
        selectedApps = prefs.all.keys
    }
    
    // Filter
    val filteredApps = remember(searchQuery, allApps) {
        allApps.filter { 
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "🎮 PILIH GAME UNTUK OPTIMASI",
                    color = Red,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Pilih game yang mau di-optimize. Game yang dipilih bakal otomatis di-tuning waktu dibuka.",
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Dipilih: ${selectedApps.size} game",
                    color = Green,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari game...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = TextGray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Red,
                unfocusedBorderColor = BorderMid,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = Red
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Filter Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Tampilkan system apps",
                color = TextGray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = showSystemApps,
                onCheckedChange = { showSystemApps = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Red,
                    checkedTrackColor = Red.copy(alpha = 0.3f)
                )
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Loading
        if (isLoading) {
            Box(
                Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Red)
            }
        }
        
        // Apps List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                AppPickerItem(
                    app = app,
                    isSelected = app.packageName in selectedApps,
                    onToggle = { selected ->
                        selectedApps = if (selected) {
                            selectedApps + app.packageName
                        } else {
                            selectedApps - app.packageName
                        }
                    }
                )
            }
            
            // Save Button at bottom
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("smart_dpi", android.content.Context.MODE_PRIVATE)
                        val editor = prefs.edit()
                        editor.clear()
                        selectedApps.forEach { pkg ->
                            editor.putInt(pkg, 580) // default DPI
                        }
                        editor.apply()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("SIMPAN (${selectedApps.size})", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AppPickerItem(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Red.copy(alpha = 0.15f) else DarkCard
        ),
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isSelected,
                onValueChange = onToggle
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (app.icon != null) {
                Image(
                    bitmap = app.icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
            } else {
                Surface(
                    color = Red.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            app.appName.firstOrNull()?.toString() ?: "?",
                            color = Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Info
            Column(Modifier.weight(1f)) {
                Text(
                    app.appName,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    app.packageName,
                    color = TextGray,
                    style = MaterialTheme.typography.labelSmall
                )
                if (app.isSystem) {
                    Text(
                        "System App",
                        color = Yellow,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = Red,
                    checkmarkColor = TextWhite
                )
            )
        }
    }
}

/**
 * Load installed apps from package manager
 */
fun loadInstalledApps(context: android.content.Context, includeSystem: Boolean): List<AppInfo> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    
    val apps = pm.queryIntentActivities(mainIntent, 0)
    
    return apps.mapNotNull { resolveInfo ->
        try {
            val pkgName = resolveInfo.activityInfo.packageName
            val appInfo = pm.getApplicationInfo(pkgName, 0)
            val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            
            if (!includeSystem && isSystem) return@mapNotNull null
            
            // Skip self
            if (pkgName == context.packageName) return@mapNotNull null
            
            val appName = resolveInfo.loadLabel(pm).toString()
            val icon = try {
                resolveInfo.loadIcon(pm).toBitmap(96, 96)
            } catch (e: Exception) {
                null
            }
            
            AppInfo(
                packageName = pkgName,
                appName = appName,
                icon = icon,
                isSystem = isSystem
            )
        } catch (e: Exception) {
            null
        }
    }.distinctBy { it.packageName }
     .sortedBy { it.appName.lowercase() }
}