package com.beekeeper.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Beekeeper App Dark Color Scheme
 * Primary theme with dark green backgrounds and gold accents
 */
private val BeekeeperDarkColorScheme = darkColorScheme(
    // Primary colors - Gold accent
    primary = GoldPrimary,
    onPrimary = DarkGreenPrimary,
    primaryContainer = GoldDark,
    onPrimaryContainer = TextPrimary,

    // Secondary colors - Green
    secondary = SuccessGreen,
    onSecondary = TextPrimary,
    secondaryContainer = SuccessGreenDark,
    onSecondaryContainer = TextPrimary,

    // Tertiary colors - Info blue
    tertiary = InfoBlue,
    onTertiary = TextPrimary,
    tertiaryContainer = InfoBlueDark,
    onTertiaryContainer = TextPrimary,

    // Error colors
    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = ErrorRedDark,
    onErrorContainer = TextPrimary,

    // Background colors - Dark green
    background = SurfaceBackground,
    onBackground = TextPrimary,

    // Surface colors - Card backgrounds
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardBackgroundLight,
    onSurfaceVariant = TextSecondary,

    // Outline colors
    outline = BorderLight,
    outlineVariant = BorderDark,

    // Surface tint
    surfaceTint = GoldPrimary,

    // Inverse colors
    inverseSurface = TextPrimary,
    inverseOnSurface = DarkGreenPrimary,
    inversePrimary = GoldDark,

    // Scrim
    scrim = OverlayDark
)

@Composable
fun BeekeeperTheme(
    darkTheme: Boolean = true, // App is always dark themed
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BeekeeperDarkColorScheme,
        typography = BeekeeperTypography,
        content = content
    )
}
