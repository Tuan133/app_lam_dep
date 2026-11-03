package com.applamdep.customer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GrabGreen,
    onPrimary = Color.White,
    primaryContainer = GrabGreenLight,
    onPrimaryContainer = GrabGreenDark,
    secondary = GrabAccentGold,
    background = GrabBackground,
    surface = GrabSurface,
    onBackground = GrabTextPrimary,
    onSurface = GrabTextPrimary,
    outline = GrabBorder
)

@Composable
fun GrabBeautyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Luôn ưu tiên giao diện nền sáng tươi mới theo chuẩn yêu cầu Grab UI
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
