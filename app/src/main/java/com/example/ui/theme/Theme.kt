package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TheiaColorScheme = darkColorScheme(
    primary = TheiaPrimary,
    onPrimary = TheiaOnPrimary,
    primaryContainer = TheiaPrimaryDim,
    secondary = TheiaSecondary,
    background = TheiaBackground,
    surface = TheiaSurface,
    surfaceVariant = TheiaSurfaceVariant,
    onBackground = TheiaOnSurface,
    onSurface = TheiaOnSurface,
    onSurfaceVariant = TheiaOnSurfaceVariant,
    outline = TheiaOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force consistent branding for Smart Glasses / HUD overlay
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TheiaColorScheme,
        typography = Typography,
        content = content
    )
}
