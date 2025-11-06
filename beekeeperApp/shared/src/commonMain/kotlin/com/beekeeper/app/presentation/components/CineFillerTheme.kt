// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/theme/CineFillerTheme.kt
package com.beekeeper.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

/**
 * CineFiller theme that wraps Material3 theme with our custom colors
 */
@Composable
fun CineFillerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    
    // Convert our custom colors to Material3 ColorScheme
    val colorScheme = if (theme.isDarkMode) {
        darkColorScheme(
            primary = theme.colors.primary,
            onPrimary = theme.colors.onPrimary,
            primaryContainer = theme.colors.primaryContainer,
            onPrimaryContainer = theme.colors.onPrimaryContainer,
            
            secondary = theme.colors.secondary,
            onSecondary = theme.colors.onSecondary,
            secondaryContainer = theme.colors.secondaryContainer,
            onSecondaryContainer = theme.colors.onSecondaryContainer,
            
            tertiary = theme.colors.tertiary,
            onTertiary = Color.White,
            tertiaryContainer = theme.colors.tertiaryContainer,
            onTertiaryContainer = Color.White,
            
            error = theme.colors.error,
            errorContainer = theme.colors.errorContainer,
            onError = Color.White,
            onErrorContainer = Color.White,
            
            background = theme.colors.background,
            onBackground = theme.colors.onBackground,
            
            surface = theme.colors.surface,
            onSurface = theme.colors.onSurface,
            surfaceVariant = theme.colors.surfaceVariant,
            onSurfaceVariant = theme.colors.onSurfaceVariant,
            
            outline = theme.colors.outline,
            outlineVariant = theme.colors.outlineVariant,
            
            inverseSurface = theme.colors.inverseSurface,
            inverseOnSurface = theme.colors.inverseOnSurface,
            inversePrimary = theme.colors.inversePrimary,
            
            surfaceTint = theme.colors.primary,
            scrim = theme.colors.scrim
        )
    } else {
        lightColorScheme(
            primary = theme.colors.primary,
            onPrimary = theme.colors.onPrimary,
            primaryContainer = theme.colors.primaryContainer,
            onPrimaryContainer = theme.colors.onPrimaryContainer,
            
            secondary = theme.colors.secondary,
            onSecondary = theme.colors.onSecondary,
            secondaryContainer = theme.colors.secondaryContainer,
            onSecondaryContainer = theme.colors.onSecondaryContainer,
            
            tertiary = theme.colors.tertiary,
            onTertiary = Color.White,
            tertiaryContainer = theme.colors.tertiaryContainer,
            onTertiaryContainer = Color.Black,
            
            error = theme.colors.error,
            errorContainer = theme.colors.errorContainer,
            onError = Color.White,
            onErrorContainer = Color.Black,
            
            background = theme.colors.background,
            onBackground = theme.colors.onBackground,
            
            surface = theme.colors.surface,
            onSurface = theme.colors.onSurface,
            surfaceVariant = theme.colors.surfaceVariant,
            onSurfaceVariant = theme.colors.onSurfaceVariant,
            
            outline = theme.colors.outline,
            outlineVariant = theme.colors.outlineVariant,
            
            inverseSurface = theme.colors.inverseSurface,
            inverseOnSurface = theme.colors.inverseOnSurface,
            inversePrimary = theme.colors.inversePrimary,
            
            surfaceTint = theme.colors.primary,
            scrim = theme.colors.scrim
        )
    }
    
    // Set system UI colors - only on Android (commented out for desktop compatibility)
    // val systemUiController = rememberSystemUiController()
    // SideEffect {
    //     systemUiController.setSystemBarsColor(
    //         color = if (theme.isDarkMode) {
    //             Color(0xFF0A0E1A)
    //         } else {
    //             Color(0xFFF8FAFB)
    //         },
    //         darkIcons = !theme.isDarkMode
    //     )
    // }
    
    // Provide both Material3 theme and our custom theme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = {
            AppThemeProvider { _ ->
                content()
            }
        }
    )
}
