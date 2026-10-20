package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StealthDarkColorScheme = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = TextWhite,
    primaryContainer = DarkCrimson,
    onPrimaryContainer = TextWhite,
    secondary = CrimsonRedHover,
    onSecondary = TextWhite,
    secondaryContainer = DarkCrimson,
    onSecondaryContainer = TextWhite,
    tertiary = GlowingRed,
    background = StealthBlack,
    onBackground = TextWhite,
    surface = StealthDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = StealthSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = StealthCardBorder,
    outlineVariant = StealthCardBorder,
    error = CrimsonRed,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Red & Black stealth tactical theme is permanent for consistent stealth aesthetics
    MaterialTheme(
        colorScheme = StealthDarkColorScheme,
        typography = Typography,
        content = content
    )
}
