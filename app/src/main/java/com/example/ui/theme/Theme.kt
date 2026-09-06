package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityDarkAccent,
    onPrimary = HighDensityDarkButtonEqualsText,
    primaryContainer = HighDensityDarkButtonOperator,
    onPrimaryContainer = HighDensityDarkButtonOperatorText,
    secondary = HighDensityDarkButtonFunctionText,
    onSecondary = Color.White,
    secondaryContainer = HighDensityDarkButtonFunction,
    onSecondaryContainer = HighDensityDarkButtonFunctionText,
    tertiary = HighDensityDarkButtonSciText,
    onTertiary = HighDensityDarkBg,
    tertiaryContainer = HighDensityDarkButtonSci,
    onTertiaryContainer = HighDensityDarkButtonSciText,
    background = HighDensityDarkBg,
    onBackground = HighDensityDarkTextPrimary,
    surface = HighDensityDarkSurface,
    onSurface = HighDensityDarkTextPrimary,
    surfaceVariant = HighDensityDarkSurfaceVariant,
    onSurfaceVariant = HighDensityDarkTextSecondary,
    outline = HighDensityDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityLightAccent,
    onPrimary = HighDensityLightButtonEqualsText,
    primaryContainer = HighDensityLightButtonOperator,
    onPrimaryContainer = HighDensityLightButtonOperatorText,
    secondary = HighDensityLightButtonFunctionText,
    onSecondary = Color.White,
    secondaryContainer = HighDensityLightButtonFunction,
    onSecondaryContainer = HighDensityLightButtonFunctionText,
    tertiary = HighDensityLightButtonSciText,
    onTertiary = Color.White,
    tertiaryContainer = HighDensityLightButtonSci,
    onTertiaryContainer = HighDensityLightButtonSciText,
    background = HighDensityLightBg,
    onBackground = HighDensityLightTextPrimary,
    surface = HighDensityLightSurface,
    onSurface = HighDensityLightTextPrimary,
    surfaceVariant = HighDensityLightSurfaceVariant,
    onSurfaceVariant = HighDensityLightTextSecondary,
    outline = HighDensityLightBorder
)

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
