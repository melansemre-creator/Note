package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB388FF),
    onPrimary = Color(0xFF31007D),
    primaryContainer = Color(0xFF4A148C),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF82B1FF),
    onSecondary = Color(0xFF00296B),
    background = Color(0xFF12121A),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E2C),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2B2B40),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF673AB7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7F6),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF29B6F6),
    onSecondary = Color.White,
    background = Color(0xFFF8F9FE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0F0F8),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun MyHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
