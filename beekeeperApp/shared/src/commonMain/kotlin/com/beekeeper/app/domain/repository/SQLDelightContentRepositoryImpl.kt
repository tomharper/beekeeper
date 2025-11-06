// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/SQLDelightContentRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * SQLDelight-backed implementation of ContentRepository
 * Reads content data (Stories, Scripts, Storyboards) from SQLDelight database (ProjectFactory JSON blobs)
 * Uses in-memory cache for performance
 */
class SQLDelightContentRepositoryImpl(
    private val factoryRepository: SQLDelightProjectFactoryRepository,
    private val database: com.beekeeper.app.data.sqldelight.CineFillerDatabase
) : ContentRepository {

    // In-memory cache for performance
    private val storiesCache = mutableMapOf<String, MutableList<Story>>()
    private val scriptsCache = mutableMapOf<String, MutableList<Script>>()
    private val storyboardsCache = mutableMapOf<String, MutableList<Storyboard>>()
    private val reviewsCache = mutableMapOf<String, MutableList<QualityReview>>()

    // Cross-reference indices for faster lookups
    private val storyToScriptsIndex = mutableMapOf<String, MutableList<String>>()
    private val scriptToStoryboardsIndex = mutableMapOf<String, MutableList<String>>()
    private val storyToCharactersIndex = mutableMapOf<String, MutableList<String>>()
    private val characterToStoriesIndex = mutableMapOf<String, MutableList<String>>()
    private val characterToScriptsIndex = mutableMapOf<String, MutableList<String>>()

    // Flows for real-time updates
    private val storiesFlow = MutableStateFlow<List<Story>>(emptyList())
    private val scriptsFlow = MutableStateFlow<List<Script>>(emptyList())
    private val storyboardsFlow = MutableStateFlow<List<Storyboard>>(emptyList())

    init {
        // Load initial data from SQLDelight
        CoroutineScope(Dispatchers.Default).launch {
            refreshCache()
        }
    }

    /**
     * Refresh cache from SQLDelight
     */
    private suspend fun refreshCache() {
        try {
            // Get basic info first to avoid CursorWindow size issues
            val basicInfo = factoryRepository.getAllFactoriesBasicInfo()
            println("📚 [ContentRepository] Refreshing cache from ${basicInfo.size} factories")

            storiesCache.clear()
            scriptsCache.clear()
            storyboardsCache.clear()

            // Load each factory individually to avoid large row issues
            basicInfo.forEach { info ->
                val projectId = info["project_id"] as? String ?: return@forEach
                println("📚 [ContentRepository] Processing factory for project: $projectId")

                try {
                    val factory = factoryRepository.getFactoryByProjectId(projectId)
                    if (factory == null) {
                        println("   ⚠️ Factory not found for project: $projectId")
                        return@forEach
                    }

                    println("   - Stories: ${factory.stories.size}, Scripts: ${factory.scripts.size}, Storyboards: ${factory.storyboards.size}")

                    // Load stories
                    if (factory.stories.isNotEmpty()) {
                        storiesCache[projectId] = factory.stories.toMutableList()
                        println("   ✅ Loaded ${factory.stories.size} stories for $projectId")

                        // Build story-character indices
                        factory.stories.forEach { story ->
                            val characterIds = factory.characters.map { it.id }
                            storyToCharactersIndex[story.id] = characterIds.toMutableList()
                            characterIds.forEach { charId ->
                                characterToStoriesIndex.getOrPut(charId) { mutableListOf() }.add(story.id)
                            }
                        }
                    }

                    // Load scripts
                    if (factory.scripts.isNotEmpty()) {
                        scriptsCache[projectId] = factory.scripts.toMutableList()
                        println("   ✅ Loaded ${factory.scripts.size} scripts for $projectId")

                        factory.scripts.forEach { script ->
                            // Link script to story
                            script.storyId?.let { storyId ->
                                storyToScriptsIndex.getOrPut(storyId) { mutableListOf() }.add(script.id)
                            }

                            // Link scripts to characters
                            val characterIds = factory.characters.map { it.id }
                            characterIds.forEach { charId ->
                                characterToScriptsIndex.getOrPut(charId) { mutableListOf() }.add(script.id)
                            }
                        }
                    }

                    // Load storyboards
                    if (factory.storyboards.isNotEmpty()) {
                        storyboardsCache[projectId] = factory.storyboards.toMutableList()
                        println("   ✅ Loaded ${factory.storyboards.size} storyboards for $projectId")

                        factory.storyboards.forEach { storyboard ->
                            storyboard.scriptId?.let { scriptId ->
                                scriptToStoryboardsIndex.getOrPut(scriptId) { mutableListOf() }.add(storyboard.id)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("   ⚠️ Skipping $projectId - factory too large: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("❌ Error refreshing content cache: ${e.message}")
        }
    }

    // ===== STORY MANAGEMENT =====

    override suspend fun getStories(projectId: String): List<Story> {
        println("SQLDelightContentRepository.getStories called for project: '$projectId'")

        val cached = storiesCache[projectId]
        if (cached != null) {
            println("✅ Returning ${cached.size} stories from cache")
            return cached
        }

        try {
            println("Loading stories from SQLDelight factory...")
            val factory = factoryRepository.getFactoryByProjectId(projectId)
            val stories = factory?.stories ?: emptyList()
            println("✅ Loaded ${stories.size} stories from factory: ${factory?.projectId ?: "none"}")
            storiesCache[projectId] = stories.toMutableList()
            return stories
        } catch (e: Exception) {
            println("❌ Error loading stories: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    override suspend fun getStory(storyId: String): Story? {
        storiesCache.values.forEach { stories ->
            stories.find { it.id == storyId }?.let { return it }
        }

        try {
            val factories = factoryRepository.getAllFactories()
            factories.forEach { factory ->
                factory.stories.find { it.id == storyId }?.let { story ->
                    storiesCache.getOrPut(factory.projectId) { mutableListOf() }.add(story)
                    return story
                }
            }
        } catch (e: Exception) {
            println("❌ Error finding story: ${e.message}")
        }

        return null
    }

    override suspend fun createStory(story: Story): Story {
        try {
            val factory = factoryRepository.getFactoryByProjectId(story.projectId)

            if (factory != null) {
                val updatedStories = factory.stories + story
                val updatedFactory = factory.copy(stories = updatedStories)

                val factoryType = when {
                    factory.metadata.isSample -> "sample"
                    factory.metadata.isTemplate -> "template"
                    else -> "user"
                }

                factoryRepository.saveFactory(updatedFactory, factoryType)
                storiesCache.getOrPut(story.projectId) { mutableListOf() }.add(story)
                storiesFlow.value = storiesCache[story.projectId] ?: emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error creating story: ${e.message}")
        }

        return story
    }

    override suspend fun updateStory(story: Story): Story {
        try {
            val factory = factoryRepository.getFactoryByProjectId(story.projectId)

            if (factory != null) {
                val updatedStories = factory.stories.map {
                    if (it.id == story.id) story else it
                }
                val updatedFactory = factory.copy(stories = updatedStories)

                val factoryType = when {
                    factory.metadata.isSample -> "sample"
                    factory.metadata.isTemplate -> "template"
                    else -> "user"
                }

                factoryRepository.saveFactory(updatedFactory, factoryType)

                storiesCache[story.projectId]?.let { stories ->
                    val index = stories.indexOfFirst { it.id == story.id }
                    if (index != -1) {
                        stories[index] = story
                        storiesFlow.value = stories
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error updating story: ${e.message}")
        }

        return story
    }

    override suspend fun deleteStory(storyId: String): Boolean {
        try {
            val factories = factoryRepository.getAllFactories()

            factories.forEach { factory ->
                val storyToDelete = factory.stories.find { it.id == storyId }
                if (storyToDelete != null) {
                    val updatedStories = factory.stories.filter { it.id != storyId }
                    val updatedFactory = factory.copy(stories = updatedStories)

                    val factoryType = when {
                        factory.metadata.isSample -> "sample"
                        factory.metadata.isTemplate -> "template"
                        else -> "user"
                    }

                    factoryRepository.saveFactory(updatedFactory, factoryType)

                    // Update cache and indices
                    storiesCache[factory.projectId]?.removeAll { it.id == storyId }
                    storyToScriptsIndex.remove(storyId)
                    storyToCharactersIndex.remove(storyId)
                    characterToStoriesIndex.values.forEach { it.remove(storyId) }

                    storiesFlow.value = storiesCache[factory.projectId] ?: emptyList()
                    return true
                }
            }
        } catch (e: Exception) {
            println("❌ Error deleting story: ${e.message}")
        }

        return false
    }

    // ===== SCRIPT MANAGEMENT =====

    override suspend fun getScripts(projectId: String): List<Script> {
        println("SQLDelightContentRepository.getScripts called for project: '$projectId'")

        val cached = scriptsCache[projectId]
        if (cached != null) {
            println("✅ Returning ${cached.size} scripts from cache")
            return cached
        }

        try {
            println("Loading scripts from SQLDelight factory...")
            val factory = factoryRepository.getFactoryByProjectId(projectId)
            val scripts = factory?.scripts ?: emptyList()
            println("✅ Loaded ${scripts.size} scripts from factory: ${factory?.projectId ?: "none"}")
            scriptsCache[projectId] = scripts.toMutableList()
            return scripts
        } catch (e: Exception) {
            println("❌ Error loading scripts: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun getScript(scriptId: String): Script? {
        scriptsCache.values.forEach { scripts ->
            scripts.find { it.id == scriptId }?.let { return it }
        }

        try {
            val factories = factoryRepository.getAllFactories()
            factories.forEach { factory ->
                factory.scripts.find { it.id == scriptId }?.let { script ->
                    scriptsCache.getOrPut(factory.projectId) { mutableListOf() }.add(script)
                    return script
                }
            }
        } catch (e: Exception) {
            println("❌ Error finding script: ${e.message}")
        }

        return null
    }

    override suspend fun createScript(script: Script): Script {
        try {
            val factory = factoryRepository.getFactoryByProjectId(script.projectId)

            if (factory != null) {
                val updatedScripts = factory.scripts + script
                val updatedFactory = factory.copy(scripts = updatedScripts)

                val factoryType = when {
                    factory.metadata.isSample -> "sample"
                    factory.metadata.isTemplate -> "template"
                    else -> "user"
                }

                factoryRepository.saveFactory(updatedFactory, factoryType)
                scriptsCache.getOrPut(script.projectId) { mutableListOf() }.add(script)
                scriptsFlow.value = scriptsCache[script.projectId] ?: emptyList()

                // Update indices
                script.storyId?.let { storyId ->
                    storyToScriptsIndex.getOrPut(storyId) { mutableListOf() }.add(script.id)
                }
            }
        } catch (e: Exception) {
            println("❌ Error creating script: ${e.message}")
        }

        return script
    }

    override suspend fun updateScript(script: Script): Script {
        try {
            val factory = factoryRepository.getFactoryByProjectId(script.projectId)

            if (factory != null) {
                val updatedScripts = factory.scripts.map {
                    if (it.id == script.id) script else it
                }
                val updatedFactory = factory.copy(scripts = updatedScripts)

                val factoryType = when {
                    factory.metadata.isSample -> "sample"
                    factory.metadata.isTemplate -> "template"
                    else -> "user"
                }

                factoryRepository.saveFactory(updatedFactory, factoryType)

                scriptsCache[script.projectId]?.let { scripts ->
                    val index = scripts.indexOfFirst { it.id == script.id }
                    if (index != -1) {
                        scripts[index] = script
                        scriptsFlow.value = scripts
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error updating script: ${e.message}")
        }

        return script
    }

    override suspend fun deleteScript(scriptId: String): Boolean {
        try {
            val factories = factoryRepository.getAllFactories()

            factories.forEach { factory ->
                val scriptToDelete = factory.scripts.find { it.id == scriptId }
                if (scriptToDelete != null) {
                    val updatedScripts = factory.scripts.filter { it.id != scriptId }
                    val updatedFactory = factory.copy(scripts = updatedScripts)

                    val factoryType = when {
                        factory.metadata.isSample -> "sample"
                        factory.metadata.isTemplate -> "template"
                        else -> "user"
                    }

                    factoryRepository.saveFactory(updatedFactory, factoryType)

                    // Update cache and indices
                    scriptsCache[factory.projectId]?.removeAll { it.id == scriptId }
                    scriptToStoryboardsIndex.remove(scriptId)

                    scriptToDelete.storyId?.let { storyId ->
                        storyToScriptsIndex[storyId]?.remove(scriptId)
                    }

                    scriptsFlow.value = scriptsCache[factory.projectId] ?: emptyList()
                    return true
                }
            }
        } catch (e: Exception) {
            println("❌ Error deleting script: ${e.message}")
        }

        return false
    }

    // ===== STORYBOARD MANAGEMENT =====

    override suspend fun getStoryboards(projectId: String): List<Storyboard> {
        println("SQLDelightContentRepository.getStoryboards called for project: '$projectId'")

        val cached = storyboardsCache[projectId]
        if (cached != null) {
            println("✅ Returning ${cached.size} storyboards from cache")
            return cached
        }

        try {
            println("Loading storyboards from SQLDelight factory...")
            val factory = factoryRepository.getFactoryByProjectId(projectId)
            val storyboards = factory?.storyboards ?: emptyList()
            println("✅ Loaded ${storyboards.size} storyboards from factory: ${factory?.projectId ?: "none"}")
            storyboardsCache[projectId] = storyboards.toMutableList()
            return storyboards
        } catch (e: Exception) {
            println("❌ Error loading storyboards: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun getStoryboard(storyboardId: String): Storyboard? {
        storyboardsCache.values.forEach { storyboards ->
            storyboards.find { it.id == storyboardId }?.let { return it }
        }

        try {
            val factories = factoryRepository.getAllFactories()
            factories.forEach { factory ->
                factory.storyboards.find { it.id == storyboardId }?.let { storyboard ->
                    storyboardsCache.getOrPut(factory.projectId) { mutableListOf() }.add(storyboard)
                    return storyboard
                }
            }
        } catch (e: Exception) {
            println("❌ Error finding storyboard: ${e.message}")
        }

        return null
    }

    override suspend fun createStoryboard(storyboard: Storyboard): Storyboard {
        try {
            val factory = factoryRepository.getFactoryByProjectId(storyboard.projectId)

            if (factory != null) {
                val updatedStoryboards = factory.storyboards + storyboard
                val updatedFactory = factory.copy(storyboards = updatedStoryboards)

                val factoryType = when {
                    factory.metadata.isSample -> "sample"
                    factory.metadata.isTemplate -> "template"
                    else -> "user"
                }

                factoryRepository.saveFactory(updatedFactory, factoryType)
                storyboardsCache.getOrPut(storyboard.projectId) { mutableListOf() }.add(storyboard)
                storyboardsFlow.value = storyboardsCache[storyboard.projectId] ?: emptyList()

                // Update indices
                storyboard.scriptId?.let { scriptId ->
                    scriptToStoryboardsIndex.getOrPut(scriptId) { mutableListOf() }.add(storyboard.id)
                }
            }
        } catch (e: Exception) {
            println("❌ Error creating storyboard: ${e.message}")
        }

        return storyboard
    }

    override suspend fun updateStoryboard(storyboard: Storyboard): Storyboard {
        try {
            val factory = factoryRepository.getFactoryByProjectId(storyboard.projectId)

            if (factory != null) {
                val updatedStoryboards = factory.storyboards.map {
                    if (it.id == storyboard.id) storyboard else it
                }
                val updatedFactory = factory.copy(storyboards = updatedStoryboards)

                val factoryType = when {
                    factory.metadata.isSample -> "sample"
                    factory.metadata.isTemplate -> "template"
                    else -> "user"
                }

                factoryRepository.saveFactory(updatedFactory, factoryType)

                storyboardsCache[storyboard.projectId]?.let { storyboards ->
                    val index = storyboards.indexOfFirst { it.id == storyboard.id }
                    if (index != -1) {
                        storyboards[index] = storyboard
                        storyboardsFlow.value = storyboards
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error updating storyboard: ${e.message}")
        }

        return storyboard
    }

    override suspend fun deleteStoryboard(storyboardId: String): Boolean {
        try {
            val factories = factoryRepository.getAllFactories()

            factories.forEach { factory ->
                val storyboardToDelete = factory.storyboards.find { it.id == storyboardId }
                if (storyboardToDelete != null) {
                    val updatedStoryboards = factory.storyboards.filter { it.id != storyboardId }
                    val updatedFactory = factory.copy(storyboards = updatedStoryboards)

                    val factoryType = when {
                        factory.metadata.isSample -> "sample"
                        factory.metadata.isTemplate -> "template"
                        else -> "user"
                    }

                    factoryRepository.saveFactory(updatedFactory, factoryType)

                    // Update cache
                    storyboardsCache[factory.projectId]?.removeAll { it.id == storyboardId }
                    storyboardsFlow.value = storyboardsCache[factory.projectId] ?: emptyList()
                    return true
                }
            }
        } catch (e: Exception) {
            println("❌ Error deleting storyboard: ${e.message}")
        }

        return false
    }

    // ===== STUB METHODS - Using ContentRepositoryImpl's implementation =====

    override suspend fun getStoryPatterns(projectId: String): List<StoryPattern> {
        return ContentRepositoryImpl().getStoryPatterns(projectId)
    }

    override suspend fun analyzeStoryStructure(storyId: String): StoryAnalysis {
        return ContentRepositoryImpl().analyzeStoryStructure(storyId)
    }

    override suspend fun extractCharactersFromScript(scriptId: String): List<CharacterProfile> {
        return ContentRepositoryImpl().extractCharactersFromScript(scriptId)
    }

    override suspend fun analyzeScriptDialogue(scriptId: String): DialogueAnalysis {
        return ContentRepositoryImpl().analyzeScriptDialogue(scriptId)
    }

    override suspend fun getScriptVersionHistory(scriptId: String): List<ScriptVersion> {
        return ContentRepositoryImpl().getScriptVersionHistory(scriptId)
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
            storyId = script.storyId,
            completionPercentage = 0,
        )
    }

    override suspend fun getQualityReviews(projectId: String): List<QualityReview> {
        try {
            return database.qualityReviewQueries.selectByProjectId(projectId)
                .executeAsList()
                .map { row ->
                    val metricsJson = row.metrics_json
                    val metrics = if (metricsJson != null) {
                        try {
                            kotlinx.serialization.json.Json.decodeFromString(QualityMetrics.serializer(), metricsJson)
                        } catch (e: Exception) {
                            // Default metrics if JSON parsing fails
                            QualityMetrics(
                                overallScore = 0f,
                                technicalQuality = 0f,
                                creativityScore = 0f,
                                platformCompliance = 0f,
                                narrativeCoherence = 0f,
                                characterConsistency = 0f,
                                dialogueQuality = 0f,
                                pacingScore = 0f,
                                visualComposition = null
                            )
                        }
                    } else {
                        // Default metrics if no JSON
                        QualityMetrics(
                            overallScore = 0f,
                            technicalQuality = 0f,
                            creativityScore = 0f,
                            platformCompliance = 0f,
                            narrativeCoherence = 0f,
                            characterConsistency = 0f,
                            dialogueQuality = 0f,
                            pacingScore = 0f,
                            visualComposition = null
                        )
                    }

                    QualityReview(
                        id = row.id,
                        contentId = row.content_id,
                        contentType = ContentType.valueOf(row.content_type),
                        projectId = row.project_id,
                        reviewer = row.reviewer,
                        score = (row.score ?: 0.0).toFloat(),
                        status = ReviewStatus.valueOf(row.status),
                        feedback = row.feedback ?: "",
                        metrics = metrics,
                        createdAt = row.created_at,
                        completedAt = row.completed_at
                    )
                }
        } catch (e: Exception) {
            println("❌ Error loading quality reviews: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun createQualityReview(review: QualityReview): QualityReview {
        try {
            val metricsJson = kotlinx.serialization.json.Json.encodeToString(QualityMetrics.serializer(), review.metrics)
            database.qualityReviewQueries.insertOrReplace(
                id = review.id,
                project_id = review.projectId,
                content_id = review.contentId,
                content_type = review.contentType.name,
                reviewer = review.reviewer ?: "unknown",
                score = review.score.toDouble(),
                status = review.status.name,
                feedback = review.feedback,
                metrics_json = metricsJson,
                created_at = review.createdAt,
                completed_at = review.completedAt
            )

            // Update cache
            reviewsCache.getOrPut(review.projectId) { mutableListOf() }.add(review)
            return review
        } catch (e: Exception) {
            println("❌ Error creating quality review: ${e.message}")
            return review
        }
    }

    override suspend fun updateQualityReview(review: QualityReview): QualityReview {
        try {
            val metricsJson = kotlinx.serialization.json.Json.encodeToString(QualityMetrics.serializer(), review.metrics)
            database.qualityReviewQueries.insertOrReplace(
                id = review.id,
                project_id = review.projectId,
                content_id = review.contentId,
                content_type = review.contentType.name,
                reviewer = review.reviewer ?: "unknown",
                score = review.score.toDouble(),
                status = review.status.name,
                feedback = review.feedback,
                metrics_json = metricsJson,
                created_at = review.createdAt,
                completed_at = review.completedAt
            )

            // Update cache
            val projectReviews = reviewsCache[review.projectId]
            projectReviews?.let { reviews ->
                val index = reviews.indexOfFirst { it.id == review.id }
                if (index != -1) {
                    reviews[index] = review
                }
            }
            return review
        } catch (e: Exception) {
            println("❌ Error updating quality review: ${e.message}")
            return review
        }
    }

    override suspend fun getReviewMetrics(contentId: String): QualityMetrics {
        return ContentRepositoryImpl().getReviewMetrics(contentId)
    }

    override suspend fun submitForReview(contentId: String, contentType: ContentType): QualityReview {
        return ContentRepositoryImpl().submitForReview(contentId, contentType)
    }

    override suspend fun searchContent(query: String, filters: ContentFilters): List<ContentInterface> {
        return ContentRepositoryImpl().searchContent(query, filters)
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

        storiesCache.values.flatten()
            .filter { story ->
                story.themes.any { theme -> tags.contains(theme) }
            }
            .forEach { results.add(StoryContent(it)) }

        return results
    }

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
}
