// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/SQLDelightProjectFactoryRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.data.sqldelight.CineFillerDatabase
import com.beekeeper.app.domain.factory.ProjectFactory
import com.beekeeper.app.domain.factory.ProjectFactorySerializer
import kotlinx.datetime.Clock

/**
 * SQLDelight repository for ProjectFactory storage
 * Stores complete projects as JSON blobs
 * Ready for backend migration to Postgres JSONB
 */
class SQLDelightProjectFactoryRepository(
    private val database: CineFillerDatabase
) {

    private val queries = database.projectFactoryQueries

    /**
     * Save a project factory
     * Serializes each component separately to avoid 2MB CursorWindow limit
     * Extracts denormalized fields (title, description, project_type) for fast queries
     */
    suspend fun saveFactory(factory: ProjectFactory, factoryType: String = "sample") {
        val now = Clock.System.now().toEpochMilliseconds()

        // Extract denormalized fields from project for fast list queries
        val title = factory.project.title
        val description = factory.project.description
        val projectType = factory.project.type.toString()

        // Serialize each component separately to keep row size manageable
        val projectJson = ProjectFactorySerializer.serializeProject(factory.project)
        val charactersJson = ProjectFactorySerializer.serializeCharacters(factory.characters)
        val storiesJson = ProjectFactorySerializer.serializeStories(factory.stories)
        val scriptsJson = ProjectFactorySerializer.serializeScripts(factory.scripts)
        val storyboardsJson = ProjectFactorySerializer.serializeStoryboards(factory.storyboards)
        val bibleJson = ProjectFactorySerializer.serializeBible(factory.projectBible)
        val publishingJson = ProjectFactorySerializer.serializePublishing(factory.publishingProject)
        val metadataJson = ProjectFactorySerializer.serializeMetadata(factory.metadata)

        queries.insertOrReplace(
            id = factory.projectId,
            project_id = factory.projectId,
            factory_type = factoryType,
            title = title,
            description = description,
            project_type = projectType,
            project_json = projectJson,
            characters_json = charactersJson,
            stories_json = storiesJson,
            scripts_json = scriptsJson,
            storyboards_json = storyboardsJson,
            bible_json = bibleJson,
            publishing_json = publishingJson,
            metadata_json = metadataJson,
            created_at = now,
            updated_at = now
        )
    }

    /**
     * Get all factories
     */
    suspend fun getAllFactories(): List<ProjectFactory> {
        return queries.selectAll()
            .executeAsList()
            .mapNotNull { row ->
                try {
                    ProjectFactorySerializer.deserializeFromComponents(
                        projectJson = row.project_json,
                        charactersJson = row.characters_json,
                        storiesJson = row.stories_json,
                        scriptsJson = row.scripts_json,
                        storyboardsJson = row.storyboards_json,
                        bibleJson = row.bible_json,
                        publishingJson = row.publishing_json,
                        metadataJson = row.metadata_json
                    )
                } catch (e: Exception) {
                    println("⚠️ Failed to deserialize factory ${row.id}: ${e.message}")
                    null
                }
            }
    }

    /**
     * Get factory by project ID
     */
    suspend fun getFactoryByProjectId(projectId: String): ProjectFactory? {
        return queries.selectByProjectId(projectId)
            .executeAsOneOrNull()
            ?.let { row ->
                try {
                    // Deserialize each component separately
                    ProjectFactorySerializer.deserializeFromComponents(
                        projectJson = row.project_json,
                        charactersJson = row.characters_json,
                        storiesJson = row.stories_json,
                        scriptsJson = row.scripts_json,
                        storyboardsJson = row.storyboards_json,
                        bibleJson = row.bible_json,
                        publishingJson = row.publishing_json,
                        metadataJson = row.metadata_json
                    )
                } catch (e: Exception) {
                    println("⚠️ Failed to deserialize factory ${row.id}: ${e.message}")
                    null
                }
            }
    }

    /**
     * Get factories by type (sample, template, user)
     */
    suspend fun getFactoriesByType(type: String): List<ProjectFactory> {
        return queries.selectByType(type)
            .executeAsList()
            .mapNotNull { row ->
                try {
                    ProjectFactorySerializer.deserializeFromComponents(
                        projectJson = row.project_json,
                        charactersJson = row.characters_json,
                        storiesJson = row.stories_json,
                        scriptsJson = row.scripts_json,
                        storyboardsJson = row.storyboards_json,
                        bibleJson = row.bible_json,
                        publishingJson = row.publishing_json,
                        metadataJson = row.metadata_json
                    )
                } catch (e: Exception) {
                    println("⚠️ Failed to deserialize factory ${row.id}: ${e.message}")
                    null
                }
            }
    }

    /**
     * Delete a factory
     */
    suspend fun deleteFactory(projectId: String) {
        queries.deleteById(projectId)
    }

    /**
     * Delete all factories
     */
    suspend fun deleteAllFactories() {
        queries.deleteAll()
    }

    /**
     * Get count of all factories
     */
    suspend fun getFactoryCount(): Long {
        return queries.countAll().executeAsOne()
    }

    /**
     * Get count by type
     */
    suspend fun getFactoryCountByType(type: String): Long {
        return queries.countByType(type).executeAsOne()
    }

    /**
     * Check if database is empty
     */
    suspend fun isEmpty(): Boolean {
        return getFactoryCount() == 0L
    }

    /**
     * Get all factories with basic info only (no JSON parsing)
     * Much faster for list views
     * Returns list of maps with: id, project_id, factory_type, title, description, project_type, created_at, updated_at
     */
    suspend fun getAllFactoriesBasicInfo(): List<Map<String, Any?>> {
        return queries.selectAllWithBasicInfo()
            .executeAsList()
            .map { row ->
                mapOf(
                    "id" to row.id,
                    "project_id" to row.project_id,
                    "factory_type" to row.factory_type,
                    "title" to row.title,
                    "description" to row.description,
                    "project_type" to row.project_type,
                    "created_at" to row.created_at,
                    "updated_at" to row.updated_at
                )
            }
    }

    /**
     * Search factories by title (fast search using denormalized field)
     */
    suspend fun searchByTitle(searchQuery: String): List<ProjectFactory> {
        return queries.searchByTitle(searchQuery)
            .executeAsList()
            .mapNotNull { row ->
                try {
                    ProjectFactorySerializer.deserializeFromComponents(
                        projectJson = row.project_json,
                        charactersJson = row.characters_json,
                        storiesJson = row.stories_json,
                        scriptsJson = row.scripts_json,
                        storyboardsJson = row.storyboards_json,
                        bibleJson = row.bible_json,
                        publishingJson = row.publishing_json,
                        metadataJson = row.metadata_json
                    )
                } catch (e: Exception) {
                    println("⚠️ Failed to deserialize factory ${row.id}: ${e.message}")
                    null
                }
            }
    }

    /**
     * Get factories by project type (fast filter using denormalized field)
     */
    suspend fun getFactoriesByProjectType(projectType: String): List<ProjectFactory> {
        return queries.selectByProjectType(projectType)
            .executeAsList()
            .mapNotNull { row ->
                try {
                    ProjectFactorySerializer.deserializeFromComponents(
                        projectJson = row.project_json,
                        charactersJson = row.characters_json,
                        storiesJson = row.stories_json,
                        scriptsJson = row.scripts_json,
                        storyboardsJson = row.storyboards_json,
                        bibleJson = row.bible_json,
                        publishingJson = row.publishing_json,
                        metadataJson = row.metadata_json
                    )
                } catch (e: Exception) {
                    println("⚠️ Failed to deserialize factory ${row.id}: ${e.message}")
                    null
                }
            }
    }
}
