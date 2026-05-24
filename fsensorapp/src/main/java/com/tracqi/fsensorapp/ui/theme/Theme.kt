package com.tracqi.fsensorapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberpunkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBackground,
    primaryContainer = CyberSurfaceBright,
    onPrimaryContainer = NeonCyan,
    secondary = NeonMagenta,
    onSecondary = CyberBackground,
    secondaryContainer = CyberSurfaceBright,
    onSecondaryContainer = NeonMagenta,
    tertiary = NeonGreen,
    onTertiary = CyberBackground,
    tertiaryContainer = CyberSurfaceBright,
    onTertiaryContainer = NeonGreen,
    error = ErrorRed,
    onError = CyberBackground,
    background = CyberBackground,
    onBackground = PrimaryText,
    surface = CyberSurface,
    onSurface = PrimaryText,
    surfaceVariant = CyberSurfaceBright,
    onSurfaceVariant = SecondaryText,
    outline = SecondaryText,
    outlineVariant = GridColor,
    surfaceContainerLow = CyberSurface,
    surfaceContainer = CyberSurface,
    surfaceContainerHigh = CyberSurfaceBright
)

@Composable
fun FSensorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberpunkColorScheme,
        content = content
    )
}
