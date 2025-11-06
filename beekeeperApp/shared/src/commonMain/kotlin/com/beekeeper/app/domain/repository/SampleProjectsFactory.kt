// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/SampleProjectsFactory.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
// Removed local factory imports - app uses API backend
import com.beekeeper.app.domain.factory.ProjectFactory

/**
 * DEPRECATED: Use ProjectFactory instead
 * Keeping for backward compatibility
 */
@Deprecated(
    message = "Use ProjectFactory instead",
    replaceWith = ReplaceWith("ProjectFactory", "com.beekeeper.app.domain.factory.ProjectFactory")
)
typealias SampleProjectData = ProjectFactory

/**
 * Main factory for creating sample projects with all related data
 * Delegates to individual project factories to avoid context window issues
 *
 * NOTE: Returns ProjectFactory instances (SampleProjectData is deprecated alias)
 * Use this for sample/demo data - can serialize to JSON for storage
 */
object SampleProjectsFactory {

    /**
     * Get all sample projects with their complete data as ProjectFactory instances
     * NOTE: Returns empty list - app uses API backend for data
     */
    fun getAllSampleProjects(): List<ProjectFactory> {
        return emptyList()
    }

    /**
     * Get a specific project by ID
     */
    fun getProjectById(projectId: String): ProjectFactory? {
        return getAllSampleProjects().find { it.project.id == projectId }
    }

    /**
     * Get all characters across all projects
     */
    fun getAllCharacters(): List<CharacterProfile> {
        return getAllSampleProjects().flatMap { it.characters }
    }

    /**
     * Get all stories across all projects
     */
    fun getAllStories(): List<Story> {
        return getAllSampleProjects().flatMap { it.stories }
    }

    /**
     * Get all scripts across all projects
     */
    fun getAllScripts(): List<Script> {
        return getAllSampleProjects().flatMap { it.scripts }
    }

    /**
     * Get all storyboards across all projects
     */
    fun getAllStoryboards(): List<Storyboard> {
        return getAllSampleProjects().flatMap { it.storyboards }
    }
    
    /**
     * Get all publishing projects
     */
    fun getAllPublishingProjects(): List<PublishingProject> {
        return getAllSampleProjects().mapNotNull { it.publishingProject }
    }
    
    /**
     * Get projects by status
     */
    fun getProjectsByStatus(status: ProjectStatus): List<CreativeProject> {
        return getAllSampleProjects()
            .map { it.project }
            .filter { it.status == status }
    }
    
    /**
     * Get projects by type
     */
    fun getProjectsByType(type: ProjectType): List<CreativeProject> {
        return getAllSampleProjects()
            .map { it.project }
            .filter { it.type == type }
    }
    
    /**
     * Get characters by project
     */
    fun getCharactersByProject(projectId: String): List<CharacterProfile> {
        return getProjectById(projectId)?.characters ?: emptyList()
    }
    
    /**
     * Get stories by project
     */
    fun getStoriesByProject(projectId: String): List<Story> {
        return getProjectById(projectId)?.stories ?: emptyList()
    }
    
    /**
     * Get scripts by project
     */
    fun getScriptsByProject(projectId: String): List<Script> {
        return getProjectById(projectId)?.scripts ?: emptyList()
    }
    
    /**
     * Get storyboards by project
     */
    fun getStoryboardsByProject(projectId: String): List<Storyboard> {
        return getProjectById(projectId)?.storyboards ?: emptyList()
    }
}
