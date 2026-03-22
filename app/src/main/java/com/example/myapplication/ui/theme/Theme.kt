package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = BackgroundWhite,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue700,
    background = BackgroundWhite,
    surface = BackgroundWhite,
    surfaceVariant = SurfaceGray,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = DividerGray,
    error = ErrorRed,
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
