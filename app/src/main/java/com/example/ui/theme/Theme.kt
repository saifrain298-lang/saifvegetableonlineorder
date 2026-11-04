package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = SecondaryGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F2E3),
    onSecondaryContainer = Color(0xFF04381F),
    tertiary = AccentOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEBD6),
    onTertiaryContainer = Color(0xFF6B2600),
    background = SurfaceBg,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFEDF5EF),
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = DividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4EDE92),
    onPrimary = Color(0xFF00391E),
    primaryContainer = Color(0xFF0A5332),
    onPrimaryContainer = Color(0xFF6FFB9C),
    secondary = Color(0xFF66D494),
    onSecondary = Color(0xFF00381D),
    secondaryContainer = Color(0xFF135032),
    onSecondaryContainer = Color(0xFF8FF4BC),
    tertiary = Color(0xFFFFB77C),
    onTertiary = Color(0xFF4D1700),
    background = Color(0xFF0F1511),
    onBackground = Color(0xFFE1E8E2),
    surface = Color(0xFF161F18),
    onSurface = Color(0xFFE1E8E2),
    surfaceVariant = Color(0xFF222F26),
    onSurfaceVariant = Color(0xFFC0CEC3),
    outline = Color(0xFF384A3E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
