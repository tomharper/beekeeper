// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/config/FeatureFlags.kt
package com.beekeeper.app.config

/**
 * Feature flags for CineFiller
 * Toggle features on/off for testing and development
 */
object FeatureFlags {

    /**
     * Use SQLDelight database for data persistence (RECOMMENDED)
     * When true, uses SQLDelight (true KMP, works on all platforms)
     * Takes precedence over useObjectBox
     */
    var useSQLDelight: Boolean = true

    /**
     * Use ObjectBox database for data persistence (DEPRECATED - Android only)
     * When false, uses in-memory repositories (faster, no persistence)
     * When true, uses ObjectBox (persistent, Android only)
     * NOTE: Ignored if useSQLDelight is true
     */
    var useObjectBox: Boolean = false

    /**
     * Enable debug logging
     */
    var enableDebugLogging: Boolean = true

    /**
     * Clear database on each launch (applies to both ObjectBox and SQLDelight)
     */
    var clearDatabaseOnLaunch: Boolean = false

    /**
     * Enable AI features (character generation, dialogue suggestions, etc.)
     */
    var enableAIFeatures: Boolean = false

    /**
     * Enable experimental wizard flows
     */
    var enableWizardFlows: Boolean = true

    /**
     * Print feature flag status
     */
    fun printStatus() {
        println("🚩 Feature Flags:")
        println("  useSQLDelight: $useSQLDelight")
        println("  useObjectBox: $useObjectBox ${if (useSQLDelight) "(ignored - SQLDelight active)" else ""}")
        println("  enableDebugLogging: $enableDebugLogging")
        println("  clearDatabaseOnLaunch: $clearDatabaseOnLaunch")
        println("  enableAIFeatures: $enableAIFeatures")
        println("  enableWizardFlows: $enableWizardFlows")
    }
}
