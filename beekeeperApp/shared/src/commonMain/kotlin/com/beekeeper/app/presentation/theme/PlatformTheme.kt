// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/theme/PlatformTheme.kt
package com.beekeeper.app.presentation.theme

/**
 * Platform-specific theme toggle that handles user preference tracking.
 * On iOS: Sets user preference flag and toggles theme
 * On Android: Just toggles theme
 */
expect fun toggleThemeWithUserPreference()
