package com.pemmob.tulungin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TulunginMintBackground,
    onPrimary = TulunginPrimary,
    primaryContainer = TulunginPrimary,
    onPrimaryContainer = Color.White,
    secondary = TulunginMintLight,
    onSecondary = TulunginPrimary,
    background = Color(0xFF121415),
    surface = Color(0xFF1A1D1E),
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0xFF2C3235),
    outlineVariant = Color(0xFF1F2E33),
    error = TulunginDangerBorder,
    errorContainer = TulunginDangerContainer,
    onErrorContainer = TulunginDangerText
)

private val LightColorScheme = lightColorScheme(
    primary = TulunginPrimary,
    onPrimary = Color.White,
    primaryContainer = TulunginMintBackground,
    onPrimaryContainer = TulunginPrimary,
    secondary = TulunginPrimary,
    onSecondary = Color.White,
    secondaryContainer = TulunginMintSoft,
    onSecondaryContainer = TulunginTextPrimary,
    background = Color.White,
    surface = Color.White,
    onBackground = TulunginTextPrimary,
    onSurface = TulunginTextPrimary,
    surfaceVariant = TulunginInputBg,
    onSurfaceVariant = TulunginTextSecondary,
    outline = TulunginInputBorder,
    outlineVariant = TulunginMintBorder,
    error = TulunginDangerBorder,
    errorContainer = TulunginDangerContainer,
    onErrorContainer = TulunginDangerText
)

@Composable
fun TulunginTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}