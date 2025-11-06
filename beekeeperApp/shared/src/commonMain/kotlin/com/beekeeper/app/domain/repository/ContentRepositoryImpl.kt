// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/ContentRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Implementation of ContentRepository with mock data and full cross-linking
 * All content types (Story, Script, Storyboard) belong to Projects
 */
class ContentRepositoryImpl : ContentRepository {

    // In-memory storage for mock data
    private val storiesCache = mutableMapOf<String, MutableList<Story>>()
    private val scriptsCache = mutableMapOf<String, MutableList<Script>>()
    private val storyboardsCache = mutableMapOf<String, MutableList<Storyboard>>()
    private val reviewsCache = mutableMapOf<String, MutableList<QualityReview>>()

    // Cross-reference indices for faster lookups
    private val storyToScriptsIndex = mutableMapOf<String, MutableList<String>>()
    private val scriptToStoryboardsIndex = mutableMapOf<String, MutableList<String>>()

    // Since Story doesn't have characterIds, we track this separately
    private val storyToCharactersIndex = mutableMapOf<String, MutableList<String>>()
    private val characterToStoriesIndex = mutableMapOf<String, MutableList<String>>()
    private val characterToScriptsIndex = mutableMapOf<String, MutableList<String>>()

    // Flow for real-time updates
    private val storiesFlow = MutableStateFlow<List<Story>>(emptyList())
    private val scriptsFlow = MutableStateFlow<List<Script>>(emptyList())
    private val storyboardsFlow = MutableStateFlow<List<Storyboard>>(emptyList())

    init {
        // Initialize with sample data and build indices
        SampleProjectsFactory.getAllSampleProjects().forEach { data ->
            val projectId = data.project.id

            // Store stories
            if (data.stories.isNotEmpty()) {
                storiesCache[projectId] = data.stories.toMutableList()

                // Map stories to their characters based on project
                data.stories.forEach { story ->
                    // Get characters for this project and associate with story
                    val characterIds = data.characters.map { it.id }
                    storyToCharactersIndex[story.id] = characterIds.toMutableList()
                    characterIds.forEach { charId ->
                        characterToStoriesIndex.getOrPut(charId) { mutableListOf() }.add(story.id)
                    }
                }
            }

            // Store scripts and build indices
            if (data.scripts.isNotEmpty()) {
                scriptsCache[projectId] = data.scripts.toMutableList()
                data.scripts.forEach { script ->
                    // Link script to story
                    script.storyId?.let { storyId ->
                        storyToScriptsIndex.getOrPut(storyId) { mutableListOf() }.add(script.id)
                    }
                    // Scripts track characters through scenes
                    val characterIds = extractCharacterIdsFromScript(script)
                    characterIds.forEach { charId ->
                        characterToScriptsIndex.getOrPut(charId) { mutableListOf() }.add(script.id)
                    }
                }
            }

            // Store storyboards and build script index
            if (data.storyboards.isNotEmpty()) {
                storyboardsCache[projectId] = data.storyboards.toMutableList()
                data.storyboards.forEach { storyboard ->
                    // Storyboards link to scripts via scenes
                    val scriptId = findScriptIdForStoryboard(storyboard, projectId)
                    scriptId?.let {
                        scriptToStoryboardsIndex.getOrPut(it) { mutableListOf() }.add(storyboard.id)
                    }
                }
            }
        }
    }

    // Story Management
    override suspend fun getStories(projectId: String): List<Story> {
        return storiesCache[projectId] ?: emptyList()
    }

    override suspend fun getStory(storyId: String): Story? {
        return storiesCache.values.flatten().find { it.id == storyId }
    }

    override suspend fun createStory(story: Story): Story {
        val projectStories = storiesCache.getOrPut(story.projectId) { mutableListOf() }
        projectStories.add(story)
        storiesFlow.value = projectStories

        // Note: Characters would need to be associated separately since Story doesn't have characterIds

        return story
    }

    override suspend fun updateStory(story: Story): Story {
        val projectStories = storiesCache[story.projectId]
        projectStories?.let { stories ->
            val index = stories.indexOfFirst { it.id == story.id }
            if (index != -1) {
                stories[index] = story
                storiesFlow.value = stories
            }
        }
        return story
    }

    // FIXED: deleteStory method
    override suspend fun deleteStory(storyId: String): Boolean {
        storiesCache.values.forEach { stories ->
            val storyToRemove = stories.find { it.id == storyId }
            if (storyToRemove != null) {
                // Cross-platform compatible removal
                val updatedStories = stories.filter { it.id != storyId }.toMutableList()
                storiesCache[storyToRemove.projectId] = updatedStories

                // Clean up indices
                storyToScriptsIndex.remove(storyId)
                storyToCharactersIndex.remove(storyId)
                characterToStoriesIndex.values.forEach { storyIds ->
                    storyIds.remove(storyId)
                }

                storiesFlow.value = updatedStories
                return true
            }
        }
        return false
    }

    override suspend fun getStoryPatterns(projectId: String): List<StoryPattern> {
        return listOf(
            StoryPattern(
                id = "pattern_001",
                name = "Hero's Journey",
                description = "Classic monomyth structure",
                structure = PatternStructure(
                    type = "Monomyth",
                    beats = listOf(
                        StoryBeat("Call to Adventure", 0.1f, "Hero receives the call"),
                        StoryBeat("Crossing Threshold", 0.25f, "Hero enters new world"),
                        StoryBeat("Return with Elixir", 0.9f, "Hero returns transformed")
                    )
                ),
                examples = listOf("Star Wars", "The Matrix", "Lord of the Rings")
            ),
            StoryPattern(
                id = "pattern_002",
                name = "Three Act Structure",
                description = "Traditional screenplay format",
                structure = PatternStructure(
                    type = "Three Act",
                    beats = listOf(
                        StoryBeat("Setup", 0.0f, "Establish world and characters"),
                        StoryBeat("Confrontation", 0.25f, "Rising action and conflict"),
                        StoryBeat("Resolution", 0.75f, "Climax and conclusion")
                    )
                ),
                examples = listOf("Most Hollywood films")
            ),
            StoryPattern(
                id = "pattern_003",
                name = "Save the Cat",
                description = "Blake Snyder's beat sheet",
                structure = PatternStructure(
                    type = "Save the Cat",
                    beats = listOf(
                        StoryBeat("Opening Image", 0.0f, "Visual snapshot of start"),
                        StoryBeat("Catalyst", 0.1f, "Life-changing event"),
                        StoryBeat("Final Image", 1.0f, "Visual proof of change")
                    )
                ),
                examples = listOf("Modern commercial films")
            )
        )
    }

    override suspend fun analyzeStoryStructure(storyId: String): StoryAnalysis {
        val story = getStory(storyId) ?: throw IllegalArgumentException("Story not found")

        // Get characters associated with this story
        val characterIds = storyToCharactersIndex[storyId] ?: emptyList()

        return StoryAnalysis(
            storyId = storyId,
            pacing = PacingAnalysis(
                overallPace = "Moderate",
                actPacing = mapOf(
                    1 to "Slow build",
                    2 to "Accelerating",
                    3 to "Intense"
                ),
                suggestions = listOf(
                    "Consider adding more conflict in Act 2",
                    "Pace could be tightened in opening"
                )
            ),
            themeConsistency = 0.85f,
            structureScore = 0.78f,
            suggestions = listOf(
                "Theme could be reinforced in climax",
                "Character motivations need clarity in Act 2",
                "Consider adding subplot for pacing"
            )
        )
    }

    // Script Management
    override suspend fun getScripts(projectId: String): List<Script> {
        return scriptsCache[projectId] ?: emptyList()
    }

    override suspend fun getScript(scriptId: String): Script? {
        return scriptsCache.values.flatten().find { it.id == scriptId }
    }

    override suspend fun createScript(script: Script): Script {
        val projectScripts = scriptsCache.getOrPut(script.projectId) { mutableListOf() }
        projectScripts.add(script)
        scriptsFlow.value = projectScripts

        // Update indices
        script.storyId?.let { storyId ->
            storyToScriptsIndex.getOrPut(storyId) { mutableListOf() }.add(script.id)
        }

        // Extract and index character IDs from script
        val characterIds = extractCharacterIdsFromScript(script)
        characterIds.forEach { charId ->
            characterToScriptsIndex.getOrPut(charId) { mutableListOf() }.add(script.id)
        }

        return script
    }

    override suspend fun updateScript(script: Script): Script {
        val projectScripts = scriptsCache[script.projectId]
        projectScripts?.let { scripts ->
            val index = scripts.indexOfFirst { it.id == script.id }
            if (index != -1) {
                val oldScript = scripts[index]

                // Update story index if story changed
                if (oldScript.storyId != script.storyId) {
                    oldScript.storyId?.let { oldStoryId ->
                        storyToScriptsIndex[oldStoryId]?.remove(script.id)
                    }
                    script.storyId?.let { newStoryId ->
                        storyToScriptsIndex.getOrPut(newStoryId) { mutableListOf() }.add(script.id)
                    }
                }

                scripts[index] = script
                scriptsFlow.value = scripts
            }
        }
        return script
    }

    override suspend fun deleteScript(scriptId: String): Boolean {
        scriptsCache.values.forEach { scripts ->
            val script = scripts.find { it.id == scriptId }
            if (script != null) {
                // Cross-platform compatible removal
                val updatedScripts = scripts.filter { it.id != scriptId }.toMutableList()
                scriptsCache[script.projectId] = updatedScripts

                // Clean up indices
                script.storyId?.let { storyId ->
                    storyToScriptsIndex[storyId]?.remove(scriptId)
                }
                scriptToStoryboardsIndex.remove(scriptId)

                scriptsFlow.value = updatedScripts
                return true
            }
        }
        return false
    }

    override suspend fun extractCharactersFromScript(scriptId: String): List<CharacterProfile> {
        val script = getScript(scriptId)
        return script?.let { s ->
            // Return characters associated with the script's project
            SampleProjectsFactory.getProjectById(s.projectId)?.characters ?: emptyList()
        } ?: emptyList()
    }

    override suspend fun analyzeScriptDialogue(scriptId: String): DialogueAnalysis {
        val script = getScript(scriptId) ?: throw IllegalArgumentException("Script not found")

        // Extract character dialogue from script content
        val characterDialogue = extractDialogueFromScript(script)

        return DialogueAnalysis(
            scriptId = scriptId,
            characterDialogueCount = characterDialogue,
            averageLineLength = calculateAverageLineLength(script),
            dialoguePacing = "Balanced",
            suggestions = listOf(
                "Consider varying dialogue length for rhythm",
                "Some characters could use more distinctive voice",
                "Pacing in Act 2 could be tightened"
            )
        )
    }

    override suspend fun getScriptVersionHistory(scriptId: String): List<ScriptVersion> {
        val currentTime = getCurrentTimeMillis()
        return listOf(
            ScriptVersion(
                id = "v1",
                scriptId = scriptId,
                version = "1.0",
                changes = "Initial draft",
                author = "tm_002",
                timestamp = currentTime - (10 * 24 * 60 * 60 * 1000) // 10 days ago
            ),
            ScriptVersion(
                id = "v2",
                scriptId = scriptId,
                version = "2.0",
                changes = "Added character development",
                author = "tm_002",
                timestamp = currentTime - (5 * 24 * 60 * 60 * 1000) // 5 days ago
            )
        )
    }

    // Storyboard Management
    override suspend fun getStoryboards(projectId: String): List<Storyboard> {
        return storyboardsCache[projectId] ?: emptyList()
    }

    override suspend fun getStoryboard(storyboardId: String): Storyboard? {
        return storyboardsCache.values.flatten().find { it.id == storyboardId }
    }

    override suspend fun createStoryboard(storyboard: Storyboard): Storyboard {
        val projectStoryboards = storyboardsCache.getOrPut(storyboard.projectId) { mutableListOf() }
        projectStoryboards.add(storyboard)
        storyboardsFlow.value = projectStoryboards

        // Update script index if we can find the related script
        val scriptId = findScriptIdForStoryboard(storyboard, storyboard.projectId)
        scriptId?.let {
            scriptToStoryboardsIndex.getOrPut(it) { mutableListOf() }.add(storyboard.id)
        }

        return storyboard
    }

    override suspend fun updateStoryboard(storyboard: Storyboard): Storyboard {
        val projectStoryboards = storyboardsCache[storyboard.projectId]
        projectStoryboards?.let { storyboards ->
            val index = storyboards.indexOfFirst { it.id == storyboard.id }
            if (index != -1) {
                storyboards[index] = storyboard
                storyboardsFlow.value = storyboards
            }
        }
        return storyboard
    }

    // FIXED: deleteStoryboard method
    override suspend fun deleteStoryboard(storyboardId: String): Boolean {
        storyboardsCache.values.forEach { storyboards ->
            val storyboardToRemove = storyboards.find { it.id == storyboardId }
            if (storyboardToRemove != null) {
                // Cross-platform compatible removal
                val updatedStoryboards = storyboards.filter { it.id != storyboardId }.toMutableList()
                storyboardsCache[storyboardToRemove.projectId] = updatedStoryboards

                storyboardsFlow.value = updatedStoryboards
                return true
            }
        }
        return false
    }

    override suspend fun getStoryboardScenes(storyboardId: String): List<Scene> {
        val storyboard = getStoryboard(storyboardId)
        return storyboard?.scenes ?: emptyList()
    }

    override suspend fun updateScene(scene: Scene): Scene {
        val storyboard = storyboardsCache.values.flatten().find { sb ->
            sb.scenes.any { it.id == scene.id }
        }

        storyboard?.let { sb ->
            val updatedScenes = sb.scenes.map { s ->
                if (s.id == scene.id) scene else s
            }
            val updatedStoryboard = sb.copy(scenes = updatedScenes)
            updateStoryboard(updatedStoryboard)
        }

        return scene
    }

    override suspend fun generateStoryboardFromScript(scriptId: String): Storyboard {
        val script = getScript(scriptId) ?: throw IllegalArgumentException("Script not found")

        return Storyboard(
            id = "sb_gen_${getCurrentTimeMillis()}",
            projectId = script.projectId,
            title = "${script.title} - Generated Storyboard",
            description = "AI-generated storyboard from script",
            scenes = listOf(
                Scene(
                    id = "scene_gen_001",
                    storyboardId = "sb_gen_${getCurrentTimeMillis()}",
                    sceneNumber = 1,
                    title = "Opening",
                    description = "Generated opening scene",
                    duration = 60,
                    frames = emptyList(),
                    notes = "AI-generated - review needed",
                    sceneScriptId = script.id
                )
            ),
            duration = 120,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            createdBy = "ai_generator",
            scriptId = scriptId,
            storyId = script?.storyId!!,
            completionPercentage = 0,
        )
    }

    // Content Quality Review
    override suspend fun getQualityReviews(projectId: String): List<QualityReview> {
        return reviewsCache[projectId] ?: emptyList()
    }

    override suspend fun createQualityReview(review: QualityReview): QualityReview {
        val projectReviews = reviewsCache.getOrPut(review.projectId) { mutableListOf() }
        projectReviews.add(review)
        return review
    }

    override suspend fun updateQualityReview(review: QualityReview): QualityReview {
        val projectReviews = reviewsCache[review.projectId]
        projectReviews?.let { reviews ->
            val index = reviews.indexOfFirst { it.id == review.id }
            if (index != -1) {
                reviews[index] = review
            }
        }
        return review
    }

    override suspend fun getReviewMetrics(contentId: String): QualityMetrics {
        return QualityMetrics(
            narrativeCoherence = 0.85f,
            characterConsistency = 0.79f,
            dialogueQuality = 0.81f,
            pacingScore = 0.77f,
            visualComposition = 0.84f,
            technicalQuality = 0.90f,
            overallScore = 0.83f,
            creativityScore = 1.0f,
            platformCompliance =1.0f,
            audienceEngagement = 1.0f,
            aiConfidenceScore = 1.0f,
            reviewNotes = listOf("notes")
        )
    }

    override suspend fun submitForReview(contentId: String, contentType: ContentType): QualityReview {
        return QualityReview(
            id = "review_${getCurrentTimeMillis()}",
            contentId = contentId,
            contentType = contentType,
            projectId = "current_project",
            reviewer = "auto_reviewer",
            score = 0f,
            status = ReviewStatus.PENDING,
            feedback = "Submitted for review",
            metrics = QualityMetrics(
                narrativeCoherence = 0f,
                characterConsistency = 0f,
                dialogueQuality = 0f,
                pacingScore = 0f,
                visualComposition = 0f,
                technicalQuality = 0f,
                overallScore = 0.83f,
                creativityScore = 1.0f,
                platformCompliance =1.0f,
                audienceEngagement = 1.0f,
                aiConfidenceScore = 1.0f,
                reviewNotes = listOf("notes")
            ),
            createdAt = getCurrentTimeMillis(),
            completedAt = null
        )
    }

    // Content Search and Filtering
    override suspend fun searchContent(query: String, filters: ContentFilters): List<ContentInterface> {
        val results = mutableListOf<ContentInterface>()
        val lowercaseQuery = query.lowercase()

        // Filter by content types if specified
        val typesToSearch = filters.types ?: listOf(
            ContentType.STORY,
            ContentType.SCRIPT,
            ContentType.STORYBOARD
        )

        // Search stories
        if (ContentType.STORY in typesToSearch) {
            storiesCache.values.flatten()
                .filter { story ->
                    (story.title.lowercase().contains(lowercaseQuery) ||
                            story.synopsis.lowercase().contains(lowercaseQuery)) &&
                            (filters.status?.contains(story.status) ?: true)
                }
                .forEach { results.add(StoryContent(it)) }
        }

        // Search scripts
        if (ContentType.SCRIPT in typesToSearch) {
            scriptsCache.values.flatten()
                .filter { script ->
                    (script.title.lowercase().contains(lowercaseQuery) ||
                            script.content.lowercase().contains(lowercaseQuery)) &&
                            (filters.status?.contains(script.status) ?: true)
                }
                .forEach { results.add(ScriptContent(it)) }
        }

        // Search storyboards
        if (ContentType.STORYBOARD in typesToSearch) {
            storyboardsCache.values.flatten()
                .filter { storyboard ->
                    storyboard.title.lowercase().contains(lowercaseQuery) ||
                            (storyboard.description?.lowercase()?.contains(lowercaseQuery) ?: false)
                }
                .forEach { results.add(StoryboardContent(it)) }
        }

        return results
    }

    override suspend fun getRecentContent(projectId: String, limit: Int): List<ContentInterface> {
        val allContent = mutableListOf<ContentInterface>()

        storiesCache[projectId]?.forEach { allContent.add(StoryContent(it)) }
        scriptsCache[projectId]?.forEach { allContent.add(ScriptContent(it)) }
        storyboardsCache[projectId]?.forEach { allContent.add(StoryboardContent(it)) }

        return allContent
            .sortedByDescending { it.lastModified }
            .take(limit)
    }

    override suspend fun getContentByTags(tags: List<String>): List<ContentInterface> {
        val results = mutableListOf<ContentInterface>()

        // Check stories for matching themes (as tags)
        storiesCache.values.flatten()
            .filter { story ->
                story.themes.any { theme -> tags.contains(theme) }
            }
            .forEach { results.add(StoryContent(it)) }

        return results
    }

    // Real-time updates
    override fun observeStories(projectId: String): Flow<List<Story>> = flow {
        emit(getStories(projectId))
        storiesFlow.collect { stories ->
            emit(stories.filter { it.projectId == projectId })
        }
    }

    override fun observeScripts(projectId: String): Flow<List<Script>> = flow {
        emit(getScripts(projectId))
        scriptsFlow.collect { scripts ->
            emit(scripts.filter { it.projectId == projectId })
        }
    }

    override fun observeStoryboards(projectId: String): Flow<List<Storyboard>> = flow {
        emit(getStoryboards(projectId))
        storyboardsFlow.collect { storyboards ->
            emit(storyboards.filter { it.projectId == projectId })
        }
    }

    // Helper methods for cross-repository queries

    suspend fun getScriptsForStory(storyId: String): List<Script> {
        return storyToScriptsIndex[storyId]?.mapNotNull { getScript(it) } ?: emptyList()
    }

    suspend fun getStoryboardsForScript(scriptId: String): List<Storyboard> {
        return scriptToStoryboardsIndex[scriptId]?.mapNotNull { getStoryboard(it) } ?: emptyList()
    }

    suspend fun getCharacterContent(characterId: String): CharacterContent {
        return CharacterContent(
            characterId = characterId,
            stories = characterToStoriesIndex[characterId]?.mapNotNull { getStory(it) } ?: emptyList(),
            scripts = characterToScriptsIndex[characterId]?.mapNotNull { getScript(it) } ?: emptyList(),
            storyboards = getStoryboardsForCharacter(characterId)
        )
    }

    private suspend fun getStoryboardsForCharacter(characterId: String): List<Storyboard> {
        val characterScripts = characterToScriptsIndex[characterId] ?: return emptyList()
        return characterScripts.flatMap { scriptId ->
            scriptToStoryboardsIndex[scriptId]?.mapNotNull { getStoryboard(it) } ?: emptyList()
        }
    }

    suspend fun getProjectContentTree(projectId: String): ProjectContentTree {
        return ProjectContentTree(
            projectId = projectId,
            stories = getStories(projectId),
            scripts = getScripts(projectId),
            storyboards = getStoryboards(projectId),
            relationships = buildContentRelationships(projectId)
        )
    }

    private suspend fun buildContentRelationships(projectId: String): ContentRelationships {
        val stories = getStories(projectId)
        val scripts = getScripts(projectId)
        val storyboards = getStoryboards(projectId)

        // Get character IDs for this project
        val characterIds = SampleProjectsFactory.getProjectById(projectId)?.characters?.map { it.id } ?: emptyList()

        return ContentRelationships(
            storyToScripts = stories.associate { story ->
                story.id to scripts.filter { it.storyId == story.id }.map { it.id }
            },
            scriptToStoryboards = scripts.associate { script ->
                script.id to storyboards.filter { sb ->
                    // Find storyboards related to this script
                    scriptToStoryboardsIndex[script.id]?.contains(sb.id) ?: false
                }.map { it.id }
            },
            characterAppearances = mutableMapOf<String, ContentAppearances>().apply {
                characterIds.forEach { charId ->
                    put(charId, ContentAppearances(
                        inStories = characterToStoriesIndex[charId] ?: emptyList(),
                        inScripts = characterToScriptsIndex[charId] ?: emptyList(),
                        inStoryboards = storyboards.filter { sb ->
                            // Check if character appears in storyboard's related script
                            val scriptId = findScriptIdForStoryboard(sb, projectId)
                            scriptId?.let {
                                characterToScriptsIndex[charId]?.contains(it) ?: false
                            } ?: false
                        }.map { it.id }
                    ))
                }
            }
        )
    }

    // Private helper methods

    private fun extractCharacterIdsFromScript(script: Script): List<String> {
        // Scripts don't have acts/scenes with characterIds in the current model
        // We need to parse the script content or use the characters list if available

        // For now, extract from the script's project
        return SampleProjectsFactory.getProjectById(script.projectId)?.characters?.map { it.id } ?: emptyList()
    }

    private fun findScriptIdForStoryboard(storyboard: Storyboard, projectId: String): String? {
        // Try to find a script that matches this storyboard
        // This would be more sophisticated in a real implementation
        return scriptsCache[projectId]?.firstOrNull { script ->
            script.title.contains(storyboard.title.replace(" - Storyboard", "").replace(" - Generated Storyboard", "")) ||
                    storyboard.title.contains(script.title)
        }?.id
    }

    private fun extractDialogueFromScript(script: Script): Map<String, Int> {
        // Parse script content to count dialogue per character
        // Simple regex-based extraction from screenplay format
        val dialogueCount = mutableMapOf<String, Int>()

        // Look for character names in uppercase followed by dialogue
        val lines = script.content.lines()
        var currentCharacter: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            // Character names are typically in uppercase and centered
            if (trimmed.isNotEmpty() && trimmed == trimmed.uppercase() && !trimmed.startsWith("INT.") && !trimmed.startsWith("EXT.")) {
                // This might be a character name
                if (trimmed.length < 50 && !trimmed.contains("FADE") && !trimmed.contains("CUT")) {
                    currentCharacter = trimmed
                }
            } else if (currentCharacter != null && trimmed.isNotEmpty() && !trimmed.startsWith("(")) {
                // This is dialogue for the current character
                val count = dialogueCount[currentCharacter] ?: 0
                dialogueCount[currentCharacter] = count + 1
            }
        }

        return dialogueCount
    }

    private fun calculateAverageLineLength(script: Script): Float {
        // Parse script content to calculate average dialogue length
        val lines = script.content.lines()
        var totalLength = 0
        var lineCount = 0

        for (line in lines) {
            val trimmed = line.trim()
            // Count lines that look like dialogue (not character names, not scene headings)
            if (trimmed.isNotEmpty() &&
                !trimmed.startsWith("INT.") &&
                !trimmed.startsWith("EXT.") &&
                trimmed != trimmed.uppercase() &&
                !trimmed.startsWith("(")) {
                totalLength += trimmed.length
                lineCount++
            }
        }

        return if (lineCount > 0) totalLength.toFloat() / lineCount else 0f
    }
}

// Content wrapper classes for the Content interface
sealed interface ContentInterface {
    val id: String
    val projectId: String
    val title: String
    val lastModified: Long
    val contentType: ContentType
}

data class StoryContent(val story: Story) : ContentInterface {
    override val id = story.id
    override val projectId = story.projectId
    override val title = story.title
    override val lastModified = story.updatedAt
    override val contentType = ContentType.STORY
}

data class ScriptContent(val script: Script) : ContentInterface {
    override val id = script.id
    override val projectId = script.projectId
    override val title = script.title
    override val lastModified = script.updatedAt.toEpochMilliseconds()
    override val contentType = ContentType.SCRIPT
}

data class StoryboardContent(val storyboard: Storyboard) : ContentInterface {
    override val id = storyboard.id
    override val projectId = storyboard.projectId
    override val title = storyboard.title
    override val lastModified = storyboard.updatedAt.toEpochMilliseconds()
    override val contentType = ContentType.STORYBOARD
}

// Supporting data classes

data class CharacterContent(
    val characterId: String,
    val stories: List<Story>,
    val scripts: List<Script>,
    val storyboards: List<Storyboard>
)

data class ProjectContentTree(
    val projectId: String,
    val stories: List<Story>,
    val scripts: List<Script>,
    val storyboards: List<Storyboard>,
    val relationships: ContentRelationships
)

data class ContentRelationships(
    val storyToScripts: Map<String, List<String>>,
    val scriptToStoryboards: Map<String, List<String>>,
    val characterAppearances: Map<String, ContentAppearances>
)

data class ContentAppearances(
    val inStories: List<String>,
    val inScripts: List<String>,
    val inStoryboards: List<String>
)