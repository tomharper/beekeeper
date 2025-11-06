// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/StoryPatternsRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing story patterns
 * Handles pattern analysis, generation, and application
 */
interface StoryPatternsRepository {
    
    // Pattern retrieval
    suspend fun getPatterns(projectId: String): List<StoryPattern>
    suspend fun getPattern(patternId: String): StoryPattern?
    suspend fun getPatternsByCategory(category: String): List<StoryPattern>
    suspend fun generateSamplePatterns(): List<StoryPattern>
    
    // Pattern analysis
    suspend fun analyzeStoryPatterns(storyId: String): StoryPatternAnalysis
    suspend fun analyzeProjectPatterns(projectId: String): ProjectPatternAnalysis
    suspend fun detectPatterns(story: Story): List<DetectedPattern>
    
    // Pattern application
    suspend fun applyPattern(storyId: String, patternId: String): PatternApplicationResult
    suspend fun suggestPatternImprovements(storyId: String, patternId: String): List<PatternSuggestion>
    
    // Pattern management
    suspend fun createCustomPattern(pattern: StoryPattern): StoryPattern
    suspend fun updatePattern(pattern: StoryPattern): StoryPattern
    suspend fun deletePattern(patternId: String): Boolean
    
    // Real-time updates
    fun observePatterns(projectId: String): Flow<List<StoryPattern>>
}

// Supporting data classes
data class StoryPatternAnalysis(
    val storyId: String,
    val detectedPatterns: List<DetectedPattern>,
    val dominantPattern: StoryPattern?,
    val patternConfidence: Float,
    val suggestions: List<PatternSuggestion>
)

data class ProjectPatternAnalysis(
    val projectId: String,
    val commonPatterns: List<PatternFrequency>,
    val patternDistribution: Map<String, Int>,
    val recommendations: List<String>
)

data class DetectedPattern(
    val pattern: StoryPattern,
    val confidence: Float,
    val matchedBeats: List<StoryBeat>,
    val missingBeats: List<StoryBeat>
)

data class PatternFrequency(
    val pattern: StoryPattern,
    val frequency: Int,
    val stories: List<String>
)

data class PatternApplicationResult(
    val success: Boolean,
    val updatedStory: Story?,
    val changes: List<PatternChange>,
    val message: String
)

data class PatternChange(
    val type: ChangeType,
    val description: String,
    val location: String
)

enum class ChangeType {
    STRUCTURE, PACING, CHARACTER_ARC, DIALOGUE, THEME
}

data class PatternSuggestion(
    val type: SuggestionType,
    val description: String,
    val impact: ImpactLevel,
    val implementation: String
)

enum class SuggestionType {
    ADD_BEAT, REMOVE_BEAT, ADJUST_PACING, ENHANCE_CONFLICT, CLARIFY_THEME
}

enum class ImpactLevel {
    LOW, MEDIUM, HIGH
}
