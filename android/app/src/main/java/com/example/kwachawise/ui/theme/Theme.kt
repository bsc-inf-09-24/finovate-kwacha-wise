package com.example.kwachawise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.kwachawise.data.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = KwachaPrimary,
    secondary = KwachaSecondary,
    background = KwachaDarkBlue,
    surface = KwachaDarkBlue,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = KwachaError
)

private val LightColorScheme = lightColorScheme(
    primary = KwachaPrimary,
    secondary = KwachaSecondary,
    background = KwachaBackground,
    surface = KwachaSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = KwachaOnBackground,
    onSurface = KwachaOnSurface,
    error = KwachaError
)

@Composable
fun KwachaWiseTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val assets = ThemeAssets.getAssets(isDark = darkTheme)

    CompositionLocalProvider(
        LocalKwachaAssets provides assets
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
