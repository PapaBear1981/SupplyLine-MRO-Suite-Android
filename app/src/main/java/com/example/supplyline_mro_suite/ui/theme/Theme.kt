package com.example.supplyline_mro_suite.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Aerospace-inspired dark color scheme
private val AerospaceDarkColorScheme = darkColorScheme(
    primary = AerospacePrimaryLight,
    onPrimary = AerospaceOnPrimary,
    primaryContainer = AerospacePrimaryDark,
    onPrimaryContainer = AerospaceOnPrimary,
    secondary = AerospaceSecondaryLight,
    onSecondary = AerospaceOnSecondary,
    secondaryContainer = AerospaceSecondary,
    onSecondaryContainer = AerospaceOnSecondary,
    tertiary = AerospaceAccentLight,
    onTertiary = AerospaceOnPrimary,
    tertiaryContainer = AerospaceAccent,
    onTertiaryContainer = AerospaceOnPrimary,
    background = AerospaceBackgroundDark,
    onBackground = AerospaceOnBackgroundDark,
    surface = AerospaceSurfaceDark,
    onSurface = AerospaceOnSurfaceDark,
    surfaceVariant = AerospaceSecondaryVariant,
    onSurfaceVariant = AerospaceOnSecondary,
    error = AerospaceError,
    onError = AerospaceOnPrimary,
    outline = AerospaceSecondaryLight,
    outlineVariant = AerospaceSecondary
)

// Aerospace-inspired light color scheme
private val AerospaceLightColorScheme = lightColorScheme(
    primary = AerospacePrimary,
    onPrimary = AerospaceOnPrimary,
    primaryContainer = AerospacePrimaryLight,
    onPrimaryContainer = AerospaceOnPrimary,
    secondary = AerospaceSecondary,
    onSecondary = AerospaceOnSecondary,
    secondaryContainer = AerospaceSecondaryLight,
    onSecondaryContainer = AerospaceOnSecondary,
    tertiary = AerospaceAccent,
    onTertiary = AerospaceOnPrimary,
    tertiaryContainer = AerospaceAccentLight,
    onTertiaryContainer = AerospaceOnPrimary,
    background = AerospaceBackground,
    onBackground = AerospaceOnBackground,
    surface = AerospaceSurface,
    onSurface = AerospaceOnSurface,
    surfaceVariant = AerospaceSurfaceVariant,
    onSurfaceVariant = AerospaceOnSurface,
    error = AerospaceError,
    onError = AerospaceOnPrimary,
    outline = AerospaceSecondary,
    outlineVariant = AerospaceSecondaryLight
)

@Composable
fun SupplyLineMROSuiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to maintain aerospace theme consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AerospaceDarkColorScheme
        else -> AerospaceLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}