package com.beekeeper.app.presentation.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.repository.StoryPatternAnalysis
import com.beekeeper.app.domain.repository.StoryPatternsRepository
import com.beekeeper.app.domain.repository.StoryPatternsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoryPatternsUiState(
    val patterns: List<StoryPattern> = emptyList(),
    val selectedPattern: StoryPattern? = null,
    val appliedPatterns: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val analysisResult: StoryPatternAnalysis? = null
)

class StoryPatternsViewModel(
    private val projectId: String,
    private val repository: StoryPatternsRepository = StoryPatternsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryPatternsUiState())
    val uiState: StateFlow<StoryPatternsUiState> = _uiState.asStateFlow()

    init {
        loadPatterns()
    }

    fun loadPatterns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val patterns = repository.generateSamplePatterns() + getDefaultPatterns()
                _uiState.update {
                    it.copy(
                        patterns = patterns,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load patterns: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectPattern(pattern: StoryPattern) {
        _uiState.update { it.copy(selectedPattern = pattern) }
    }

    fun applyPattern(patternId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    appliedPatterns = it.appliedPatterns + patternId
                )
            }
            // In a real implementation, this would apply the pattern to the current story
        }
    }

    fun analyzeStory(storyId: String) {
        viewModelScope.launch {
            try {
                val analysis = repository.analyzeStoryPatterns(storyId)
                _uiState.update { it.copy(analysisResult = analysis) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Analysis failed: ${e.message}")
                }
            }
        }
    }

    private fun getDefaultPatterns(): List<StoryPattern> {
        return listOf(
            StoryPattern(
                id = "1",
                name = "Three-Act Structure",
                description = "Classic beginning-middle-end narrative structure",
                frequency = 12,
                confidence = 0.85f,
                examples = listOf(
                    "Opening establishes world and characters",
                    "Midpoint twist changes protagonist's goal"
                ),
                category = PatternCategory.NARRATIVE_STRUCTURE,
                icon = Icons.Default.ViewAgenda,
                structure = PatternStructure(
                    type = "three-act",
                    beats = listOf(
                        StoryBeat("Setup", 0.0f, "Establish world and characters"),
                        StoryBeat("Rising Action", 0.25f, "Build conflict"),
                        StoryBeat("Midpoint", 0.5f, "Major reversal"),
                        StoryBeat("Climax", 0.75f, "Peak conflict"),
                        StoryBeat("Resolution", 0.9f, "New equilibrium")
                    )
                )
            ),
            StoryPattern(
                id = "2",
                name = "Hero's Journey",
                description = "Protagonist undergoes transformation through challenges",
                frequency = 8,
                confidence = 0.78f,
                examples = listOf(
                    "Call to adventure disrupts ordinary world",
                    "Mentor provides guidance"
                ),
                category = PatternCategory.CHARACTER_ARC,
                icon = Icons.Default.Person,
                structure = PatternStructure(
                    type = "hero-journey",
                    beats = listOf(
                        StoryBeat("Ordinary World", 0.0f, "Hero's normal life"),
                        StoryBeat("Call to Adventure", 0.1f, "Disruption occurs"),
                        StoryBeat("Refusal of Call", 0.15f, "Initial hesitation"),
                        StoryBeat("Meeting Mentor", 0.2f, "Guidance received"),
                        StoryBeat("Crossing Threshold", 0.25f, "Enter new world"),
                        StoryBeat("Tests & Allies", 0.4f, "Challenges faced"),
                        StoryBeat("Approach", 0.6f, "Prepare for ordeal"),
                        StoryBeat("Ordeal", 0.7f, "Major crisis"),
                        StoryBeat("Reward", 0.8f, "Victory achieved"),
                        StoryBeat("The Road Back", 0.85f, "Return journey"),
                        StoryBeat("Resurrection", 0.9f, "Final test"),
                        StoryBeat("Return with Elixir", 0.95f, "Transformed hero")
                    )
                )
            ),
            StoryPattern(
                id = "3",
                name = "Save the Cat",
                description = "Blake Snyder's beat sheet for commercial storytelling",
                frequency = 10,
                confidence = 0.82f,
                examples = listOf(
                    "Opening image contrasts with final image",
                    "Fun and games section showcases premise"
                ),
                category = PatternCategory.NARRATIVE_STRUCTURE,
                icon = Icons.Default.Pets,
                structure = PatternStructure(
                    type = "save-the-cat",
                    beats = listOf(
                        StoryBeat("Opening Image", 0.0f, "Visual snapshot of start"),
                        StoryBeat("Theme Stated", 0.05f, "Hint at lesson"),
                        StoryBeat("Setup", 0.1f, "Establish world"),
                        StoryBeat("Catalyst", 0.12f, "Life-changing event"),
                        StoryBeat("Debate", 0.15f, "Should I do this?"),
                        StoryBeat("Break into Two", 0.2f, "Enter Act 2"),
                        StoryBeat("B Story", 0.22f, "Love story begins"),
                        StoryBeat("Fun and Games", 0.3f, "Promise of premise"),
                        StoryBeat("Midpoint", 0.5f, "Stakes raised"),
                        StoryBeat("Bad Guys Close In", 0.55f, "Things go wrong"),
                        StoryBeat("All Is Lost", 0.75f, "Lowest point"),
                        StoryBeat("Dark Night", 0.8f, "Hero hits bottom"),
                        StoryBeat("Break into Three", 0.85f, "Solution found"),
                        StoryBeat("Finale", 0.9f, "Final confrontation"),
                        StoryBeat("Final Image", 0.99f, "Proof of change")
                    )
                )
            ),
            StoryPattern(
                id = "4",
                name = "Circular Journey",
                description = "Story ends where it began with new perspective",
                frequency = 5,
                confidence = 0.72f,
                examples = listOf(
                    "Opening scene mirrors closing scene",
                    "Character returns home transformed"
                ),
                category = PatternCategory.NARRATIVE_STRUCTURE,
                icon = Icons.Default.Refresh,
                structure = PatternStructure(
                    type = "circular",
                    beats = listOf(
                        StoryBeat("Opening Image", 0.0f, "Initial state"),
                        StoryBeat("Journey Begins", 0.2f, "Departure"),
                        StoryBeat("Transformation", 0.5f, "Change occurs"),
                        StoryBeat("Return Beginning", 0.8f, "Circle back"),
                        StoryBeat("Closing Image", 0.95f, "New perspective")
                    )
                )
            ),
            StoryPattern(
                id = "5",
                name = "Parallel Storylines",
                description = "Multiple narratives that interconnect",
                frequency = 7,
                confidence = 0.69f,
                examples = listOf(
                    "Different character perspectives",
                    "Stories converge at climax"
                ),
                category = PatternCategory.NARRATIVE_STRUCTURE,
                icon = Icons.Default.Timeline,
                structure = PatternStructure(
                    type = "parallel",
                    beats = listOf(
                        StoryBeat("Story A Start", 0.1f, "First narrative"),
                        StoryBeat("Story B Start", 0.15f, "Second narrative"),
                        StoryBeat("Story C Start", 0.2f, "Third narrative"),
                        StoryBeat("Interconnections", 0.5f, "Stories relate"),
                        StoryBeat("Convergence", 0.8f, "Narratives merge"),
                        StoryBeat("Unified Resolution", 0.95f, "Combined ending")
                    )
                )
            )
        )
    }
}