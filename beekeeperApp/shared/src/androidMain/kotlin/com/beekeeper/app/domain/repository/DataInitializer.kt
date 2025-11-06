// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/DataInitializer.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.config.FeatureFlags
import com.beekeeper.app.data.MigrationManager
import com.beekeeper.app.data.ObjectBoxMigrationService
import kotlinx.coroutines.*

/**
 * Handles data initialization for the app
 * Controlled by FeatureFlags to determine behavior
 * Supports both SQLDelight (recommended) and ObjectBox (legacy)
 */
class DataInitializer(
    private val objectBoxRepository: ObjectBoxRepository? = null,
    private val migrationService: ObjectBoxMigrationService? = null,
    private val migrationManager: MigrationManager? = null,
    private val sqlDelightRepository: SQLDelightProjectFactoryRepository? = null
) {

    private val initScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Initialize data based on feature flags
     * - Priority: SQLDelight > ObjectBox > In-memory
     * - If useSQLDelight is true: Uses SQLDelight
     * - Else if useObjectBox is true: Uses ObjectBox (Android only)
     * - Else: Uses in-memory repositories
     */
    suspend fun initialize() {
        when {
            FeatureFlags.useSQLDelight -> {
                if (FeatureFlags.enableDebugLogging) {
                    println("💾 Using SQLDelight repositories (KMP-ready)")
                }
                if (sqlDelightRepository == null) {
                    println("❌ SQLDelight repository not initialized!")
                    return
                }
                if (FeatureFlags.clearDatabaseOnLaunch) {
                    clearAndReloadSQLDelight()
                } else {
                    loadIfEmptySQLDelight()
                }
            }
            FeatureFlags.useObjectBox -> {
                if (FeatureFlags.enableDebugLogging) {
                    println("📦 Using ObjectBox repositories (Android only)")
                }
                if (objectBoxRepository == null) {
                    println("❌ ObjectBox repository not initialized!")
                    return
                }
                // Initialize ObjectBox in RepositoryManager
                RepositoryManager.initializeObjectBox(objectBoxRepository)

                if (FeatureFlags.clearDatabaseOnLaunch) {
                    clearAndReloadObjectBox()
                } else {
                    loadIfEmptyObjectBox()
                }
            }
            else -> {
                if (FeatureFlags.enableDebugLogging) {
                    println("📝 Using in-memory repositories (no persistence)")
                }
            }
        }
    }

    // ==================== SQLDelight Methods ====================

    /**
     * Clear SQLDelight database and reload sample data (development mode)
     */
    private suspend fun clearAndReloadSQLDelight() {
        try {
            if (FeatureFlags.enableDebugLogging) {
                println("🗑️ Clearing SQLDelight database...")
            }
            sqlDelightRepository!!.deleteAllFactories()

            if (FeatureFlags.enableDebugLogging) {
                println("📦 Loading sample data to SQLDelight...")
            }

            val factories = SampleProjectsFactory.getAllSampleProjects()
            var savedCount = 0

            factories.forEach { factory ->
                try {
                    sqlDelightRepository.saveFactory(factory, factoryType = "sample")
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
        } catch (e: Exception) {
            println("❌ Error during SQLDelight initialization: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Load sample data to SQLDelight only if database is empty (production mode)
     */
    private suspend fun loadIfEmptySQLDelight() {
        try {
            if (sqlDelightRepository!!.isEmpty()) {
                if (FeatureFlags.enableDebugLogging) {
                    println("📦 SQLDelight database is empty, loading sample data...")
                }
                clearAndReloadSQLDelight()
            } else {
                if (FeatureFlags.enableDebugLogging) {
                    val count = sqlDelightRepository.getFactoryCount()
                    println("✅ SQLDelight database already contains $count projects")
                }
            }
        } catch (e: Exception) {
            println("❌ Error checking SQLDelight database: ${e.message}")
            e.printStackTrace()
        }
    }

    // ==================== ObjectBox Methods (Legacy) ====================

    /**
     * Clear ObjectBox database and reload sample data (development mode)
     */
    private suspend fun clearAndReloadObjectBox() {
        try {
            if (FeatureFlags.enableDebugLogging) {
                println("🗑️ Clearing database (clearDatabaseOnLaunch = true)...")
            }
            objectBoxRepository!!.clearAllData()

            if (FeatureFlags.enableDebugLogging) {
                println("📦 Loading sample data to ObjectBox...")
            }
            val result = migrationManager!!.runFullMigration()

            if (result.success) {
                if (FeatureFlags.enableDebugLogging) {
                    println("✅ Sample data loaded: ${result.totalEntitiesCreated} entities")
                    result.factoryResults.forEach { factoryResult ->
                        println("  ${factoryResult.factoryName}: ${factoryResult.entitiesCreated} entities")
                    }
                }
            } else {
                println("❌ Failed to load sample data: ${result.error}")
            }
        } catch (e: Exception) {
            println("❌ Error during database initialization: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Load sample data to ObjectBox only if database is empty (production mode)
     */
    private suspend fun loadIfEmptyObjectBox() {
        try {
            val needsMigration = migrationManager!!.isMigrationNeeded()

            if (needsMigration) {
                if (FeatureFlags.enableDebugLogging) {
                    println("📦 Database is empty, loading sample data...")
                }
                val result = migrationManager.runFullMigration()

                if (result.success) {
                    if (FeatureFlags.enableDebugLogging) {
                        println("✅ Sample data loaded: ${result.totalEntitiesCreated} entities")
                    }
                } else {
                    println("❌ Failed to load sample data: ${result.error}")
                }
            } else {
                if (FeatureFlags.enableDebugLogging) {
                    println("✅ Database already contains data")
                }
            }
        } catch (e: Exception) {
            println("❌ Error during database initialization: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Initialize asynchronously (non-blocking)
     */
    fun initializeAsync() {
        initScope.launch {
            initialize()
        }
    }

    /**
     * Cancel any ongoing initialization
     */
    fun cancel() {
        initScope.cancel()
    }
}
