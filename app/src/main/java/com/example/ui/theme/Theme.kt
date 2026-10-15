package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val StockVisionDarkColorScheme = darkColorScheme(
    primary = BrandEmerald,
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF00513B),
    onPrimaryContainer = Color(0xFF70F8C8),
    secondary = BrandCyan,
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D62),
    onSecondaryContainer = Color(0xFFA5EEFF),
    tertiary = BrandGold,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF633F00),
    onTertiaryContainer = Color(0xFFFFDDB3),
    error = BrandCrimson,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = TerminalBg,
    onBackground = TerminalTextPrimary,
    surface = TerminalSurface,
    onSurface = TerminalTextPrimary,
    surfaceVariant = TerminalSurfaceElevated,
    onSurfaceVariant = TerminalTextSecondary,
    outline = TerminalBorder,
    outlineVariant = TerminalSurfaceVariant
)

val StockVisionLightColorScheme = lightColorScheme(
    primary = BrandEmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCEF8E7),
    onPrimaryContainer = Color(0xFF002115),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF92400E),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun StockVisionTheme(
    darkTheme: Boolean = true, // Default to sleek Demat Dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StockVisionDarkColorScheme else StockVisionLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
