// shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/CharacterAnalysisViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.datetime.Instant

/**
 * ViewModel for Character Analysis Screen with full AI features
 */
class CharacterAnalysisViewModel(
    private val characterRepository: CharacterRepository
) : ViewModel() {

    // State flows
    private val _characters = MutableStateFlow<List<CharacterProfile>>(emptyList())
    val characters: StateFlow<List<CharacterProfile>> = _characters.asStateFlow()

    private val _selectedCharacter = MutableStateFlow<CharacterProfile?>(null)
    val selectedCharacter: StateFlow<CharacterProfile?> = _selectedCharacter.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResults = MutableStateFlow<Map<String, Any>?>(null)
    val analysisResults: StateFlow<Map<String, Any>?> = _analysisResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

    init {
        loadCharacters()
    }

    fun loadCharacters(projectId: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // First try to load from repository
                val repoCharacters = characterRepository.getCharacters(projectId)

                 _characters.value = repoCharacters
            } catch (e: Exception) {
                // On error, show sample characters so screen isn't empty
                println("Error loading characters: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCharacter(character: CharacterProfile) {
        _selectedCharacter.value = character
        // Clear previous analysis when selecting new character
        _analysisResults.value = null
    }

    fun analyzeCharacter(character: CharacterProfile) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisProgress.value = 0f

            try {
                // Simulate AI analysis with progress updates
                for (i in 1..10) {
                    _analysisProgress.value = i * 0.1f
                }

                // Generate comprehensive AI analysis
                _analysisResults.value = mapOf(
                    "insights" to generateDeepInsights(character),
                    "sentimentScore" to calculateSentimentScore(character),
                    "complexity" to calculateComplexity(character),
                    "consistency" to calculateConsistency(character),
                    "arcProgression" to analyzeCharacterArc(character),
                    "relationshipDynamics" to analyzeRelationships(character),
                    "predictedDevelopment" to predictFutureDevelopment(character)
                )

                _analysisProgress.value = 1f
            } catch (e: Exception) {
                println("Error analyzing character: ${e.message}")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    private fun generateDeepInsights(character: CharacterProfile): String {
        return """
            ${character.name} embodies the ${character.archetype} archetype with distinctive characteristics.
            
            PERSONALITY ANALYSIS:
            The character displays ${character.personality?.traits?.size ?: 0} core traits, with 
            ${character.personality?.traits?.firstOrNull()?.name ?: "unknown"} being the most dominant.
            This creates a complex personality matrix that drives narrative tension.
            
            MOTIVATION FRAMEWORK:
            Primary motivations center around ${character.personality?.motivations?.firstOrNull() ?: "self-preservation"}.
            These motivations conflict with fears of ${character.personality?.fears?.firstOrNull() ?: "failure"},
            creating internal conflict essential for character development.
            
            NARRATIVE FUNCTION:
            With ${(character.screenTime * 100).toInt()}% screen time and ${character.dialogueCount} dialogue instances,
            this character serves as a ${if (character.screenTime > 0.5) "primary" else "supporting"} narrative driver.
            
            RELATIONSHIP DYNAMICS:
            ${character.relationships.size} key relationships define the character's social network,
            suggesting ${if (character.relationships.size > 3) "high" else "moderate"} interpersonal complexity.
            
            DEVELOPMENT POTENTIAL:
            Based on current arc position, the character has ${if (character.screenTime < 0.3) "significant" else "moderate"}
            room for growth and transformation in the remaining narrative.
        """.trimIndent()
    }

    private fun calculateSentimentScore(character: CharacterProfile): Float {
        // Analyze emotional valence based on traits and relationships
        val positiveTraits = listOf("Brave", "Kind", "Loyal", "Optimistic", "Compassionate")
        val negativeTraits = listOf("Ruthless", "Cynical", "Selfish", "Aggressive", "Deceitful")

        var score = 0f
        character.personality?.traits?.forEach { trait ->
            when {
                positiveTraits.contains(trait.name) -> score += trait.strength
                negativeTraits.contains(trait.name) -> score -= trait.strength
                else -> score += 0.1f // Neutral traits
            }
        }

        return (score / (character.personality?.traits?.size ?: 1)).coerceIn(-1f, 1f)
    }

    private fun calculateComplexity(character: CharacterProfile): Float {
        val traitComplexity = (character.personality?.traits?.size ?: 0) * 0.1f
        val relationshipComplexity = character.relationships.size * 0.15f
        val motivationComplexity = (character.personality?.motivations?.size ?: 0) * 0.2f
        val fearComplexity = (character.personality?.fears?.size ?: 0) * 0.2f

        return (traitComplexity + relationshipComplexity + motivationComplexity + fearComplexity).coerceIn(0f, 1f)
    }

    private fun calculateConsistency(character: CharacterProfile): Float {
        // Simulate consistency analysis
        return 0.85f + (character.name.hashCode() % 15) * 0.01f
    }

    private fun analyzeCharacterArc(character: CharacterProfile): Map<String, Any> {
        return mapOf(
            "currentPhase" to when (character.screenTime) {
                in 0f..0.3f -> "Introduction"
                in 0.3f..0.6f -> "Development"
                in 0.6f..0.8f -> "Transformation"
                else -> "Resolution"
            },
            "completion" to (character.screenTime * 100).toInt(),
            "nextMilestone" to "Character confrontation with inner fears"
        )
    }

    private fun analyzeRelationships(character: CharacterProfile): List<Map<String, Any>> {
        return character.relationships.map { rel ->
            mapOf(
                "target" to rel.targetCharacterName,
                "type" to rel.relationshipType,
                "strength" to rel.strength,
                "dynamic" to when (rel.relationshipType.uppercase()) {
                    "MENTOR" -> "Guidance and wisdom"
                    "RIVAL" -> "Competition and growth"
                    "ROMANTIC" -> "Emotional connection"
                    "ENEMY" -> "Conflict and opposition"
                    else -> "Complex interaction"
                }
            )
        }
    }

    private fun predictFutureDevelopment(character: CharacterProfile): String {
        return when (character.archetype) {
            "The Hero" -> "Likely to face ultimate test of courage and sacrifice"
            "The Mentor" -> "May reveal hidden knowledge or make ultimate sacrifice"
            "The Shadow" -> "Will challenge protagonist's core beliefs"
            else -> "Character arc will evolve based on narrative needs"
        }
    }
}