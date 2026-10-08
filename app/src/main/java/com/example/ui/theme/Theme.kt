package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = OnMintContainer,
    secondary = WaterBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = WaterBlueContainer,
    onSecondaryContainer = OnWaterContainer,
    tertiary = SunGoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = BackgroundClean,
    onBackground = TextDarkPrimary,
    surface = SurfaceCard,
    onSurface = TextDarkPrimary,
    surfaceVariant = SurfaceVariantTint,
    onSurfaceVariant = TextDarkSecondary,
    outline = OutlineBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color.Black,
    primaryContainer = ForestGreenPrimary,
    onPrimaryContainer = Color.White,
    secondary = WaterBlueLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color.White,
    tertiary = SunGoldTertiary,
    onTertiary = Color.Black,
    background = Color(0xFF111411),
    onBackground = Color(0xFFE2E3DE),
    surface = Color(0xFF1A1D1A),
    onSurface = Color(0xFFE2E3DE),
    surfaceVariant = Color(0xFF2E332D),
    onSurfaceVariant = Color(0xFFC2C9BD)
)

@Composable
fun JalRakshakTheme(
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
