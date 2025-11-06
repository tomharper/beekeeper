// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/ObjectBoxMigrationService.kt
package com.beekeeper.app.data

import com.beekeeper.app.domain.repository.*
// Removed local factory imports - migrations no longer needed with API backend
import com.beekeeper.app.domain.model.*
import io.objectbox.BoxStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * Service to migrate factory data to ObjectBox database
 * Preserves all existing relationships and adds image URL support
 */
class ObjectBoxMigrationService(
    private val boxStore: BoxStore,
    private val repository: ObjectBoxRepository = ObjectBoxRepository(boxStore)
) {

    private val migrationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Migrate all factory data to ObjectBox
     * NOTE: Migrations disabled - app uses API backend
     */
    suspend fun migrateAllFactories(): MigrationResult {
        return MigrationResult(
            success = false,
            error = "Migrations disabled - app uses API backend",
            factoryResults = emptyList(),
            totalEntitiesCreated = 0
        )
    }

    /**
     * Migrate a single project factory
     * NOTE: Migrations disabled - app uses API backend
     */
    suspend fun migrateProjectFactory(factoryClass: KClass<*>): FactoryMigrationResult {
        return FactoryMigrationResult(
            factoryName = factoryClass.simpleName ?: "Unknown",
            success = false,
            error = "Migrations disabled - app uses API backend",
            entitiesCreated = 0
        )
    }

    /**
     * Validate migrated data
     */
    suspend fun validateMigration(projectId: String): ValidationResult {
        val project = repository.getProject(projectId)
        if (project == null) {
            return ValidationResult(
                isValid = false,
                errors = listOf(ValidationError(
                    field = "project",
                    message = "Project not found: $projectId",
                    code = "PROJECT_NOT_FOUND"
                ))
            )
        }

        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()
        val suggestions = mutableListOf<String>()

        // Validate characters
        val characters = repository.getCharactersForProject(projectId)
        if (characters.isEmpty()) {
            warnings.add(ValidationWarning(
                field = "characters",
                message = "No characters found for project",
                severity = WarningSeverity.LOW
            ))
        }

        // Validate stories
        val stories = repository.getStoriesForProject(projectId)
        if (stories.isEmpty()) {
            errors.add(ValidationError(
                field = "stories",
                message = "No stories found for project",
                severity = ValidationSeverity.ERROR
            ))
        }

        // Validate scripts
        val scripts = repository.getScriptsForProject(projectId)
        if (scripts.isEmpty()) {
            errors.add(ValidationError(
                field = "scripts",
                message = "No scripts found for project",
                severity = ValidationSeverity.ERROR
            ))
        }

        // Validate frame-dialogue relationships
        val frames = repository.getAllFramesForProject(projectId) as List<Frame>
        frames.forEach { frame ->
            repository.getDialogueLine(frame?.dialogueLineId!!)
            if (frame.dialogueLineId == null) {
                errors.add(
                    ValidationError(
                        field = "frame.dialogueLineId",
                        message = "Frame ${frame.id} has no dialogue",
                        severity = ValidationSeverity.WARNING
                    )
                )
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            suggestions = suggestions
        )
    }

    /**
     * Get migration status
     */
    suspend fun getMigrationStatus(): MigrationStatus {
        val totalProjects = repository.getAllProjects()

        return MigrationStatus(
            totalFactories = 11, // Number of known project factories
            migratedFactories = totalProjects.size,
            totalProjects = totalProjects.size,
            lastMigration = Clock.System.now().toString()
        )
    }

    /**
     * Clean up and close resources
     */
    fun close() {
        migrationScope.cancel()
        boxStore.close()
    }
}

/**
 * Data classes for migration results
 */
@Serializable
data class MigrationResult(
    val success: Boolean,
    val factoryResults: List<FactoryMigrationResult>,
    val totalEntitiesCreated: Int,
    val error: String? = null
)

@Serializable
data class FactoryMigrationResult(
    val factoryName: String,
    val success: Boolean,
    val entitiesCreated: Int,
    val error: String? = null
)

@Serializable
data class FrameImageUpdate(
    val frameId: String,
    val imageUrl: String
)

@Serializable
data class UpdateResult(
    val success: Boolean,
    val updatedCount: Int,
    val error: String? = null
)

@Serializable
data class MigrationStatus(
    val totalFactories: Int,
    val migratedFactories: Int,
    val totalProjects: Int,
    val lastMigration: String?
)