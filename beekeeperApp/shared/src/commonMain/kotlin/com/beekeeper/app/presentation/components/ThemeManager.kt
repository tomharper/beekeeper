// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/theme/ThemeManager.kt
package com.beekeeper.app.presentation.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Complete theme configuration for the app
 */
data class AppTheme(
    val isDarkMode: Boolean,
    val colors: ThemeColors,
    val typography: ThemeTypography,
    val spacing: ThemeSpacing,
    val elevation: ThemeElevation,
    val shapes: ThemeShapes,
    val name: String
)

data class ThemeColors(
    // Primary colors
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryVariant: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,

    // Secondary colors
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,

    // Tertiary colors
    val tertiary: Color,
    val tertiaryContainer: Color,

    // Text colors
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,

    // Surface colors
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val onPrimary: Color,
    val onSecondary: Color,

    // Semantic colors
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val info: Color,
    val infoContainer: Color,

    // Utility colors
    val divider: Color,
    val outline: Color,
    val outlineVariant: Color,
    val scrim: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,

    // Special effects
    val shimmer: Color,
    val ripple: Color,
    val shadow: Color
)

data class ThemeTypography(
    val displayLarge: Float = 57f,
    val displayMedium: Float = 45f,
    val displaySmall: Float = 36f,
    val headlineLarge: Float = 32f,
    val headlineMedium: Float = 28f,
    val headlineSmall: Float = 24f,
    val titleLarge: Float = 22f,
    val titleMedium: Float = 16f,
    val titleSmall: Float = 14f,
    val bodyLarge: Float = 16f,
    val bodyMedium: Float = 14f,
    val bodySmall: Float = 12f,
    val labelLarge: Float = 14f,
    val labelMedium: Float = 12f,
    val labelSmall: Float = 11f
)

data class ThemeSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp
)

data class ThemeElevation(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 1.dp,
    val small: Dp = 2.dp,
    val medium: Dp = 4.dp,
    val large: Dp = 8.dp,
    val extraLarge: Dp = 16.dp
)

data class ThemeShapes(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 24.dp,
    val circular: Dp = 50.dp
)

object DarkTheme {
    val colors = ThemeColors(
        // Rich dark backgrounds with depth
        background = Color(0xFF0A0E1A),  // Deep space blue-black
        surface = Color(0xFF1A1F2E),     // Elevated surface
        surfaceVariant = Color(0xFF252B3D), // Card backgrounds

        // Vibrant primary colors
        primary = Color(0xFF5E9EFF),     // Bright electric blue
        primaryVariant = Color(0xFF4A7FDB), // Deeper blue
        primaryContainer = Color(0xFF1E3A5F), // Dark blue container
        onPrimaryContainer = Color(0xFFB8D4FF), // Light blue on container

        // Accent colors
        secondary = Color(0xFF9C88FF),   // Purple accent
        secondaryContainer = Color(0xFF3D2F7A), // Dark purple container
        onSecondaryContainer = Color(0xFFDDD6FE), // Light purple

        // Tertiary colors
        tertiary = Color(0xFF64FFDA),    // Teal accent
        tertiaryContainer = Color(0xFF00574B), // Dark teal

        // Text hierarchy
        textPrimary = Color(0xFFF5F5F7), // Almost white
        textSecondary = Color(0xFFB8BCC8), // Muted gray
        textTertiary = Color(0xFF7A8091), // Darker gray

        // Surface text
        onBackground = Color(0xFFF5F5F7),
        onSurface = Color(0xFFF5F5F7),
        onSurfaceVariant = Color(0xFFCAD0DC),
        onPrimary = Color(0xFF000814),   // Dark on primary
        onSecondary = Color(0xFF1A0F3D), // Dark on secondary

        // Semantic colors with glow effects
        error = Color(0xFFFF6B6B),       // Bright red
        errorContainer = Color(0xFF4D1F1F),
        onErrorContainer = Color(0xFFFFB8B8), // Light red on dark container
        success = Color(0xFF51CF66),     // Bright green
        successContainer = Color(0xFF1B4D23),
        warning = Color(0xFFFFD43B),     // Bright yellow
        warningContainer = Color(0xFF4D3F00),
        info = Color(0xFF4ECDC4),        // Cyan
        infoContainer = Color(0xFF004D4A),

        // Borders and dividers
        divider = Color(0xFF2A3142),
        outline = Color(0xFF3D4459),
        outlineVariant = Color(0xFF525B75),

        // Special colors
        scrim = Color(0xFF000000).copy(alpha = 0.6f),
        inverseSurface = Color(0xFFE8EAED),
        inverseOnSurface = Color(0xFF1A1F2E),
        inversePrimary = Color(0xFF355A9B),

        // Effects
        shimmer = Color(0xFFFFFFFF).copy(alpha = 0.1f),
        ripple = Color(0xFF5E9EFF).copy(alpha = 0.2f),
        shadow = Color(0xFF000000).copy(alpha = 0.3f)
    )
}

object LightTheme {
    val colors = ThemeColors(
        // Clean light backgrounds
        background = Color(0xFFF8FAFB),  // Off-white background
        surface = Color(0xFFFFFFFF),     // Pure white for cards
        surfaceVariant = Color(0xFFF5F5F5), // Slightly darker for elevated cards

        // Professional primary colors
        primary = Color(0xFF2563EB),     // Strong blue
        primaryVariant = Color(0xFF1D4ED8), // Darker blue
        primaryContainer = Color(0xFFDBEAFE), // Light blue container
        onPrimaryContainer = Color(0xFF1E3A5F), // Dark blue on light

        // Accent colors
        secondary = Color(0xFF7C3AED),   // Purple
        secondaryContainer = Color(0xFFEDE9FE), // Light purple
        onSecondaryContainer = Color(0xFF3D2F7A), // Dark purple

        // Tertiary colors
        tertiary = Color(0xFF06B6D4),    // Cyan
        tertiaryContainer = Color(0xFFCFE8FB), // Light cyan

        // Text hierarchy
        textPrimary = Color(0xFF111827), // Almost black
        textSecondary = Color(0xFF4B5563), // Medium gray
        textTertiary = Color(0xFF9CA3AF), // Light gray

        // Surface text
        onBackground = Color(0xFF111827),
        onSurface = Color(0xFF111827),
        onSurfaceVariant = Color(0xFF374151),
        onPrimary = Color(0xFFFFFFFF),   // White on primary
        onSecondary = Color(0xFFFFFFFF), // White on secondary

        // Semantic colors
        error = Color(0xFFDC2626),       // Red
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF7F1D1D), // Dark red on light container
        success = Color(0xFF16A34A),     // Green
        successContainer = Color(0xFFDCFCE7),
        warning = Color(0xFFEAB308),     // Yellow
        warningContainer = Color(0xFFFEF3C7),
        info = Color(0xFF0EA5E9),        // Sky blue
        infoContainer = Color(0xFFE0F2FE),

        // Borders and dividers
        divider = Color(0xFFE5E7EB),
        outline = Color(0xFFD1D5DB),
        outlineVariant = Color(0xFF9CA3AF),

        // Special colors
        scrim = Color(0xFF000000).copy(alpha = 0.3f),
        inverseSurface = Color(0xFF1F2937),
        inverseOnSurface = Color(0xFFF3F4F6),
        inversePrimary = Color(0xFF93BBFC),

        // Effects
        shimmer = Color(0xFF000000).copy(alpha = 0.05f),
        ripple = Color(0xFF2563EB).copy(alpha = 0.1f),
        shadow = Color(0xFF000000).copy(alpha = 0.1f)
    )
}

/**
 * Singleton theme manager for the application
 */
object ThemeManager {
    // Track user's theme preference mode (LIGHT, DARK, or SYSTEM)
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Track the actual theme being applied
    // Default to light theme on startup - will be updated by iOS setIOSTheme() immediately
    private val _currentTheme = MutableStateFlow(
        AppTheme(
            isDarkMode = false,
            colors = LightTheme.colors,
            typography = ThemeTypography(),
            spacing = ThemeSpacing(),
            elevation = ThemeElevation(),
            shapes = ThemeShapes(),
            name = "Light"
        )
    )

    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    /**
     * User manually toggled the theme - toggles between LIGHT and DARK
     * Sets explicit override so system appearance is ignored
     */
    fun toggleTheme() {
        println("ThemeManager: toggleTheme called, current mode = ${_themeMode.value}")

        val newMode = when (_themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> {
                // If in SYSTEM mode, toggle to opposite of current appearance
                if (_currentTheme.value.isDarkMode) ThemeMode.LIGHT else ThemeMode.DARK
            }
        }

        println("ThemeManager: Setting new mode = $newMode")
        setThemeMode(newMode)
    }

    /**
     * Set the theme mode (LIGHT, DARK, or SYSTEM)
     * Note: When setting to SYSTEM mode, you should call updateSystemTheme()
     * immediately after with the current system appearance
     */
    fun setThemeMode(mode: ThemeMode) {
        println("ThemeManager: setThemeMode($mode)")
        _themeMode.value = mode
        saveThemePreference(mode)

        // If mode is LIGHT or DARK, apply immediately
        // If mode is SYSTEM, caller should call updateSystemTheme with current appearance
        when (mode) {
            ThemeMode.LIGHT -> applyTheme(isDark = false)
            ThemeMode.DARK -> applyTheme(isDark = true)
            ThemeMode.SYSTEM -> {
                // Don't apply anything yet - wait for updateSystemTheme call
                println("ThemeManager: Mode set to SYSTEM, waiting for system appearance update")
            }
        }
    }

    /**
     * Called by iOS/Android when system appearance changes
     * Only updates the theme if mode is SYSTEM
     */
    fun updateSystemTheme(isDark: Boolean) {
        println("ThemeManager: updateSystemTheme($isDark), current mode = ${_themeMode.value}")

        if (_themeMode.value == ThemeMode.SYSTEM) {
            println("ThemeManager: Applying system theme (isDark=$isDark)")
            applyTheme(isDark)
        } else {
            println("ThemeManager: Ignoring system theme change, user has override (${_themeMode.value})")
        }
    }

    /**
     * Legacy method for compatibility - sets DARK or LIGHT mode explicitly
     */
    fun setDarkMode(isDark: Boolean) {
        println("ThemeManager: setDarkMode($isDark) - setting explicit mode")
        setThemeMode(if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    /**
     * Internal method to actually apply the theme colors
     */
    private fun applyTheme(isDark: Boolean) {
        println("ThemeManager: applyTheme(isDark=$isDark)")
        _currentTheme.value = if (isDark) {
            AppTheme(
                isDarkMode = true,
                colors = DarkTheme.colors,
                typography = ThemeTypography(),
                spacing = ThemeSpacing(),
                elevation = ThemeElevation(),
                shapes = ThemeShapes(),
                name = "Dark"
            )
        } else {
            AppTheme(
                isDarkMode = false,
                colors = LightTheme.colors,
                typography = ThemeTypography(),
                spacing = ThemeSpacing(),
                elevation = ThemeElevation(),
                shapes = ThemeShapes(),
                name = "Light"
            )
        }
    }

    private fun saveThemePreference(mode: ThemeMode) {
        println("ThemeManager: saveThemePreference($mode)")
        // TODO: Implement persistent storage using DataStore or SharedPreferences
    }

    fun loadThemePreference() {
        println("ThemeManager: loadThemePreference - defaulting to SYSTEM")
        // TODO: Load saved theme preference from storage
        // For now, default to SYSTEM mode to follow device appearance
        // Note: Don't call setThemeMode(SYSTEM) here - just set the mode
        // The actual theme will be applied when iOS calls updateSystemTheme()
        _themeMode.value = ThemeMode.SYSTEM
    }
}

/**
 * Composable to provide theme to the app
 */
@Composable
fun AppThemeProvider(
    content: @Composable (AppTheme) -> Unit
) {
    val theme by ThemeManager.currentTheme.collectAsState()
    CompositionLocalProvider(LocalAppTheme provides theme) {
        content(theme)
    }
}

/**
 * Local composition for accessing theme in any composable
 */
val LocalAppTheme = compositionLocalOf<AppTheme> {
    AppTheme(
        isDarkMode = true,
        colors = DarkTheme.colors,
        typography = ThemeTypography(),
        spacing = ThemeSpacing(),
        elevation = ThemeElevation(),
        shapes = ThemeShapes(),
        name = "Dark"
    )
}

@Composable
fun rememberAppTheme(): AppTheme {
    val theme by ThemeManager.currentTheme.collectAsState()
    return theme
}