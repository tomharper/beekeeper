// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/ScriptRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Scripts and their relationships with Projects, Stories, and Scenes
 * Maintains the critical links between all narrative elements within a project context
 */
interface ScriptRepository {
    
    // ===== Core Script Operations =====
    suspend fun getScript(scriptId: String): Script?
    suspend fun getScriptsByProject(projectId: String): List<Script>
    suspend fun getScriptsByStory(storyId: String): List<Script>
    suspend fun createScript(script: Script): Script
    suspend fun updateScript(script: Script): Script
    suspend fun deleteScript(scriptId: String): Boolean
    
    // ===== Scene Management =====
    suspend fun getScriptScenes(scriptId: String): List<SceneScript>
    suspend fun generateScriptScenes(scriptId: String): List<SceneScript>
    suspend fun updateScriptScene(scene: SceneScript): SceneScript
    suspend fun deleteScriptScene(sceneId: String): Boolean
    suspend fun reorderScenes(scriptId: String, sceneIds: List<String>): List<SceneScript>
    
    // ===== Story-Script Relationships =====
    suspend fun linkScriptToStory(scriptId: String, storyId: String): Boolean
    suspend fun unlinkScriptFromStory(scriptId: String, storyId: String): Boolean
    suspend fun getScriptWithStory(scriptId: String): Pair<Script, Story?>?
    
    // ===== Project Context Operations =====
    suspend fun getProjectScriptTree(projectId: String): ProjectScriptTree
    suspend fun generateScriptFromStory(storyId: String, projectId: String): Script
    suspend fun extractCharactersFromScript(scriptId: String): List<String>
    
    // ===== Script Text Generation =====
    suspend fun generateScriptText(scene: SceneScript): String
    suspend fun generateFullScriptText(scriptId: String): String
    suspend fun exportScriptToFormat(scriptId: String, format: ScriptExportFormat): String
    
    // ===== Analysis and Metrics =====
    suspend fun analyzeScriptStructure(scriptId: String): ScriptAnalysis
    suspend fun getScriptMetrics(scriptId: String): ScriptMetrics
    suspend fun analyzeDialogue(scriptId: String): DialogueAnalysis
    
    // ===== Real-time Updates =====
    fun observeProjectScripts(projectId: String): Flow<List<Script>>
    fun observeScriptScenes(scriptId: String): Flow<List<SceneScript>>
    fun observeScriptUpdates(scriptId: String): Flow<Script?>
}

// Supporting data classes
data class ProjectScriptTree(
    val projectId: String,
    val projectTitle: String,
    val stories: List<StoryScriptRelation>,
    val orphanScripts: List<Script> // Scripts not linked to any story
)

data class StoryScriptRelation(
    val story: Story,
    val script: Script?,
    val drafts: List<Script>,
)

data class ScriptMetrics(
    val totalScenes: Int,
    val totalWords: Int,
    val estimatedDuration: Int, // in seconds
    val charactersCount: Int,
    val dialoguePercentage: Float,
    val completionPercentage: Float
)

enum class ScriptExportFormat {
    FOUNTAIN,
    PDF,
    FINAL_DRAFT,
    CELTX,
    PLAIN_TEXT
}
