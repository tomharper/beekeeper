// File: shared/src/iosMain/kotlin/com/cinefiller/fillerapp/IOSHelper.kt
package com.beekeeper.app

import com.beekeeper.app.config.FeatureFlags
import com.beekeeper.app.data.sqldelight.CineFillerDatabase
import com.beekeeper.app.data.sqldelight.DatabaseDriverFactory
import com.beekeeper.app.domain.repository.RepositoryManager
import com.beekeeper.app.domain.repository.SQLDelightProjectFactoryRepository
import com.beekeeper.app.domain.repository.SampleProjectsFactory
import kotlinx.coroutines.*

// Track the last known system appearance from iOS
private var lastKnownSystemIsDark = false

// Callback to notify SwiftUI when theme changes
private var themeChangeCallback: (() -> Unit)? = null

/**
 * Register a callback from SwiftUI to be notified of theme changes
 */
fun setThemeChangeCallback(callback: () -> Unit) {
    themeChangeCallback = callback
}

/**
 * Called by iOS when system appearance changes
 * ThemeManager will decide whether to apply based on current ThemeMode
 */
fun setIOSTheme(isDarkMode: Boolean) {
    println("IOSHelper: setIOSTheme called with isDarkMode=$isDarkMode")
    lastKnownSystemIsDark = isDarkMode
    com.beekeeper.app.presentation.theme.ThemeManager.updateSystemTheme(isDarkMode)
    themeChangeCallback?.invoke()
}

/**
 * User manually toggled the theme via UI button
 * This sets an explicit LIGHT or DARK preference, overriding system appearance
 */
fun userToggledTheme() {
    println("IOSHelper: userToggledTheme called")
    com.beekeeper.app.presentation.theme.ThemeManager.toggleTheme()
    themeChangeCallback?.invoke()
}

/**
 * Get the current theme dark mode state
 */
fun isCurrentThemeDark(): Boolean {
    return com.beekeeper.app.presentation.theme.ThemeManager.currentTheme.value.isDarkMode
}

/**
 * iOS initialization helper
 * This must be called before launching the app to initialize the database
 */
fun initializeIOS() {
    // Print feature flags on startup
    FeatureFlags.printStatus()

    // Initialize SQLDelight database
    val driver = DatabaseDriverFactory().createDriver()
    val database = CineFillerDatabase(driver)
    val repository = SQLDelightProjectFactoryRepository(database)

    // Register with RepositoryManager
    RepositoryManager.initializeSQLDelight(repository, database)

    // Initialize data asynchronously (load sample projects if empty)
    if (FeatureFlags.useSQLDelight) {
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            try {
                if (repository.isEmpty()) {
                    if (FeatureFlags.enableDebugLogging) {
                        println("📦 SQLDelight database is empty, loading sample data...")
                    }

                    val factories = SampleProjectsFactory.getAllSampleProjects()
                    var savedCount = 0

                    factories.forEach { factory ->
                        try {
                            repository.saveFactory(factory, factoryType = "sample")
                            savedCount++
                            if (FeatureFlags.enableDebugLogging) {
                                println("  ✅ ${factory.title}: ${factory.getTotalEntities()} entities")
                            }
                        } catch (e: Exception) {
                            println("  ❌ Failed to save ${factory.title}: ${e.message}")
                        }
                    }

                    if (FeatureFlags.enableDebugLogging) {
                        println("✅ SQLDelight migration complete: $savedCount projects loaded")
                    }
                } else {
                    if (FeatureFlags.enableDebugLogging) {
                        val count = repository.getFactoryCount()
                        println("✅ SQLDelight database already contains $count projects")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error during iOS initialization: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    if (FeatureFlags.enableDebugLogging) {
        println("✅ iOS initialization complete")
    }
}
