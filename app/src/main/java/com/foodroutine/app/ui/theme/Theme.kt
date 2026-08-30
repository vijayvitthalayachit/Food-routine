package com.foodroutine.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF2E7D32)
private val GreenLight = Color(0xFF60AD5E)
private val Saffron = Color(0xFFF4A825)

private val LightColors = lightColorScheme(
    primary = Green,
    secondary = Saffron,
    tertiary = GreenLight
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    secondary = Saffron,
    tertiary = Green
)

@Composable
fun FoodRoutineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
