package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AnimeDarkColorScheme = darkColorScheme(
    primary = AnimeCrimson,
    onPrimary = Color.White,
    primaryContainer = AnimeCrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = AnimeCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF005F63),
    onSecondaryContainer = Color.White,
    tertiary = AnimeGold,
    onTertiary = Color.Black,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = ErrorRed,
    onError = Color.White
)

private val AnimeLightColorScheme = lightColorScheme(
    primary = AnimeCrimson,
    onPrimary = Color.White,
    secondary = AnimeCyan,
    onSecondary = Color.White,
    tertiary = AnimeGold,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = ErrorRed
)

@Composable
fun AnimeStreamTheme(
    darkTheme: Boolean = true, // Default to dark theme for anime streaming aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AnimeDarkColorScheme else AnimeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
