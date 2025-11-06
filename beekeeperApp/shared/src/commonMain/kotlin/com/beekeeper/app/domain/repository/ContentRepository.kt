// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/ContentRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing all content-related data
 * Uses models from domain.model package - no duplicate definitions
 */
interface ContentRepository {

    // Story Management
    suspend fun getStories(projectId: String): List<Story>
    suspend fun getStory(storyId: String): Story?
    suspend fun createStory(story: Story): Story
    suspend fun updateStory(story: Story): Story
    suspend fun deleteStory(storyId: String): Boolean
    suspend fun getStoryPatterns(projectId: String): List<StoryPattern>
    suspend fun analyzeStoryStructure(storyId: String): StoryAnalysis

    // Script Management
    suspend fun getScripts(projectId: String): List<Script>
    suspend fun getScript(scriptId: String): Script?
    suspend fun createScript(script: Script): Script
    suspend fun updateScript(script: Script): Script
    suspend fun deleteScript(scriptId: String): Boolean
    suspend fun extractCharactersFromScript(scriptId: String): List<CharacterProfile>
    suspend fun analyzeScriptDialogue(scriptId: String): DialogueAnalysis
    suspend fun getScriptVersionHistory(scriptId: String): List<ScriptVersion>

    // Storyboard Management
    suspend fun getStoryboards(projectId: String): List<Storyboard>
    suspend fun getStoryboard(storyboardId: String): Storyboard?
    suspend fun createStoryboard(storyboard: Storyboard): Storyboard
    suspend fun updateStoryboard(storyboard: Storyboard): Storyboard
    suspend fun deleteStoryboard(storyboardId: String): Boolean
    suspend fun getStoryboardScenes(storyboardId: String): List<Scene>
    suspend fun updateScene(scene: Scene): Scene
    suspend fun generateStoryboardFromScript(scriptId: String): Storyboard

    // Content Quality Review
    suspend fun getQualityReviews(projectId: String): List<QualityReview>
    suspend fun createQualityReview(review: QualityReview): QualityReview
    suspend fun updateQualityReview(review: QualityReview): QualityReview
    suspend fun getReviewMetrics(contentId: String): QualityMetrics
    suspend fun submitForReview(contentId: String, contentType: ContentType): QualityReview

    // Content Search and Filtering
    suspend fun searchContent(query: String, filters: ContentFilters): List<ContentInterface>
    suspend fun getRecentContent(projectId: String, limit: Int = 10): List<ContentInterface>
    suspend fun getContentByTags(tags: List<String>): List<ContentInterface>

    // Real-time updates (using Flow for reactive updates)
    fun observeStories(projectId: String): Flow<List<Story>>
    fun observeScripts(projectId: String): Flow<List<Script>>
    fun observeStoryboards(projectId: String): Flow<List<Storyboard>>
}
