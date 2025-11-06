// File: shared/src/androidMain/kotlin/com/cinefiller/fillerapp/presentation/theme/PlatformTheme.kt
package com.beekeeper.app.presentation.theme

/**
 * Android implementation - just toggles theme (no user preference tracking needed)
 */
actual fun toggleThemeWithUserPreference() {
    ThemeManager.toggleTheme()
}
