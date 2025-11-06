// File: shared/src/androidMain/kotlin/com/cinefiller/fillerapp/domain/repository/ObjectBoxRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.data.*
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.project.*
import com.beekeeper.app.presentation.viewmodels.GenerationPlatform
import com.beekeeper.app.utils.getCurrentTimeMillis
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

// JSON serializer for complex objects
private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
// For metadata fields - Convert Map to String representation
fun Map<String, String>.toMetadataString(): String {
    return this.entries.joinToString("|") { "${it.key}:${it.value}" }
}

// Convert String back to Map
fun String.toMetadataMap(): Map<String, String> {
    if (this.isEmpty()) return emptyMap()
    return this.split("|").associate {
        val parts = it.split(":", limit = 2)
        parts.getOrNull(0).orEmpty() to parts.getOrNull(1).orEmpty()
    }
}
/**
 * Actual implementation of ObjectBoxRepository for Android
 * Handles conversion between domain models and ObjectBox entities
 */
actual class ObjectBoxRepository(private val boxStore: BoxStore) {

    private lateinit var propIds: String

    // Box references for each entity type
    private val projectBox: Box<ProjectEntity> = boxStore.boxFor()
    private val storyBox: Box<StoryEntity> = boxStore.boxFor()
    private val scriptBox: Box<ScriptEntity> = boxStore.boxFor()
    private val actBox: Box<ActEntity> = boxStore.boxFor()
    private val sceneScriptBox: Box<SceneScriptEntity> = boxStore.boxFor()
    private val dialogueLineBox: Box<DialogueLineEntity> = boxStore.boxFor()
    private val storyboardBox: Box<StoryboardEntity> = boxStore.boxFor()
    private val sceneBox: Box<SceneEntity> = boxStore.boxFor()
    private val frameBox: Box<FrameEntity> = boxStore.boxFor()
    private val characterBox: Box<CharacterEntity> = boxStore.boxFor()
    private val deliverableBox: Box<DeliverableEntity> = boxStore.boxFor()
    private val avatarBox: Box<AvatarEntity> = boxStore.boxFor()
    private val contentBox: Box<ContentEntity> = boxStore.boxFor()
    private val feedPostBox: Box<FeedPostEntity> = boxStore.boxFor()
    private val notificationBox: Box<NotificationEntity> = boxStore.boxFor()
    private val userBox: Box<UserEntity> = boxStore.boxFor()
    private val publishingProjectBox: Box<PublishingProjectEntity> = boxStore.boxFor()
    private val characterRelationshipBox: Box<CharacterRelationshipEntity> = boxStore.boxFor()

    // Project operations
    suspend fun saveProject(project: CreativeProject) {
        val entity = project.toEntity()
        projectBox.put(entity)
    }

    suspend fun getProject(projectId: String): CreativeProject? {
        return projectBox.all.firstOrNull { it.projectId == projectId }?.toDomainModel()
    }

    suspend fun getAllProjects(): List<CreativeProject> {
        return projectBox.all.map { it.toDomainModel() }
    }

    // Story operations
    suspend fun saveStory(story: Story) {
        val entity = story.toEntity()
        storyBox.put(entity)
    }

    suspend fun getStory(storyId: String): Story? {
        return storyBox.all.firstOrNull { it.storyId == storyId }?.toDomainModel()
    }

    suspend fun getStoriesByProject(projectId: String): List<Story> {
        return storyBox.all.filter { it.projectIdString == projectId }.map { it.toDomainModel() }
    }

    // Script operations
    suspend fun saveScript(script: Script) {
        val entity = script.toEntity()
        scriptBox.put(entity)

        // Save acts
        script.acts.forEach { act ->
            saveAct(act)
        }

        // Save direct scenes
        script.sceneScripts.forEach { scene ->
            saveSceneScript(scene)
        }
    }

    suspend fun getScript(scriptId: String): Script? {
        return scriptBox.all.firstOrNull { it.scriptId == scriptId }?.toDomainModel()
    }

    suspend fun getScriptsForProject(projectId: String): List<Script> {
        return scriptBox.all.filter { it.projectIdString == projectId }.map { it.toDomainModel() }
    }

    suspend fun getProjectCount(): Long {
        return projectBox.count()
    }

    suspend fun clearAllData() {
        // Clear all boxes in reverse dependency order
        dialogueLineBox.removeAll()
        sceneScriptBox.removeAll()
        actBox.removeAll()
        scriptBox.removeAll()
        frameBox.removeAll()
        sceneBox.removeAll()
        storyboardBox.removeAll()
        characterRelationshipBox.removeAll()
        characterBox.removeAll()
        storyBox.removeAll()
        contentBox.removeAll()
        avatarBox.removeAll()
        deliverableBox.removeAll()
        feedPostBox.removeAll()
        notificationBox.removeAll()
        publishingProjectBox.removeAll()
        projectBox.removeAll()
        userBox.removeAll()
    }

    suspend fun getStoriesForProject(projectId: String): List<Story> {
        return getStoriesByProject(projectId)
    }

    // Act operations
    suspend fun saveAct(act: Act) {
        val entity = ActEntity().apply {
            actId = act.id
            scriptIdString = act.scriptId
            actNumber = act.actNumber
            title = act.title ?: ""
            description = act.description ?: ""
            duration = act.duration
            purpose = act.purpose
            turningPoint = act.turningPoint
            emotionalArc = act.emotionalArc
            themes = json.encodeToString(act.themes)
            notes = act.notes
            colorCode = act.colorCode
            isFlashback = act.isFlashback
            isFlashforward = act.isFlashforward
            parallelAction = json.encodeToString(act.parallelAction)
            metadata = json.encodeToString(act.metadata)
            pageStart = act.pageStart
            pageEnd = act.pageEnd
            sceneCount = act.sceneScripts.size
        }
        actBox.put(entity)

        // Save scenes
        act.sceneScripts.forEach { scene ->
            saveSceneScript(scene)
        }
    }

    // SceneScript operations
    suspend fun saveSceneScript(sceneScript: SceneScript) {
        val entity = SceneScriptEntity().apply {
            sceneScriptId = sceneScript.id
            scriptIdString = sceneScript.scriptId
            actIdString = sceneScript.actId
            sceneNumber = sceneScript.number
            sceneNumberStr = sceneScript.sceneNumber
            heading = sceneScript.heading
            action = sceneScript.action
            emotionalTone = sceneScript.emotionalTone.ordinal
            narrativeFunction = sceneScript.narrativeFunction.ordinal
            title = sceneScript.title
            description = sceneScript.description
            duration = sceneScript.duration
            characterIds = json.encodeToString(sceneScript.characterIds)
            isCompleted = sceneScript.isCompleted
            location = sceneScript.location
            timeOfDay = sceneScript.timeOfDay
            transitions = sceneScript.transitions?.let { json.encodeToString(it) }
            metadata = json.encodeToString(sceneScript.metadata ?: emptyMap<String, String>())
            pageStart = sceneScript.pageStart
            pageEnd = sceneScript.pageEnd
            beatSheet = json.encodeToString(sceneScript.beatSheet)
            subheading = sceneScript.subheading
            propIds = json.encodeToString(sceneScript.propIds ?: emptyList<String>())
            cameraDirections = json.encodeToString(sceneScript.cameraDirections ?: emptyList<String>())
            notes = sceneScript.notes
        }
        sceneScriptBox.put(entity)

        // Save dialogue lines
        sceneScript.dialogue.forEach { dialogueLine ->
            saveDialogueLine(dialogueLine, sceneScript.id)
        }
    }

    // DialogueLine operations
    suspend fun saveDialogueLine(dialogueLine: DialogueLine, sceneScriptId: String) {
        val entity = DialogueLineEntity().apply {
            dialogueId = dialogueLine.id
            sceneScriptIdString = sceneScriptId
            characterName = dialogueLine.characterName
            dialogue = dialogueLine.dialogue
            characterId = dialogueLine.characterId
            parenthetical = dialogueLine.parenthetical
            isDualDialogue = dialogueLine.isDualDialogue
            emotion = dialogueLine.emotion.ordinal
            timestamp = dialogueLine.timestamp
            lineNumber = dialogueLine.lineNumber
            revisionColor = dialogueLine.revisionColor
            isVoiceOver = dialogueLine.isVoiceOver
            isOffScreen = dialogueLine.isOffScreen
            isPreLap = dialogueLine.isPreLap
            isContinued = dialogueLine.isContinued
            isAdLib = dialogueLine.isAdLib
            deliveryNote = dialogueLine.deliveryNote
            dialect = dialogueLine.dialect
            language = dialogueLine.language
            subtitleRequired = dialogueLine.subtitleRequired
            alternateLines = json.encodeToString(dialogueLine.alternateLines)
            audioFileId = dialogueLine.audioFileId
            duration = dialogueLine.duration
            overlapping = dialogueLine.overlapping
            emphasis = json.encodeToString(dialogueLine.emphasis)
            pronunciation = json.encodeToString(dialogueLine.pronunciation)
            culturalNote = dialogueLine.culturalNote
            censorshipNote = dialogueLine.censorshipNote
            metadata = json.encodeToString(dialogueLine.metadata ?: emptyMap<String, String>())
        }
        dialogueLineBox.put(entity)
    }

    suspend fun getDialogueLine(dialogueId: String): DialogueLine? {
        return dialogueLineBox.all.firstOrNull { it.dialogueId == dialogueId }?.let { entity ->
            DialogueLine(
                id = entity.dialogueId,
                characterName = entity.characterName,
                dialogue = entity.dialogue,
                characterId = entity.characterId!!,
                parenthetical = entity.parenthetical,
                isDualDialogue = entity.isDualDialogue,
                emotion = EmotionalTone.values()[entity.emotion],
                timestamp = entity.timestamp,
                lineNumber = entity.lineNumber,
                revisionColor = entity.revisionColor,
                isVoiceOver = entity.isVoiceOver,
                isOffScreen = entity.isOffScreen,
                isPreLap = entity.isPreLap,
                isContinued = entity.isContinued,
                isAdLib = entity.isAdLib,
                deliveryNote = entity.deliveryNote,
                dialect = entity.dialect,
                language = entity.language,
                subtitleRequired = entity.subtitleRequired,
                alternateLines = json.decodeFromString(entity.alternateLines),
                audioFileId = entity.audioFileId,
                duration = entity.duration,
                overlapping = entity.overlapping,
                emphasis = json.decodeFromString(entity.emphasis),
                pronunciation = json.decodeFromString(entity.pronunciation),
                culturalNote = entity.culturalNote,
                censorshipNote = entity.censorshipNote,
                metadata = entity.metadata.toMetadataMap(),
            )
        }
    }

    // CharacterProfile operations (NOT Character!)
    suspend fun saveCharacter(character: CharacterProfile) {
        val entity = character.toEntity()
        characterBox.put(entity)

        // Save relationships
        character.relationships.forEach { relationship ->
            val relationEntity = CharacterRelationshipEntity().apply {
                sourceCharacterIdString = character.id
                targetCharacterId = relationship.targetCharacterId
                targetCharacterName = relationship.targetCharacterName
                relationshipType = 0 // Placeholder - ObjectBox is deprecated, using SQLDelight instead
                description = relationship.description
                strength = relationship.strength
            }
            characterRelationshipBox.put(relationEntity)
        }
    }

    suspend fun getCharacter(characterId: String): CharacterProfile? {
        return characterBox.all.firstOrNull { it.characterId == characterId }?.toDomainModel()
    }

    suspend fun getCharactersByProject(projectId: String): List<CharacterProfile> {
        return characterBox.all.filter { it.projectIdString == projectId }.map { it.toDomainModel() }
    }

    suspend fun getCharactersForProject(projectId: String): List<CharacterProfile> {
        return getCharactersByProject(projectId)
    }

    // Storyboard operations
    suspend fun saveStoryboard(storyboard: Storyboard) {
        val entity = storyboard.toEntity()
        storyboardBox.put(entity)

        // Save scenes
        storyboard.scenes.forEach { scene ->
            saveScene(scene)
        }
    }

    suspend fun getStoryboard(storyboardId: String): Storyboard? {
        return storyboardBox.all.firstOrNull { it.storyboardId == storyboardId }?.toDomainModel()
    }

    // Scene operations
    suspend fun saveScene(scene: Scene) {
        val entity = scene.toEntity()
        sceneBox.put(entity)

        // Save frames
        scene.frames.forEach { frame ->
            saveFrame(frame)
        }
    }

    suspend fun getScenesByStoryboard(storyboardId: String): List<Scene> {
        return sceneBox.all.filter { it.storyboardIdString == storyboardId }.map { it.toDomainModel() }
    }

    // Frame operations
    suspend fun saveFrame(frame: Frame) {
        val entity = frame.toEntity()
        frameBox.put(entity)
    }

    suspend fun getAllFramesForProject(projectId: String): List<Frame> {
        // Get all storyboards for the project
        val storyboards = storyboardBox.all.filter { it.projectIdString == projectId }
        val storyboardIds = storyboards.map { it.storyboardId }

        // Get all scenes for those storyboards
        val scenes = sceneBox.all.filter { scene ->
            storyboardIds.contains(scene.storyboardIdString)
        }
        val sceneIds = scenes.map { it.sceneId }

        // Get all frames for those scenes
        return frameBox.all.filter { frame ->
            sceneIds.contains(frame.sceneIdString)
        }.map { it.toDomainModel() }
    }

    // Content operations
    suspend fun saveContent(content: Content) {
        val entity = content.toEntity()
        contentBox.put(entity)
    }

    suspend fun getContent(contentId: String): Content? {
        return contentBox.all.firstOrNull { it.contentId == contentId }?.toDomainModel()
    }

    suspend fun getContentByProject(projectId: String): List<Content> {
        return contentBox.all.filter { it.projectIdString == projectId }.map { it.toDomainModel() }
    }

    // Avatar operations
    suspend fun saveAvatar(avatar: Avatar) {
        val entity = avatar.toEntity()
        avatarBox.put(entity)
    }

    suspend fun getAvatar(avatarId: String): Avatar? {
        return avatarBox.all.firstOrNull { it.avatarId == avatarId }?.toDomainModel()
    }

    // User operations
    suspend fun saveUser(user: User) {
        val entity = user.toEntity()
        userBox.put(entity)
    }

    suspend fun getUser(userId: String): User? {
        return userBox.all.firstOrNull { it.userId == userId }?.toDomainModel()
    }

    // Publishing Project operations
    suspend fun savePublishingProject(project: PublishingProject) {
        val entity = project.toEntity()
        publishingProjectBox.put(entity)
    }

    suspend fun getPublishingProject(projectId: String): PublishingProject? {
        return publishingProjectBox.all.firstOrNull { it.projectId == projectId }?.toDomainModel()
    }

    // Flow operations for reactive updates
    fun getProjectsFlow(): Flow<List<CreativeProject>> = flow {
        emit(getAllProjects())
    }

    fun getCharactersFlow(projectId: String): Flow<List<CharacterProfile>> = flow {
        emit(getCharactersByProject(projectId))
    }
}

// ===== CONVERSION EXTENSIONS =====

// Project conversions
fun CreativeProject.toEntity(): ProjectEntity {
    return ProjectEntity().apply {
        projectId = this@toEntity.id
        title = this@toEntity.title
        description = this@toEntity.description
        type = this@toEntity.type.ordinal
        status = this@toEntity.status.ordinal
        currentPhase = this@toEntity.currentPhase.ordinal
        priority = this@toEntity.priority.ordinal
    }
}

fun ProjectEntity.toDomainModel(): CreativeProject {
    return CreativeProject(
        id = projectId,
        title = title,
        description = description,
        type = ProjectType.values()[type],
        status = ProjectStatus.values()[status],
        currentPhase = ProductionPhase.values()[currentPhase],
        priority = ProjectPriority.values()[priority]
    )
}

// Story conversions
fun Story.toEntity(): StoryEntity {
    return StoryEntity().apply {
        storyId = this@toEntity.id
        projectIdString = this@toEntity.projectId
        scriptId = this@toEntity.scriptId
        title = this@toEntity.title
        logline = json.encodeToString(this@toEntity.logline)
        synopsis = this@toEntity.synopsis
        genre = this@toEntity.genre
        themes = json.encodeToString(this@toEntity.themes)
        setting = this@toEntity.setting ?: ""
        targetAudience = json.encodeToString(listOfNotNull(this@toEntity.targetAudience))
        status = this@toEntity.status.ordinal
        createdAt = this@toEntity.createdAt
        updatedAt = this@toEntity.updatedAt
        acts = json.encodeToString(this@toEntity.acts)
        mainCharacters = json.encodeToString(emptyList<String>())
        storyboardIds = json.encodeToString(this@toEntity.storyboardIds)
        duration = this@toEntity.duration
        lastEditedBy = this@toEntity.lastEditedBy
        episodeNumber = this@toEntity.episodeNumber
        seasonNumber = this@toEntity.seasonNumber
        episodeCode = this@toEntity.episodeCode
        episodeTitle = this@toEntity.episodeTitle
        isStandalone = this@toEntity.isStandalone
        sequenceOrder = this@toEntity.sequenceOrder
        storyCategory = this@toEntity.storyCategory.name
        estimatedBudget = this@toEntity.estimatedBudget
        metadata = json.encodeToString(this@toEntity.metadata)
        exportFormats = json.encodeToString(this@toEntity.exportFormats)
    }
}

fun StoryEntity.toDomainModel(): Story {
    return Story(
        id = storyId,
        projectId = projectIdString,
        scriptId = scriptId ?: "",
        title = title,
        synopsis = synopsis,
        genre = genre,
        themes = json.decodeFromString(themes),
        setting = setting,
        createdAt = createdAt,
        updatedAt = updatedAt,
        status = ContentStatus.values()[status],
        duration = duration,
        lastEditedBy = lastEditedBy,
        episodeNumber = episodeNumber,
        seasonNumber = seasonNumber,
        episodeCode = episodeCode,
        episodeTitle = episodeTitle,
        isStandalone = isStandalone,
        sequenceOrder = sequenceOrder,
        storyboardIds = json.decodeFromString(storyboardIds),
        storyCategory = StoryCategory.valueOf(storyCategory),
        targetAudience = json.decodeFromString<List<String>>(targetAudience).firstOrNull(),
        estimatedBudget = estimatedBudget,
        acts = json.decodeFromString(acts),
        metadata = json.decodeFromString(metadata),
        exportFormats = json.decodeFromString(exportFormats),
        logline = json.decodeFromString(logline)
    )
}

// Script conversions
fun Script.toEntity(): ScriptEntity {
    return ScriptEntity().apply {
        scriptId = this@toEntity.id
        projectIdString = this@toEntity.projectId
        storyIdString = this@toEntity.storyId
        title = this@toEntity.title
        content = this@toEntity.content
        format = this@toEntity.format.name
        writtenBy = this@toEntity.writtenBy
        lastEditedBy = this@toEntity.lastEditedBy
        createdAt = this@toEntity.createdAt.toEpochMilliseconds()
        updatedAt = this@toEntity.updatedAt.toEpochMilliseconds()
        draft = this@toEntity.draftNumber
        sceneCount = this@toEntity.sceneScripts.size
        characterCount = this@toEntity.characterCount
        pageCount = this@toEntity.pages
        estimatedDuration = this@toEntity.estimatedDuration
        version = this@toEntity.version
        pages = this@toEntity.pages
        wordCount = this@toEntity.wordCount
        duration = this@toEntity.duration
        collaborators = json.encodeToString(this@toEntity.collaborators)
        isLocked = this@toEntity.isLocked
        status = this@toEntity.status.ordinal
        structure = this@toEntity.structure
        draftNumber = this@toEntity.draftNumber
        language = this@toEntity.language
        genre = json.encodeToString(this@toEntity.genre)
        targetAudience = this@toEntity.targetAudience
        rating = this@toEntity.rating
        logline = this@toEntity.logline
        synopsis = this@toEntity.synopsis
        treatment = this@toEntity.treatment
        locationCount = this@toEntity.locationCount
        dayScenes = this@toEntity.dayScenes
        nightScenes = this@toEntity.nightScenes
        interiorScenes = this@toEntity.interiorScenes
        exteriorScenes = this@toEntity.exteriorScenes
        revisionHistory = json.encodeToString(this@toEntity.revisionHistory)
        colorRevision = this@toEntity.colorRevision
        shootingScriptNumber = this@toEntity.shootingScriptNumber
        registrationNumber = this@toEntity.registrationNumber
        copyrightInfo = this@toEntity.copyrightInfo
        contactInfo = this@toEntity.contactInfo
        agent = this@toEntity.agent
        notes = json.encodeToString(this@toEntity.notes)
        breakdowns = this@toEntity.breakdowns?.let { json.encodeToString(it) }
        budget = this@toEntity.budget?.let { json.encodeToString(it) }
        schedule = this@toEntity.schedule?.let { json.encodeToString(it) }
        metadata = json.encodeToString(this@toEntity.metadata)
        revisionNotes = this@toEntity.revisionNotes
        themes = json.encodeToString(this@toEntity.themes)
        props = this@toEntity.props.let { json.encodeToString(it) }
    }
}

fun ScriptEntity.toDomainModel(): Script {
    return Script(
        id = scriptId,
        projectId = projectIdString,
        storyId = storyIdString,
        title = title,
        version = version,
        content = content,
        format = ScriptFormat.valueOf(format),
        pages = pages,
        wordCount = wordCount,
        duration = duration,
        estimatedDuration = estimatedDuration,
        sceneScripts = emptyList(), // Load separately
        acts = emptyList(), // Load separately
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        lastEditedBy = lastEditedBy,
        collaborators = json.decodeFromString(collaborators),
        isLocked = isLocked,
        status = ContentStatus.values()[status],
        structure = structure,
        draftNumber = draftNumber,
        writtenBy = writtenBy,
        language = language,
        genre = json.decodeFromString(genre),
        targetAudience = targetAudience,
        rating = rating,
        logline = logline,
        synopsis = synopsis,
        treatment = treatment,
        characterCount = characterCount,
        locationCount = locationCount,
        dayScenes = dayScenes,
        nightScenes = nightScenes,
        interiorScenes = interiorScenes,
        exteriorScenes = exteriorScenes,
        revisionHistory = json.decodeFromString(revisionHistory),
        colorRevision = colorRevision,
        shootingScriptNumber = shootingScriptNumber,
        registrationNumber = registrationNumber,
        copyrightInfo = copyrightInfo,
        contactInfo = contactInfo,
        agent = agent,
        notes = json.decodeFromString(notes),
        breakdowns = breakdowns?.let { json.decodeFromString(it) },
        budget = budget?.let { json.decodeFromString(it) },
        schedule = schedule?.let { json.decodeFromString(it) },
        metadata = json.decodeFromString(metadata),
        revisionNotes = revisionNotes,
        themes = json.decodeFromString(themes),
        props = json.decodeFromString(props)
    )
}

// CharacterProfile conversions (NOT Character!)
fun CharacterProfile.toEntity(): CharacterEntity {
    return CharacterEntity().apply {
        characterId = this@toEntity.id
        projectIdString = this@toEntity.projectId
        name = this@toEntity.name
        fullName = this@toEntity.name
        role = this@toEntity.role.name
        description = this@toEntity.description ?: ""
        archetype = this@toEntity.archetype
        personality = json.encodeToString(this@toEntity.personality)
        assignedAvatarId = this@toEntity.assignedAvatarId
        screenTime = this@toEntity.screenTime
        dialogueCount = this@toEntity.dialogueCount
        age = this@toEntity.age
        height = this@toEntity.height
        gender = this@toEntity.gender.name
        build = this@toEntity.build
        hairColor = this@toEntity.hairColor
        eyeColor = this@toEntity.eyeColor
        distinctiveFeatures = json.encodeToString(this@toEntity.distinctiveFeatures)
        voiceProfile = this@toEntity.voiceProfile?.let { json.encodeToString(it) }
        physicalAttributes = json.encodeToString(this@toEntity.physicalAttributes)
        imageUrl = this@toEntity.imageUrl
        createdAt = this@toEntity.createdAt.toEpochMilliseconds()
        updatedAt = this@toEntity.updatedAt.toEpochMilliseconds()
        metadata = json.encodeToString(this@toEntity.metadata)
        characterRole = this@toEntity.role.ordinal
    }
}

fun CharacterEntity.toDomainModel(): CharacterProfile {
    val relationships = relationships.map { relationEntity ->
        CharacterRelationship(
            targetCharacterId = relationEntity.targetCharacterId,
            targetCharacterName = relationEntity.targetCharacterName,
            relationshipType = "Unknown", // Placeholder - ObjectBox is deprecated, using SQLDelight instead
            description = relationEntity.description,
            strength = relationEntity.strength
        )
    }

    return CharacterProfile(
        id = characterId,
        projectId = projectIdString,
        name = name,
        role = CharacterRole.values()[characterRole],
        archetype = archetype,
        description = description,
        personality = json.decodeFromString(personality),
        relationships = relationships,
        assignedAvatarId = assignedAvatarId,
        screenTime = screenTime,
        dialogueCount = dialogueCount,
        age = age,
        height = height,
        gender = Gender.valueOf(gender),
        build = build,
        hairColor = hairColor,
        eyeColor = eyeColor,
        distinctiveFeatures = json.decodeFromString(distinctiveFeatures),
        voiceProfile = voiceProfile?.let { json.decodeFromString(it) },
        physicalAttributes = json.decodeFromString(physicalAttributes!!),
        imageUrl = imageUrl,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        metadata = json.decodeFromString(metadata)
    )
}

// Storyboard conversions
fun Storyboard.toEntity(): StoryboardEntity {
    return StoryboardEntity().apply {
        storyboardId = this@toEntity.id
        projectIdString = this@toEntity.projectId
        storyIdString = this@toEntity.storyId
        scriptIdString = this@toEntity.scriptId
        title = this@toEntity.title
        description = this@toEntity.description
        sceneCount = this@toEntity.sceneCount
        duration = this@toEntity.duration
        createdAt = this@toEntity.createdAt.toEpochMilliseconds()
        updatedAt = this@toEntity.updatedAt.toEpochMilliseconds()
        createdBy = this@toEntity.createdBy
        version = this@toEntity.version
        isLocked = this@toEntity.isLocked
        thumbnailUrl = this@toEntity.thumbnailUrl
        completionPercentage = this@toEntity.completionPercentage
        storyboardType = this@toEntity.storyboardType.ordinal
        aspectRatio = this@toEntity.aspectRatio.name
        resolution = this@toEntity.resolution.name
        platformSettings = json.encodeToString(this@toEntity.platformSettings)
        sceneScriptIds = json.encodeToString(emptyList<String>()) // TODO: populate from scenes
    }
}

fun StoryboardEntity.toDomainModel(): Storyboard {
    return Storyboard(
        id = storyboardId,
        projectId = projectIdString,
        title = title,
        description = description,
        scenes = scenes.map { it.toDomainModel() },
        sceneCount = sceneCount,
        duration = duration,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        createdBy = createdBy,
        version = version,
        isLocked = isLocked,
        thumbnailUrl = thumbnailUrl,
        completionPercentage = completionPercentage,
        storyId = storyIdString,
        scriptId = scriptIdString,
        storyboardType = StoryboardType.values()[storyboardType],
        aspectRatio = AspectRatio.valueOf(aspectRatio),
        resolution = Resolution.valueOf(resolution),
        platformSettings = json.decodeFromString(platformSettings)
    )
}
// Fixed Scene conversions - ensure all fields are present
fun Scene.toEntity(): SceneEntity {
    return SceneEntity().apply {
        sceneId = this@toEntity.id
        storyboardIdString = this@toEntity.storyboardId
        sceneNumber = this@toEntity.sceneNumber
        title = this@toEntity.title
        description = this@toEntity.description
        sceneScriptId = this@toEntity.sceneScriptId
        duration = this@toEntity.duration
        dialogue = this@toEntity.dialogue
        notes = this@toEntity.notes
        cameraInstructions = this@toEntity.cameraInstructions
        soundEffects = json.encodeToString(this@toEntity.soundEffects)
        musicCues = json.encodeToString(this@toEntity.musicCues)
        imageUrl = this@toEntity.imageUrl
        videoUrl = this@toEntity.videoUrl
        dialogueSnippet = this@toEntity.dialogueSnippet
        cameraDirection = this@toEntity.cameraDirection
        location = this@toEntity.location
        timeOfDay = this@toEntity.timeOfDay
        transitionType = this@toEntity.transitionType.name
        isKeyScene = this@toEntity.isKeyScene
        aiSuggestions = json.encodeToString(this@toEntity.aiSuggestions)
        lightingNotes = this@toEntity.lightingNotes
        soundNotes = this@toEntity.soundNotes
        vfxNotes = this@toEntity.vfxNotes
        transitionIn = this@toEntity.transitionIn.name
        transitionOut = this@toEntity.transitionOut.name

        // Handle optional enums
        shotType = null // Not in Scene domain model
        cameraAngle = null // Not in Scene domain model
        cameraMovement = null // Not in Scene domain model
        audioNotes = null // Not in Scene domain model
        generationSettings = null // Not in Scene domain model
    }
}

fun SceneEntity.toDomainModel(): Scene {
    return Scene(
        id = sceneId,
        storyboardId = storyboardIdString,
        sceneNumber = sceneNumber,
        title = title,
        description = description,
        duration = duration,
        frames = frames.map { it.toDomainModel() },
        dialogue = dialogue,
        notes = notes,
        cameraInstructions = cameraInstructions,
        soundEffects = json.decodeFromString(soundEffects),
        musicCues = json.decodeFromString(musicCues),
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        dialogueSnippet = dialogueSnippet,
        cameraDirection = cameraDirection,
        location = location,
        timeOfDay = timeOfDay,
        transitionType = TransitionType.valueOf(transitionType),
        isKeyScene = isKeyScene,
        aiSuggestions = json.decodeFromString(aiSuggestions),
        sceneScriptId = sceneScriptId,
        lightingNotes = lightingNotes,
        soundNotes = soundNotes,
        vfxNotes = vfxNotes,
        transitionIn = TransitionType.valueOf(transitionIn ?: "CUT"),
        transitionOut = TransitionType.valueOf(transitionOut ?: "CUT")
    )
}

// Frame conversions - CRITICAL FOR DIALOGUE
fun Frame.toEntity(): FrameEntity {
    return FrameEntity().apply {
        frameId = this@toEntity.id
        sceneIdString = this@toEntity.sceneId
        frameNumber = this@toEntity.frameNumber
        duration = this@toEntity.duration
        shotType = this@toEntity.shotType.name
        cameraAngle = this@toEntity.cameraAngle.name
        cameraMovement = this@toEntity.cameraMovement?.name ?: ""
        description = this@toEntity.description ?: ""
        imageUrl = this@toEntity.imageUrl ?: ""
        thumbnailUrl = this@toEntity.thumbnailUrl
        // Handle nullable dialogueLineId properly
        dialogueLineId = this@toEntity.dialogueLineId ?: ""  // Convert null to empty string for database
        action = this@toEntity.action ?: ""
        vfxNotes = this@toEntity.vfxNotes
        audioNotes = this@toEntity.audioNotes
        transitionIn = this@toEntity.transitionIn?.name
        transitionOut = this@toEntity.transitionOut?.name
        generationSettings = this@toEntity.generationSettings?.let { json.encodeToString(it) }
        createdAt = 0 // Add if needed
        updatedAt = 0 // Add if needed
    }
}

fun FrameEntity.toDomainModel(): Frame {
    return Frame(
        id = frameId,
        sceneId = sceneIdString,
        frameNumber = frameNumber,
        imageUrl = imageUrl.takeIf { it.isNotEmpty() },  // Convert empty string to null
        thumbnailUrl = thumbnailUrl,
        description = description,  // Convert empty string to null
        shotType = ShotType.valueOf(shotType),
        cameraAngle = CameraAngle.valueOf(cameraAngle),
        cameraMovement = cameraMovement.takeIf { it.isNotEmpty() }?.let { CameraMovement.valueOf(it) },
        duration = duration,
        // Handle empty string as null for dialogueLineId
        dialogueLineId = dialogueLineId.takeIf { it.isNotEmpty() }, // CRITICAL: Convert empty string to null
        action = action.takeIf { it.isNotEmpty() },
        vfxNotes = vfxNotes,
        audioNotes = audioNotes,
        transitionIn = transitionIn?.let { TransitionType.valueOf(it) },
        transitionOut = transitionOut?.let { TransitionType.valueOf(it) },
        generationSettings = generationSettings?.let { json.decodeFromString(it) }
    )
}

// Avatar conversions
fun Avatar.toEntity(): AvatarEntity {
    return AvatarEntity().apply {
        avatarId = this@toEntity.id
        name = this@toEntity.name
        description = this@toEntity.description ?: ""
        thumbnailUrl = this@toEntity.thumbnailUrl
        fullImageUrl = this@toEntity.fullImageUrl
        videoPreviewUrl = this@toEntity.videoPreviewUrl
        style = this@toEntity.style.name
        generationPlatform = this@toEntity.generationPlatform.name
        generationParams = json.encodeToString(this@toEntity.generationParams)
        tags = json.encodeToString(this@toEntity.tags)
        createdAt = this@toEntity.createdAt.toEpochMilliseconds()
        updatedAt = this@toEntity.updatedAt.toEpochMilliseconds()
        createdBy = this@toEntity.createdBy
        isPublic = this@toEntity.isPublic
    }
}

fun AvatarEntity.toDomainModel(): Avatar {
    return Avatar(
        id = avatarId,
        name = name,
        description = description,
        thumbnailUrl = thumbnailUrl,
        fullImageUrl = fullImageUrl,
        videoPreviewUrl = videoPreviewUrl,
        style = AvatarStyle.valueOf(style),
        generationPlatform = GenerationPlatform.valueOf(generationPlatform),
        generationParams = json.decodeFromString(generationParams),
        tags = json.decodeFromString(tags),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        createdBy = createdBy,
        isPublic = isPublic
    )
}


// Fixed User conversion functions
fun User.toEntity(): UserEntity {
    return UserEntity().apply {
        userId = this@toEntity.id
        username = this@toEntity.username
        name = this@toEntity.name
        email = this@toEntity.email
        role = this@toEntity.role
        joinDate = this@toEntity.joinDate
        avatarUrl = this@toEntity.avatarUrl
        subscription = this@toEntity.subscription.name
        projectsCreated = this@toEntity.projectsCreated
        videosPublished = this@toEntity.videosPublished
        totalViews = this@toEntity.totalViews
        storageUsed = this@toEntity.storageUsed
        displayName = this@toEntity.displayName
        bio = this@toEntity.bio
        isVerified = this@toEntity.isVerified
        followerCount = this@toEntity.followerCount
        followingCount = this@toEntity.followingCount
        postCount = this@toEntity.postCount
        enableNotifications = this@toEntity.enableNotifications
        privateAccount = this@toEntity.privateAccount
        showAiContent = this@toEntity.showAiContent
        autoplayVideos = this@toEntity.autoplayVideos
        feedPreference = this@toEntity.feedPreference.name
        createdAt = getCurrentTimeMillis()
        updatedAt = getCurrentTimeMillis()
        followingIds = "" // Will be populated when implementing follow feature
        followerIds = "" // Will be populated when implementing follow feature
    }
}

fun UserEntity.toDomainModel(): User {
    return User(
        id = userId,
        username = username,
        name = name,
        email = email,
        role = role,
        joinDate = joinDate,
        avatarUrl = avatarUrl,
        subscription = try {
            SubscriptionType.valueOf(subscription)
        } catch (e: Exception) {
            SubscriptionType.PRO
        },
        projectsCreated = projectsCreated,
        videosPublished = videosPublished,
        totalViews = totalViews,
        storageUsed = storageUsed,
        displayName = displayName,
        bio = bio,
        isVerified = isVerified,
        followerCount = followerCount,
        followingCount = followingCount,
        postCount = postCount,
        enableNotifications = enableNotifications,
        privateAccount = privateAccount,
        showAiContent = showAiContent,
        autoplayVideos = autoplayVideos,
        feedPreference = try {
            FeedType.valueOf(feedPreference)
        } catch (e: Exception) {
            FeedType.FOR_YOU
        }
    )
}

// Publishing Project conversions
fun PublishingProject.toEntity(): PublishingProjectEntity {
    return PublishingProjectEntity().apply {
        projectId = this@toEntity.id
        title = this@toEntity.title
        description = this@toEntity.description
        contentIds = this@toEntity.contentIds.joinToString("|")
        platforms = json.encodeToString(this@toEntity.platforms)
        exportSettings = json.encodeToString(this@toEntity.exportSettings)
        metadata = json.encodeToString(this@toEntity.metadata)
        status = this@toEntity.status.ordinal
        createdAt = this@toEntity.createdAt
        updatedAt = this@toEntity.updatedAt
        publishedAt = this@toEntity.publishedAt
    }
}

fun PublishingProjectEntity.toDomainModel(): PublishingProject {
    return PublishingProject(
        id = projectId,
        title = title,
        description = description,
        contentIds = contentIds.split("|").filter { it.isNotEmpty() },
        platforms = json.decodeFromString(platforms),
        exportSettings = json.decodeFromString(exportSettings),
        metadata = json.decodeFromString(metadata),
        status = PublishingStatus.values()[status],
        createdAt = createdAt,
        updatedAt = updatedAt,
        publishedAt = publishedAt
    )
}

// Deliverable conversions
fun Deliverable.toEntity(): DeliverableEntity {
    return DeliverableEntity().apply {
        deliverableId = this@toEntity.id
        projectIdString = this@toEntity.projectId
        title = this@toEntity.title
        description = this@toEntity.description ?: ""
        type = this@toEntity.type.name
        format = this@toEntity.format
        duration = this@toEntity.duration
        targetPlatforms = json.encodeToString(this@toEntity.targetPlatforms)
        status = this@toEntity.status.name
        dueDate = this@toEntity.dueDate.toEpochMilliseconds()
        deliveredDate = this@toEntity.deliveredDate?.toEpochMilliseconds()
        filePath = this@toEntity.filePath
        fileSize = this@toEntity.fileSize
        qualityMetrics = this@toEntity.qualityMetrics?.let { json.encodeToString(it) }
    }
}

fun DeliverableEntity.toDomainModel(): Deliverable {
    return Deliverable(
        id = deliverableId,
        projectId = projectIdString,
        title = title,
        description = description,
        type = DeliverableType.valueOf(type),
        format = format,
        duration = duration,
        targetPlatforms = json.decodeFromString(targetPlatforms),
        status = DeliverableStatus.valueOf(status),
        dueDate = Instant.fromEpochMilliseconds(dueDate),
        deliveredDate = deliveredDate?.let { Instant.fromEpochMilliseconds(it) },
        filePath = filePath,
        fileSize = fileSize,
        qualityMetrics = qualityMetrics?.let { json.decodeFromString(it) }
    )
}


// Fixed Content conversions
fun Content.toEntity(): ContentEntity {
    return ContentEntity().apply {
        contentId = this@toEntity.id
        projectIdString = this@toEntity.projectId ?: ""
        title = this@toEntity.title
        format = this@toEntity.type.name
        url = this@toEntity.url
        thumbnailUrl = this@toEntity.thumbnailUrl
        size = this@toEntity.size
        duration = this@toEntity.duration?.toFloat()
        uploadedBy = this@toEntity.uploadedBy
        isProcessed = this@toEntity.isProcessed
        isPublic = this@toEntity.isPublic
        description = this@toEntity.description
        version = this@toEntity.version ?: ""
        checksum = this@toEntity.checksum
        processingStatus = this@toEntity.processingStatus.name
        tags = json.encodeToString(this@toEntity.tags)
        createdAt = this@toEntity.createdAt.toEpochMilliseconds()
        updatedAt = this@toEntity.updatedAt.toEpochMilliseconds()

        // Store complex objects in metadata
        val metadataMap = mutableMapOf<String, String>()
        this@toEntity.dimensions?.let {
            metadataMap["dimensions"] = json.encodeToString(it)
        }
        this@toEntity.metadata?.let {
            metadataMap["contentMetadata"] = json.encodeToString(it)
        }
        metadata = json.encodeToString(metadataMap)
    }
}

fun ContentEntity.toDomainModel(): Content {
    val metadataMap = metadata?.let {
        json.decodeFromString<Map<String, String>>(it)
    } ?: emptyMap()

    return Content(
        id = contentId,
        projectId = projectIdString.takeIf { it.isNotEmpty() },
        title = title,
        type = ContentType.valueOf(format),
        format = format, // This field already exists in Content
        url = url,
        thumbnailUrl = thumbnailUrl,
        size = size,
        duration = duration?.toInt(),
        dimensions = metadataMap["dimensions"]?.let {
            json.decodeFromString(it)
        },
        metadata = metadataMap["contentMetadata"]?.let {
            json.decodeFromString(it)
        },
        tags = tags?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList(),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        uploadedBy = uploadedBy,
        isProcessed = isProcessed,
        isPublic = isPublic,
        description = description,
        version = version,
        checksum = checksum,
        processingStatus = ContentProcessingStatus.valueOf(processingStatus)
    )
}
