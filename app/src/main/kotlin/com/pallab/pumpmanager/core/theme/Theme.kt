package com.pallab.pumpmanager.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Blue500,
    onPrimary          = SurfaceLight,
    primaryContainer   = Blue50,
    onPrimaryContainer = Blue700,
    secondary          = Green500,
    onSecondary        = SurfaceLight,
    secondaryContainer = Green50,
    onSecondaryContainer = Green600,
    background         = Neutral50,
    onBackground       = Neutral900,
    surface            = SurfaceLight,
    onSurface          = Neutral900,
    surfaceVariant     = Neutral100,
    onSurfaceVariant   = Neutral600,
    outline            = Neutral200,
    outlineVariant     = Neutral200,
    error              = RedError,
    onError            = SurfaceLight,
    errorContainer     = Color(0xFFFEF2F2),
    onErrorContainer   = RedError600,
    tertiary           = AmberWarning,
    onTertiary         = SurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary            = Blue500,
    onPrimary          = SurfaceLight,
    primaryContainer   = Blue700,
    onPrimaryContainer = Blue100,
    secondary          = Green500,
    onSecondary        = SurfaceLight,
    secondaryContainer = Green600,
    onSecondaryContainer = Green50,
    background         = Dark100,
    onBackground       = Dark900,
    surface            = SurfaceDark,
    onSurface          = Dark900,
    surfaceVariant     = Dark50,
    onSurfaceVariant   = Dark400,
    outline            = Dark200,
    outlineVariant     = Dark200,
    error              = RedError,
    onError            = SurfaceLight,
    errorContainer     = Color(0xFF450A0A),
    onErrorContainer   = Color(0xFFFCA5A5),
    tertiary           = AmberWarning,
    onTertiary         = Color(0xFF451A03),
)

@Composable
fun PumpManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}
