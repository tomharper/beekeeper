// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/ScriptRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

class ScriptRepositoryImpl(
    private val projectRepository: ProjectRepository = RepositoryManager.projectRepository,
    private val contentRepository: ContentRepository = RepositoryManager.contentRepository
) : ScriptRepository {

    // In-memory caches
    private val scriptsCache = mutableMapOf<String, Script>()
    private val scriptScenesCache = mutableMapOf<String, MutableList<SceneScript>>()
    private val projectScriptsIndex = mutableMapOf<String, MutableList<String>>()
    private val storyScriptsIndex = mutableMapOf<String, MutableList<String>>()
    private val scriptProjectIndex = mutableMapOf<String, String>()

    // Flows
    private val projectScriptsFlow = mutableMapOf<String, MutableStateFlow<List<Script>>>()
    private val scriptScenesFlow = mutableMapOf<String, MutableStateFlow<List<SceneScript>>>()

    init {
        initializeSampleData()
    }

    private fun initializeSampleData() {
        SampleProjectsFactory.getAllSampleProjects().forEach { projectData ->
            val projectId = projectData.project.id

            projectData.scripts.forEach { script ->
                scriptsCache[script.id] = script
                scriptProjectIndex[script.id] = projectId
                projectScriptsIndex.getOrPut(projectId) { mutableListOf() }.add(script.id)

                script.storyId?.let { storyId ->
                    storyScriptsIndex.getOrPut(storyId) { mutableListOf() }.add(script.id)
                }

                val scenes = createSceneScripts(script, projectData)
                scriptScenesCache[script.id] = scenes.toMutableList()
            }
        }
    }

    private fun createSceneScripts(script: Script, projectData: SampleProjectData): List<SceneScript> {
        val characters = projectData.characters
        val story = projectData.stories.find { it.id == script.storyId }

        // Use the structure property with default
        val structure = try {
            script.structure
        } catch (e: Exception) {
            ScriptStructureType.THREE_ACT
        }

        return when (structure) {
            ScriptStructureType.THREE_ACT -> generateThreeActScenes(script, characters, story)
            ScriptStructureType.FIVE_ACT -> generateFiveActScenes(script, characters, story)
            ScriptStructureType.HEROES_JOURNEY -> generateHerosJourneyScenes(script, characters, story)
            else -> generateDefaultScenes(script, characters, story)
        }
    }

    // Core operations
    override suspend fun getScript(scriptId: String): Script? {
        return scriptsCache[scriptId]
    }

    override suspend fun getScriptsByProject(projectId: String): List<Script> {
        val scriptIds = projectScriptsIndex[projectId] ?: return emptyList()
        return scriptIds.mapNotNull { scriptsCache[it] }
    }

    override suspend fun getScriptsByStory(storyId: String): List<Script> {
        val scriptIds = storyScriptsIndex[storyId] ?: return emptyList()
        return scriptIds.mapNotNull { scriptsCache[it] }
    }

    override suspend fun createScript(script: Script): Script {
        val projectId = scriptProjectIndex[script.id] ?: script.projectId
        require(projectId.isNotEmpty()) { "Script must be associated with a project" }

        scriptsCache[script.id] = script
        scriptProjectIndex[script.id] = projectId
        projectScriptsIndex.getOrPut(projectId) { mutableListOf() }.add(script.id)

        script.storyId?.let { storyId ->
            storyScriptsIndex.getOrPut(storyId) { mutableListOf() }.add(script.id)
        }

        updateProjectScriptsFlow(projectId)
        return script
    }

    override suspend fun updateScript(script: Script): Script {
        scriptsCache[script.id] = script
        scriptProjectIndex[script.id]?.let { projectId ->
            updateProjectScriptsFlow(projectId)
        }
        return script
    }

    override suspend fun deleteScript(scriptId: String): Boolean {
        val script = scriptsCache.remove(scriptId) ?: return false

        scriptProjectIndex[scriptId]?.let { projectId ->
            projectScriptsIndex[projectId]?.remove(scriptId)
            updateProjectScriptsFlow(projectId)
        }

        script.storyId?.let { storyId ->
            storyScriptsIndex[storyId]?.remove(scriptId)
        }

        scriptScenesCache.remove(scriptId)
        scriptProjectIndex.remove(scriptId)

        return true
    }

    // Scene Management
    override suspend fun getScriptScenes(scriptId: String): List<SceneScript> {
        return scriptScenesCache[scriptId] ?: emptyList()
    }

    override suspend fun generateScriptScenes(scriptId: String): List<SceneScript> {
        val script = getScript(scriptId) ?: return emptyList()
        val projectId = scriptProjectIndex[scriptId] ?: return emptyList()

        val projectData = SampleProjectsFactory.getProjectById(projectId) ?: return emptyList()
        val characters = projectData.characters
        val story = projectData.stories.find { it.id == script.storyId }

        val structure = try {
            script.structure
        } catch (e: Exception) {
            ScriptStructureType.THREE_ACT
        }

        val scenes = when (structure) {
            ScriptStructureType.THREE_ACT -> generateThreeActScenes(script, characters, story)
            ScriptStructureType.FIVE_ACT -> generateFiveActScenes(script, characters, story)
            ScriptStructureType.HEROES_JOURNEY -> generateHerosJourneyScenes(script, characters, story)
            else -> generateDefaultScenes(script, characters, story)
        }

        scriptScenesCache[scriptId] = scenes.toMutableList()
        updateScriptScenesFlow(scriptId)

        return scenes
    }

    override suspend fun updateScriptScene(scene: SceneScript): SceneScript {
        val scenes = scriptScenesCache[scene.scriptId]
        scenes?.let {
            val index = it.indexOfFirst { s -> s.id == scene.id }
            if (index != -1) {
                it[index] = scene
                updateScriptScenesFlow(scene.scriptId)
            }
        }
        return scene
    }

    override suspend fun deleteScriptScene(sceneId: String): Boolean {
        scriptScenesCache.forEach { (scriptId, scenes) ->
            val iterator = scenes.iterator()
            var removed = false

            while (iterator.hasNext()) {
                val scene = iterator.next()
                if (scene.id == sceneId) {
                    iterator.remove()
                    removed = true
                    break
                }
            }

            if (removed) {
                updateScriptScenesFlow(scriptId)
                return true
            }
        }
        return false
    }

    override suspend fun reorderScenes(scriptId: String, sceneIds: List<String>): List<SceneScript> {
        val scenes = scriptScenesCache[scriptId] ?: return emptyList()
        val sceneMap = scenes.associateBy { it.id }

        val reorderedScenes = sceneIds.mapIndexedNotNull { index, id ->
            sceneMap[id]?.copy(number = index + 1)
        }

        scriptScenesCache[scriptId] = reorderedScenes.toMutableList()
        updateScriptScenesFlow(scriptId)

        return reorderedScenes
    }

    // Story-Script Relationships
    override suspend fun linkScriptToStory(scriptId: String, storyId: String): Boolean {
        val script = scriptsCache[scriptId] ?: return false
        val updatedScript = script.copy(storyId)
        return updateScript(updatedScript) == updatedScript
    }

    override suspend fun unlinkScriptFromStory(scriptId: String, storyId: String): Boolean {
        val script = scriptsCache[scriptId] ?: return false
        val updatedScript = script.copy(storyId)
        return updateScript(updatedScript) == updatedScript
    }

    override suspend fun getScriptWithStory(scriptId: String): Pair<Script, Story?>? {
        val script = getScript(scriptId) ?: return null
        val story = script.storyId?.let { contentRepository.getStory(it) }
        return script to story
    }

    // Project Context
    override suspend fun getProjectScriptTree(projectId: String): ProjectScriptTree {
        val project = projectRepository.getProject(projectId).getOrNull()
            ?: throw IllegalArgumentException("Project not found: $projectId")

        val allScripts = getScriptsByProject(projectId)
        val stories = contentRepository.getStories(projectId)

        val storyScriptRelations = stories.map { story ->
            StoryScriptRelation(
                story = story,
                drafts = allScripts.filter { it.storyId == story.id },
                script = allScripts.filter { it.storyId == story.id && it.draftNumber > 0 }?.first())
        }

        val orphanScripts = allScripts.filter { it.storyId == null }

        return ProjectScriptTree(
            projectId = projectId,
            projectTitle = project.title,
            stories = storyScriptRelations,
            orphanScripts = orphanScripts
        )
    }

    override suspend fun generateScriptFromStory(storyId: String, projectId: String): Script {
        val story = contentRepository.getStory(storyId)
            ?: throw IllegalArgumentException("Story not found: $storyId")

        val now = Clock.System.now()
        val script = Script(
            id = "script_${now.toEpochMilliseconds()}",
            projectId = projectId,
            storyId = storyId,
            title = "${story.title} - Screenplay",
            content = "",
            format = ScriptFormat.SCREENPLAY, // Would be AI-generated
            createdAt = now,
            updatedAt = now,
            lastEditedBy = "AI Assistant",
            sceneScripts = emptyList(),
            props = emptyList()
        )

        return createScript(script)
    }

    override suspend fun extractCharactersFromScript(scriptId: String): List<String> {
        return emptyList()
    }

    // Text Generation
    override suspend fun generateScriptText(scene: SceneScript): String {
        return buildString {
            appendLine("SCENE ${scene.number}")
            appendLine()

            val intExt = if (scene.location?.contains("INT") == true ||
                scene.location?.contains("inside", ignoreCase = true) == true) "INT." else "EXT."
            appendLine("$intExt ${scene.location?.uppercase() ?: "LOCATION"} - ${scene.timeOfDay?.uppercase() ?: "DAY"}")
            appendLine()

            scene.cameraDirections?.forEach { direction ->
                appendLine(direction)
            }
            if (!scene.cameraDirections.isNullOrEmpty()) appendLine()

            appendLine(scene.description)
            appendLine()

            if (!scene.propIds.isNullOrEmpty()) {
                appendLine("(Props: ${scene.propIds.joinToString(", ")})")
                appendLine()
            }

            scene.dialogue.forEach { line ->
                appendLine("${line.characterName.uppercase()}")
                appendLine("(${line.emotion})")
                appendLine(line.dialogue)
                appendLine()
            }

            if (scene.narrativeFunction != NarrativeFunction.RESOLUTION) {
                appendLine("CUT TO:")
            } else {
                appendLine("FADE OUT.")
            }
        }
    }

    override suspend fun generateFullScriptText(scriptId: String): String {
        val script = getScript(scriptId) ?: return ""
        val scenes = getScriptScenes(scriptId)
        val projectId = scriptProjectIndex[scriptId] ?: return ""
        val project = projectRepository.getProject(projectId).getOrNull() ?: return ""

        return buildString {
            appendLine(script.title.uppercase())
            appendLine()
            appendLine("Written by")
            appendLine(script.lastEditedBy)
            appendLine()
            appendLine("Based on: ${script.storyId?.let { "Story ID: $it" } ?: "Original Screenplay"}")
            appendLine()
            appendLine("Project: ${project.title}")
            appendLine("Draft: ${try { script.draftNumber } catch (e: Exception) { 1 }}")
            appendLine("Date: ${Clock.System.now()}")
            appendLine()
            appendLine("FADE IN:")
            appendLine()

            scenes.forEach { scene ->
                append(generateScriptText(scene))
                appendLine()
            }

            if (scenes.isEmpty()) {
                appendLine("(No scenes generated yet)")
            }
            appendLine()
            appendLine("THE END")
        }
    }

    override suspend fun exportScriptToFormat(scriptId: String, format: ScriptExportFormat): String {
        // Simple implementation - in real app would use proper formatters
        return when (format) {
            ScriptExportFormat.FOUNTAIN -> generateFullScriptText(scriptId)
            ScriptExportFormat.PDF -> "PDF export not implemented"
            ScriptExportFormat.FINAL_DRAFT -> "Final Draft export not implemented"
            ScriptExportFormat.CELTX -> "Celtx export not implemented"
            ScriptExportFormat.PLAIN_TEXT -> generateFullScriptText(scriptId)
        }
    }

    // Analysis
    override suspend fun analyzeScriptStructure(scriptId: String): ScriptAnalysis {
        val script = getScript(scriptId) ?: throw IllegalArgumentException("Script not found")
        val scenes = getScriptScenes(scriptId)

        return ScriptAnalysis(
            scriptId = scriptId,
            structureType = try { script.structure } catch (e: Exception) { ScriptStructureType.THREE_ACT },
            structureScore = 0.75f,
            suggestions = listOf(
                "Consider adding more conflict in Act 2",
                "Character arc could be stronger",
                "Pacing in middle section needs work"
            )
        )
    }

    override suspend fun getScriptMetrics(scriptId: String): ScriptMetrics {
        val scenes = getScriptScenes(scriptId)
        val totalDuration = scenes.sumOf { it.duration }
        val wordCount = scenes.sumOf { scene ->
            scene.dialogue.sumOf { it.dialogue.split(" ").size }
        }

        return ScriptMetrics(
            totalScenes = scenes.size,
            totalWords = wordCount,
            estimatedDuration = totalDuration / 60, // Convert to minutes
            charactersCount = scenes.flatMap { it.characterIds }.distinct().size,
            dialoguePercentage = 0.65f,
            completionPercentage = scenes.count { it.isCompleted }.toFloat() / scenes.size
        )
    }

    override suspend fun analyzeDialogue(scriptId: String): DialogueAnalysis {
        val scenes = getScriptScenes(scriptId)
        val characterDialogueCount = mutableMapOf<String, Int>()

        scenes.forEach { scene ->
            scene.dialogue.forEach { line ->
                characterDialogueCount[line.characterName] =
                    (characterDialogueCount[line.characterName] ?: 0) + 1
            }
        }

        val avgLineLength = scenes.flatMap { it.dialogue }
            .map { it.dialogue.length }
            .average().toFloat()

        return DialogueAnalysis(
            scriptId = scriptId,
            characterDialogueCount = characterDialogueCount,
            averageLineLength = avgLineLength,
            dialoguePacing = "Moderate",
            suggestions = listOf("Consider varying dialogue length", "Add more subtext")
        )
    }

    // Real-time updates
    override fun observeProjectScripts(projectId: String): Flow<List<Script>> {
        return projectScriptsFlow.getOrPut(projectId) {
            MutableStateFlow(emptyList())
        }.asStateFlow()
    }

    override fun observeScriptScenes(scriptId: String): Flow<List<SceneScript>> {
        return scriptScenesFlow.getOrPut(scriptId) {
            MutableStateFlow(emptyList())
        }.asStateFlow()
    }

    override fun observeScriptUpdates(scriptId: String): Flow<Script?> {
        return flow {
            while (true) {
                emit(scriptsCache[scriptId])
            }
        }
    }

    // Scene generation helpers
    private fun generateThreeActScenes(
        script: Script,
        characters: List<CharacterProfile>,
        story: Story?
    ): List<SceneScript> {
        val protagonist = characters.find { it.role == CharacterRole.PROTAGONIST }
        val antagonist = characters.find { it.role == CharacterRole.ANTAGONIST }
        val supporting = characters.filter { it.role == CharacterRole.SUPPORTING }

        return listOf(
            SceneScript(
                id = "${script.id}_scene_1",
                scriptId = script.id,
                number = 1,
                emotionalTone = EmotionalTone.MYSTERIOUS,
                narrativeFunction = NarrativeFunction.EXPOSITION,
                dialogue = listOf(
                    DialogueLine(
                        id = "dl_ex_1_1_1",
                        characterName = protagonist?.name ?: "PROTAGONIST",
                        characterId = protagonist?.name.toString(),
                        dialogue = "This world... it's changing faster than any of us expected.",
                        emotion = EmotionalTone.CONTEMPLATIVE,
                        timestamp = 0f
                    )
                ),
                title = "Opening - ${protagonist?.name ?: "Hero"}'s World",
                description = story?.synopsis?.take(100) ?: "Introduction to the protagonist's ordinary world",
                duration = 120,
                isCompleted = true,
                location = story?.setting ?: "City",
                timeOfDay = "Dawn",
                characterIds = listOf(protagonist?.id!!, antagonist?.id!!),
                cameraDirections = listOf(
                    "FADE IN:",
                    "EXT. ${story?.setting ?: "CITY"} - DAWN",
                    "Wide establishing shot of the city awakening"
                ),
            ),
            SceneScript(
                id = "${script.id}_scene_2",
                scriptId = script.id,
                number = 2,
                emotionalTone = EmotionalTone.INTRIGUING,
                narrativeFunction = NarrativeFunction.INCITING_INCIDENT,
                dialogue = generateIncitingDialogue(protagonist, supporting),
                title = "The Call to Adventure",
                description = "An event disrupts the protagonist's normal life",
                duration = 180,
                characterIds = listOfNotNull(
                    protagonist?.id,
                    supporting.getOrNull(1)?.id
                ),
                isCompleted = true,
                location = "Office",
                timeOfDay = "Morning"
            ),

            SceneScript(
                id = "${script.id}_scene_3",
                scriptId = script.id,
                number = 3,
                emotionalTone = EmotionalTone.TENSE,
                narrativeFunction = NarrativeFunction.RISING_ACTION,
                dialogue = emptyList(), // TODO: have to generate this
                title = "First Obstacle",
                description = "The protagonist faces their first major challenge",
                duration = 240,
                location = "Abandoned Warehouse",
                characterIds = listOf(protagonist?.id!!, antagonist?.id!!),
                timeOfDay = "Night"
            )
        )
    }

    // Stub implementations for other structures
    private fun generateFiveActScenes(script: Script, characters: List<CharacterProfile>, story: Story?): List<SceneScript> {
        // Similar to three act but with 5 major sections
        return generateThreeActScenes(script, characters, story) // Simplified for now
    }

    private fun generateHerosJourneyScenes(script: Script, characters: List<CharacterProfile>, story: Story?): List<SceneScript> {
        // Would implement 12-stage hero's journey
        return generateThreeActScenes(script, characters, story) // Simplified for now
    }

    private fun generateDefaultScenes(script: Script, characters: List<CharacterProfile>, story: Story?): List<SceneScript> {
        return generateThreeActScenes(script, characters, story)
    }

    private fun generateIncitingDialogue(
        protagonist: CharacterProfile?,
        supporting: List<CharacterProfile>
    ): List<DialogueLine> {
        val supportChar = supporting.firstOrNull()
        return listOf(
            DialogueLine(
                id = "dl_ex_1_1_2",
                characterName = supportChar?.name ?: "MESSENGER",
                characterId = "",
                dialogue = "You need to see this. Everything we knew... it's all wrong.",
                emotion = EmotionalTone.INTENSE,
                timestamp = 0f
            ),
            DialogueLine(
                id = "dl_ex_1_1_3",
                characterName = protagonist?.name ?: "PROTAGONIST",
                characterId = "",
                dialogue = "That's impossible. I verified everything myself.",
                emotion = EmotionalTone.DISBELIEF,
                timestamp = 3f
            )
        )
    }

    // Helper functions
    private fun updateProjectScriptsFlow(projectId: String) {
        val scripts = projectScriptsIndex[projectId]?.mapNotNull { scriptsCache[it] } ?: emptyList()
        projectScriptsFlow.getOrPut(projectId) { MutableStateFlow(emptyList()) }.value = scripts
    }

    private fun updateScriptScenesFlow(scriptId: String) {
        val scenes = scriptScenesCache[scriptId] ?: emptyList()
        scriptScenesFlow.getOrPut(scriptId) { MutableStateFlow(emptyList()) }.value = scenes
    }
}