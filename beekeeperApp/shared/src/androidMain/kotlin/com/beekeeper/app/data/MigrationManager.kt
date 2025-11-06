// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/MigrationManager.kt
package com.beekeeper.app.data

import com.beekeeper.app.domain.repository.ObjectBoxRepository
// Removed local factory imports - migrations no longer needed with API backend
import kotlinx.coroutines.*
import kotlin.reflect.KClass

/**
 * Migration manager for handling ObjectBox data migrations
 * Handles both full migrations and incremental updates
 */
class MigrationManager(
    private val repository: ObjectBoxRepository,
    private val migrationService: ObjectBoxMigrationService
) {
    /**
     * Run migration only if the database is empty
     */
    suspend fun runMigrationIfNeeded(): MigrationResult {
        return withContext(Dispatchers.Default) {
            // Check if migration has already been run
            if (repository.getProjectCount() > 0) {
                println("Database already contains data, skipping migration")
                return@withContext MigrationResult(
                    success = true,
                    factoryResults = emptyList(),
                    totalEntitiesCreated = 0
                )
            }

            // Run full migration
            runFullMigration()
        }
    }

    /**
     * Force run the complete migration (will add to existing data)
     */
    suspend fun runFullMigration(): MigrationResult {
        return withContext(Dispatchers.Default) {
            println("🚀 Starting CineFiller ObjectBox Migration...")

            try {
                val result = migrationService.migrateAllFactories()

                // Log results
                println("✅ Migration Results:")
                println("Success: ${result.success}")
                println("Total Entities Created: ${result.totalEntitiesCreated}")

                result.factoryResults.forEach { factoryResult ->
                    println("  ${factoryResult.factoryName}: ${factoryResult.entitiesCreated} entities")
                    if (!factoryResult.success) {
                        println("    Error: ${factoryResult.error}")
                    }
                }

                // Validate if requested
                if (result.success) {
                    validateAllProjects()
                }

                result
            } catch (e: Exception) {
                println("❌ Migration failed: ${e.message}")
                MigrationResult(
                    success = false,
                    factoryResults = emptyList(),
                    totalEntitiesCreated = 0,
                    error = e.message
                )
            }
        }
    }

    /**
     * Run migration for a specific project factory
     * NOTE: Migrations no longer needed - app uses API backend
     */
    suspend fun runIncrementalMigration(factoryName: String): FactoryMigrationResult {
        return FactoryMigrationResult(
            factoryName = factoryName,
            success = false,
            error = "Migrations disabled - app uses API backend",
            entitiesCreated = 0
        )
    }

    /**
     * Check if migration is needed
     */
    suspend fun isMigrationNeeded(): Boolean {
        val projectCount = repository.getProjectCount()
        return !(projectCount > 0)
    }

    /**
     * Validate all migrated projects
     */
    private suspend fun validateAllProjects() {
        val projects = repository.getAllProjects()
        println("\n📋 Validating ${projects.size} projects...")

        projects.forEach { project ->
            val result = migrationService.validateMigration(project.id)
            if (!result.isValid) {
                println("  ⚠️ Project ${project.title} has validation issues:")
                result.errors.forEach { error ->
                    println("    - ${error.field}: ${error.message}")
                }
            } else {
                println("  ✅ Project ${project.title} validated successfully")
            }
        }
    }

    /**
     * Get migration status
     */
    suspend fun getMigrationStatus(): MigrationStatus {
        return migrationService.getMigrationStatus()
    }
}