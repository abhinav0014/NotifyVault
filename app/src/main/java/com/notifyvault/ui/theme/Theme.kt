package com.notifyvault.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val VaultPurple      = Color(0xFF7C3AED)
val VaultPurpleLight = Color(0xFFA78BFA)
val VaultIndigo      = Color(0xFF4F46E5)
val VaultCyan        = Color(0xFF06B6D4)
val VaultEmerald     = Color(0xFF10B981)
val VaultAmber       = Color(0xFFF59E0B)
val VaultRose        = Color(0xFFF43F5E)

private val DarkColorScheme = darkColorScheme(
    primary = VaultPurpleLight,
    onPrimary = Color(0xFF1A0050),
    primaryContainer = Color(0xFF5B21B6),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = VaultCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFFB2EBEF),
    tertiary = VaultEmerald,
    onTertiary = Color(0xFF00391F),
    tertiaryContainer = Color(0xFF00522D),
    onTertiaryContainer = Color(0xFFB7F5D4),
    error = VaultRose,
    background = Color(0xFF0F0A1A),
    onBackground = Color(0xFFEDE9FE),
    surface = Color(0xFF1A1230),
    onSurface = Color(0xFFEDE9FE),
    surfaceVariant = Color(0xFF2D1F4A),
    onSurfaceVariant = Color(0xFFCDC5E1),
    outline = Color(0xFF9784C2),
    outlineVariant = Color(0xFF3D2E60),
    inverseSurface = Color(0xFFEDE9FE),
    inverseOnSurface = Color(0xFF1A1230),
    inversePrimary = VaultPurple,
    surfaceTint = VaultPurpleLight,
)

private val LightColorScheme = lightColorScheme(
    primary = VaultPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = VaultIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = VaultEmerald,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF022C22),
    error = VaultRose,
    background = Color(0xFFFAF7FF),
    onBackground = Color(0xFF1A0A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A0A2E),
    surfaceVariant = Color(0xFFF3EEFF),
    onSurfaceVariant = Color(0xFF4A3B6B),
    outline = Color(0xFF9784C2),
    outlineVariant = Color(0xFFD4C9ED),
    inverseSurface = Color(0xFF2E2050),
    inverseOnSurface = Color(0xFFF3EEFF),
    inversePrimary = VaultPurpleLight,
    surfaceTint = VaultPurple,
)

@Composable
fun NotifyVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
