package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SecurityDarkColorScheme = darkColorScheme(
    primary = SecurityGreen,
    onPrimary = Color.Black,
    primaryContainer = SecurityGreenDark,
    onPrimaryContainer = Color.White,
    secondary = SecurityCyan,
    onSecondary = Color.Black,
    tertiary = SecurityBlue,
    background = SecurityDarkBackground,
    onBackground = TextPrimaryDark,
    surface = SecurityDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SecurityDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    error = DangerRed,
    onError = Color.White
)

private val SecurityLightColorScheme = lightColorScheme(
    primary = SecurityGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = SecurityBlue,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = DangerRed,
    onError = Color.White
)

@Composable
fun SecurityServiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SecurityDarkColorScheme
        else -> SecurityLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
