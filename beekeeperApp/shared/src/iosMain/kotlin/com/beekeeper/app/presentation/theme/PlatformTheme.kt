// File: shared/src/iosMain/kotlin/com/cinefiller/fillerapp/presentation/theme/PlatformTheme.kt
package com.beekeeper.app.presentation.theme

import com.beekeeper.app.userToggledTheme

/**
 * iOS implementation - calls userToggledTheme() which sets the user preference flag
 */
actual fun toggleThemeWithUserPreference() {
    userToggledTheme()
}
