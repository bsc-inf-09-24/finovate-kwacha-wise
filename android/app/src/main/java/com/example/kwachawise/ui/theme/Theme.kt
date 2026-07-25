package com.example.kwachawise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = KwachaPrimary,
    secondary = KwachaSecondary,
    background = KwachaDarkBlue,
    surface = KwachaDarkBlue,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = KwachaPrimary,
    secondary = KwachaSecondary,
    background = KwachaBackground,
    surface = KwachaSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = KwachaOnBackground,
    onSurface = KwachaOnSurface
)

@Composable
fun KwachaWiseTheme(
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