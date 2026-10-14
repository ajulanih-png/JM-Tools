// ui/theme/Theme.kt
package com.JM.Optimizer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JMColorScheme = darkColorScheme(
    primary = Red,
    onPrimary = TextWhite,
    primaryContainer = RedDark,
    onPrimaryContainer = TextWhite,
    secondary = Orange,
    onSecondary = DarkBg,
    tertiary = Green,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextWhite,
    surface = DarkCard,
    onSurface = TextWhite,
    surfaceVariant = DarkBg2,
    onSurfaceVariant = TextGray,
    outline = BorderMid,
    error = Red,
    onError = TextWhite
)

@Composable
fun JMOptimizerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = JMColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBg.toArgb()
            window.navigationBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = JMTypography,
        content = content
    )
}