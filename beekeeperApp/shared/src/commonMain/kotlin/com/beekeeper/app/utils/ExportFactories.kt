// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/utils/ExportFactories.kt
package com.beekeeper.app.utils

import com.beekeeper.app.domain.repository.SampleProjectsFactory
import com.beekeeper.app.domain.factory.ProjectFactory
import com.beekeeper.app.domain.factory.toJsonArray

/**
 * Utility to export all project factories to JSON
 * Uses the built-in ProjectFactory.toJson() serialization
 */
object FactoryExporter {

    /**
     * Export all sample project factories to JSON strings
     * Returns a map of project_id -> JSON string
     */
    fun exportAllFactories(): Map<String, String> {
        val factories = SampleProjectsFactory.getAllSampleProjects()

        return factories.associate { factory ->
            factory.projectId to factory.toJson()
        }
    }

    /**
     * Export all factories as a single JSON array
     */
    fun exportFactoriesAsArray(): String {
        val factories = SampleProjectsFactory.getAllSampleProjects()
        return factories.toJsonArray()
    }

    /**
     * Export a specific factory by project ID
     */
    fun exportFactoryById(projectId: String): String? {
        val factory = SampleProjectsFactory.getProjectById(projectId)
        return factory?.toJson()
    }

    /**
     * Get all factory metadata (without full content)
     */
    fun getFactoryMetadata(): List<FactoryMetadata> {
        return SampleProjectsFactory.getAllSampleProjects().map { factory ->
            FactoryMetadata(
                projectId = factory.projectId,
                title = factory.title,
                entityCount = factory.getTotalEntities(),
                characterCount = factory.characters.size,
                storyCount = factory.stories.size,
                scriptCount = factory.scripts.size,
                storyboardCount = factory.storyboards.size,
                hasPublishingProject = factory.publishingProject != null
            )
        }
    }

    /**
     * Print all factories to console (for debugging/export)
     */
    fun printAllFactories() {
        val factories = SampleProjectsFactory.getAllSampleProjects()

        println("="  * 80)
        println("Exporting ${factories.size} Project Factories")
        println("=" * 80)
        println()

        factories.forEach { factory ->
            println("Project: ${factory.title}")
            println("ID: ${factory.projectId}")
            println("Entities: ${factory.getTotalEntities()}")
            println("JSON Length: ${factory.toJson().length} characters")
            println()
        }
    }
}

/**
 * Metadata about a factory (lightweight summary)
 */
data class FactoryMetadata(
    val projectId: String,
    val title: String,
    val entityCount: Int,
    val characterCount: Int,
    val storyCount: Int,
    val scriptCount: Int,
    val storyboardCount: Int,
    val hasPublishingProject: Boolean
)

private operator fun String.times(n: Int) = repeat(n)
