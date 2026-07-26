package com.example.kwachawise.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.kwachawise.ui.theme.ThemeAssets
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val assets = ThemeAssets.current()
    
    LaunchedEffect(Unit) {
        delay(1500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Pattern Background
        Image(
            painter = painterResource(id = assets.pattern),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.08f), // 8% opacity as requested
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(
                color = MaterialTheme.colorScheme.onBackground,
                blendMode = BlendMode.SrcIn
            )
        )

        // Centered Brand Wordmark (Theme-aware)
        Image(
            painter = painterResource(id = assets.wordmark),
            contentDescription = "KwachaWise Logo",
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}
