// MainActivity.kt
package com.JM.Optimizer

import kotlinx.coroutines.launch
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.JM.Optimizer.ui.screens.*
import com.JM.Optimizer.ui.theme.*
import com.JM.Optimizer.util.ShellExecutor

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize shell executor
        ShellExecutor.init(this)
        
        setContent {
            JMOptimizerTheme {
                JMApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JMApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Screens
    val screens = listOf(
        Screen("home", "Home", Icons.Default.Home),
        Screen("game_picker", "Pilih Game", Icons.Default.VideogameAsset),
        Screen("smart_dpi", "Smart DPI", Icons.Default.ScreenRotation),
        Screen("pointer", "Pointer Speed", Icons.Default.TouchApp),
        Screen("graphics", "Graphics", Icons.Default.Memory),
        Screen("network", "Network", Icons.Default.Wifi),
        Screen("bloat", "Bloat Cleaner", Icons.Default.CleaningServices),
        Screen("mouse", "Mouse Speed", Icons.Default.Mouse),
        Screen("info", "Device Info", Icons.Default.Info)
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBg2
            ) {
                Spacer(Modifier.height(32.dp))
                
                Text(
                    "JM",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Red,
                    modifier = Modifier.padding(16.dp)
                )
                
                Text(
                    "Game Optimizer v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Divider(color = BorderMid)
                
                screens.forEach { screen ->
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Red.copy(alpha = 0.2f),
                            selectedIconColor = Red,
                            selectedTextColor = Red,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            screens.find { it.route == currentRoute }?.title ?: "JM",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBg,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextWhite
                    )
                )
            },
            containerColor = DarkBg
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {
                composable("home") { HomeScreen(navController) }
                composable("game_picker") { GamePickerScreen(navController) }
                composable("smart_dpi") { SmartDpiScreen() }
                composable("pointer") { PointerSpeedScreen() }
                composable("graphics") { GraphicsTweakScreen() }
                composable("network") { NetworkTweakScreen() }
                composable("bloat") { BloatCleanerScreen() }
                composable("mouse") { MouseSpeedScreen() }
                composable("info") { DeviceInfoScreen() }
            }
        }
    }
}

data class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
)