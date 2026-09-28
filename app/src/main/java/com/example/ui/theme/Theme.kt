package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LofiColorScheme = darkColorScheme(
    primary = LofiPrimary,
    onPrimary = LofiOnPrimary,
    primaryContainer = LofiPrimaryContainer,
    onPrimaryContainer = LofiTextPrimary,
    secondary = LofiPurple,
    onSecondary = LofiOnPrimary,
    secondaryContainer = LofiSurfaceVariant,
    onSecondaryContainer = LofiTextPrimary,
    tertiary = LofiAmber,
    onTertiary = LofiOnPrimary,
    tertiaryContainer = LofiAmberContainer,
    onTertiaryContainer = LofiAmber,
    background = LofiDarkBg,
    onBackground = LofiTextPrimary,
    surface = LofiSurface,
    onSurface = LofiTextPrimary,
    surfaceVariant = LofiSurfaceVariant,
    onSurfaceVariant = LofiTextSecondary,
    outline = LofiCardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LofiColorScheme,
        typography = Typography,
        content = content
    )
}
