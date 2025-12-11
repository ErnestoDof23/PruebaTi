package com.example.notasapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import mx.edu.utez.integrtadoranotes.ui.theme.Pink40
import mx.edu.utez.integrtadoranotes.ui.theme.Pink80
import mx.edu.utez.integrtadoranotes.ui.theme.Purple40
import mx.edu.utez.integrtadoranotes.ui.theme.Purple80
import mx.edu.utez.integrtadoranotes.ui.theme.PurpleGrey40
import mx.edu.utez.integrtadoranotes.ui.theme.PurpleGrey80

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun NotasAppTheme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}