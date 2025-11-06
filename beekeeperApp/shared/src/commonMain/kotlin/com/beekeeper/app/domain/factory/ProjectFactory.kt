// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/factory/ProjectFactory.kt
package com.beekeeper.app.domain.factory

import com.beekeeper.app.domain.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Factory data structure for complete projects
 * Can serialize/deserialize to JSON for storage and backend sync
 *
 * This is the permanent data structure for project templates/factories
 * Used for:
 * - Sample/demo projects
 * - Project templates
 * - Importing/exporting projects
 * - Backend synchronization
 *
 * V2 Architecture:
 * - Supports both legacy projectBible and canonical bibleOverview/episodeBlueprints
 * - Backend stores these in separate columns in project_factory_v2 table
 * - bibleOverview: Simpler, flatter structure for world rules and production guidelines
 * - episodeBlueprints: Episode planning/design phase (before Story production)
 */
@Serializable
data class ProjectFactory(
    val project: CreativeProject,
    val characters: List<CharacterProfile> = emptyList(),
    val stories: List<Story> = emptyList(),
    val scripts: List<Script> = emptyList(),
    val storyboards: List<Storyboard> = emptyList(),
    val publishingProject: PublishingProject? = null,

    // Legacy ProjectBible (backward compatibility)
    val projectBible: ProjectBible? = null,

    // V2 Canonical Fields (preferred)
    val bibleOverview: BibleOverview? = null,
    val episodeBlueprints: List<EpisodeBlueprintCanonical> = emptyList(),

    val metadata: ProjectFactoryMetadata = ProjectFactoryMetadata()
) {
    /**
     * Serialize to JSON string
     */
    fun toJson(): String {
        return jsonSerializer.encodeToString(this)
    }

    /**
     * Get project ID
     */
    val projectId: String
        get() = project.id

    /**
     * Get project title
     */
    val title: String
        get() = project.title

    /**
     * Count total entities in this factory
     */
    fun getTotalEntities(): Int {
        return 1 + // project
                characters.size +
                stories.size +
                scripts.size +
                storyboards.size +
                (if (publishingProject != null) 1 else 0)
    }

    companion object {
        private val jsonSerializer = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        /**
         * Deserialize from JSON string
         */
        fun fromJson(json: String): ProjectFactory {
            return jsonSerializer.decodeFromString(json)
        }

        /**
         * Create empty factory for a project
         */
        fun createEmpty(project: CreativeProject): ProjectFactory {
            return ProjectFactory(
                project = project,
                metadata = ProjectFactoryMetadata(
                    isTemplate = false,
                    isSample = false
                )
            )
        }

        /**
         * Create sample/demo factory
         */
        fun createSample(
            project: CreativeProject,
            characters: List<CharacterProfile> = emptyList(),
            stories: List<Story> = emptyList(),
            scripts: List<Script> = emptyList(),
            storyboards: List<Storyboard> = emptyList(),
            publishingProject: PublishingProject? = null,
            projectBible: ProjectBible? = null,
            bibleOverview: BibleOverview? = null,
            episodeBlueprints: List<EpisodeBlueprintCanonical> = emptyList()
        ): ProjectFactory {
            return ProjectFactory(
                project = project,
                characters = characters,
                stories = stories,
                scripts = scripts,
                storyboards = storyboards,
                publishingProject = publishingProject,
                projectBible = projectBible,
                bibleOverview = bibleOverview,
                episodeBlueprints = episodeBlueprints,
                metadata = ProjectFactoryMetadata(
                    isTemplate = false,
                    isSample = true
                )
            )
        }

        /**
         * Create template factory
         */
        fun createTemplate(
            project: CreativeProject,
            characters: List<CharacterProfile> = emptyList(),
            stories: List<Story> = emptyList(),
            scripts: List<Script> = emptyList(),
            storyboards: List<Storyboard> = emptyList(),
            publishingProject: PublishingProject? = null,
            projectBible: ProjectBible? = null,
            bibleOverview: BibleOverview? = null,
            episodeBlueprints: List<EpisodeBlueprintCanonical> = emptyList()
        ): ProjectFactory {
            return ProjectFactory(
                project = project,
                characters = characters,
                stories = stories,
                scripts = scripts,
                storyboards = storyboards,
                publishingProject = publishingProject,
                projectBible = projectBible,
                bibleOverview = bibleOverview,
                episodeBlueprints = episodeBlueprints,
                metadata = ProjectFactoryMetadata(
                    isTemplate = true,
                    isSample = false
                )
            )
        }
    }
}

/**
 * Metadata about the project factory
 */
@Serializable
data class ProjectFactoryMetadata(
    val isTemplate: Boolean = false,
    val isSample: Boolean = false,
    val version: String = "1.0",
    val createdBy: String? = null,
    val tags: List<String> = emptyList(),
    val description: String? = null
)

/**
 * Result of factory operations
 */
sealed class ProjectFactoryResult {
    data class Success(val factory: ProjectFactory) : ProjectFactoryResult()
    data class Error(val message: String, val cause: Throwable? = null) : ProjectFactoryResult()
}

/**
 * Extension functions for collections of factories
 */
fun List<ProjectFactory>.toJsonArray(): String {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    return json.encodeToString(this)
}

fun String.toProjectFactoryList(): List<ProjectFactory> {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    return json.decodeFromString(this)
}
