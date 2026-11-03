package com.applamdep.merchant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MerchantPrimary,
    onPrimary = MerchantSurface,
    primaryContainer = MerchantPrimaryLight,
    onPrimaryContainer = MerchantPrimaryDark,
    secondary = MerchantSecondary,
    onSecondary = MerchantSurface,
    background = MerchantBackground,
    onBackground = MerchantTextPrimary,
    surface = MerchantSurface,
    onSurface = MerchantTextPrimary,
    surfaceVariant = MerchantSurfaceVariant,
    onSurfaceVariant = MerchantTextSecondary,
    error = MerchantError,
    onError = MerchantSurface
)

/**
 * Theme giao diện Merchant Portal phong cách Grab hiện đại:
 * Nền sáng, card bo góc, typography sắc nét, không lòe loẹt.
 */
@Composable
fun MerchantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MerchantTypography,
        shapes = MerchantShapes,
        content = content
    )
}
