package com.courrier.archive

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    secondary = Color(0xFF625B71),
    background = Color(0xFFF6F3FF),
    surface = Color.White,
    onSurface = Color(0xFF1D1B20)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    secondary = Color(0xFFCCC2DC),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2B2B2F),
    onSurface = Color.White
)

@Composable
fun CourierArchiveTheme(content: @Composable () -> Unit) {
    val colors = if (MaterialTheme.colorScheme == LightColors) LightColors else DarkColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
