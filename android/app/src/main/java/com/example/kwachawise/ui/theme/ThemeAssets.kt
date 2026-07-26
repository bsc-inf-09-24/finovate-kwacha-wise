package com.example.kwachawise.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.kwachawise.R

/**
 * Single source of truth for theme-dependent brand assets.
 */
data class KwachaAssets(
    @DrawableRes val wordmark: Int,
    @DrawableRes val appIcon: Int,
    @DrawableRes val pattern: Int,
    @DrawableRes val bwWordmark: Int,
    @DrawableRes val outlineLogomark: Int,
    @DrawableRes val invertedOutlineLogomark: Int
)

val LocalKwachaAssets = staticCompositionLocalOf<KwachaAssets> {
    error("No KwachaAssets provided")
}

object ThemeAssets {
    @Composable
    fun current(): KwachaAssets = LocalKwachaAssets.current

    fun getAssets(isDark: Boolean): KwachaAssets {
        return if (isDark) {
            KwachaAssets(
                wordmark = R.drawable.light_wordmark, 
                appIcon = R.drawable.logomark,
                pattern = R.drawable.logomark_pattern,
                bwWordmark = R.drawable.b_w_wordmark,
                outlineLogomark = R.drawable.outline_logomark,
                invertedOutlineLogomark = R.drawable.inverted_outline_logomark
            )
        } else {
            KwachaAssets(
                wordmark = R.drawable.dark_wordmark,
                appIcon = R.drawable.outline_logomark,
                pattern = R.drawable.logomark_pattern,
                bwWordmark = R.drawable.b_w_wordmark,
                outlineLogomark = R.drawable.outline_logomark,
                invertedOutlineLogomark = R.drawable.inverted_outline_logomark
            )
        }
    }
}
