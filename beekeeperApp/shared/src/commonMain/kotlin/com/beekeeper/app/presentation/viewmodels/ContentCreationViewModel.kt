// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/ContentCreationViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import com.beekeeper.app.domain.model.*

// Simple AI Service Manager for current implementation
interface AIServiceManager {
    suspend fun generateContent(request: ContentGenerationRequest): Result<ContentGenerationResult>
    suspend fun generateScript(prompt: String, type: String): Result<String>
    suspend fun generateAvatar(characterDescription: String): Result<String>
    suspend fun generateVideo(script: String, avatarId: String): Result<String>
    fun getAvailableProviders(): List<String>
    fun switchProvider(provider: String): Boolean
    fun getCurrentProvider(): String
}

// ViewModel
class ContentCreationViewModel(
    private val aiServiceManager: AIServiceManager = DefaultAIServiceManager()
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(ContentCreationState())
    val state: StateFlow<ContentCreationState> = _state.asStateFlow()

    private val _availableProviders = MutableStateFlow<List<String>>(emptyList())
    val availableProviders: StateFlow<List<String>> = _availableProviders.asStateFlow()

    private val _currentProvider = MutableStateFlow("")
    val currentProvider: StateFlow<String> = _currentProvider.asStateFlow()

    init {
        loadAvailableProviders()
    }

    private fun loadAvailableProviders() {
        _availableProviders.value = aiServiceManager.getAvailableProviders()
    }

    fun switchAIProvider(provider: String) {
        viewModelScope.launch {
            try {
                if (aiServiceManager.switchProvider(provider)) {
                    updateState {
                        it.copy(errors = it.errors.filter { error ->
                            !error.contains("provider")
                        })
                    }
                } else {
                    addError("Failed to switch to provider: $provider")
                }
            } catch (e: Exception) {
                addError("Error switching provider: ${e.message}")
            }
        }
    }

    fun generateScript(prompt: String, contentType: String = "social_media") {
        viewModelScope.launch {
            updateState {
                it.copy(
                    currentStep = ContentCreationStep.SCRIPT_GENERATION,
                    isProcessing = true,
                    progress = 0.1f
                )
            }

            try {
                aiServiceManager.generateScript(prompt, contentType).fold(
                    onSuccess = { script ->
                        updateState {
                            it.copy(
                                script = script,
                                isProcessing = false,
                                progress = 0.2f
                            )
                        }
                    },
                    onFailure = { error ->
                        addError("Script generation failed: ${error.message}")
                        updateState { it.copy(isProcessing = false) }
                    }
                )
            } catch (e: Exception) {
                addError("Script generation error: ${e.message}")
                updateState { it.copy(isProcessing = false) }
            }
        }
    }

    fun generateCharacters(characterDescriptions: List<String>) {
        viewModelScope.launch {
            updateState {
                it.copy(
                    currentStep = ContentCreationStep.CHARACTER_CREATION,
                    isProcessing = true,
                    progress = 0.3f
                )
            }

            try {
                val characters = mutableListOf<CharacterData>()

                characterDescriptions.forEachIndexed { index, description ->
                    val avatarResult = aiServiceManager.generateAvatar(description)
                    avatarResult.fold(
                        onSuccess = { avatarUrl ->
                            characters.add(
                                CharacterData(
                                    id = "char_${Clock.System.now().toEpochMilliseconds()}_$index",
                                    name = "Character ${index + 1}",
                                    description = description,
                                    avatarUrl = avatarUrl,
                                    voiceId = "voice_$index"
                                )
                            )
                        },
                        onFailure = { error ->
                            addError("Failed to generate avatar for character ${index + 1}: ${error.message}")
                        }
                    )
                }

                updateState {
                    it.copy(
                        characters = characters,
                        isProcessing = false,
                        progress = 0.4f
                    )
                }
            } catch (e: Exception) {
                addError("Character generation error: ${e.message}")
                updateState { it.copy(isProcessing = false) }
            }
        }
    }

    fun generateStoryboard() {
        viewModelScope.launch {
            updateState {
                it.copy(
                    currentStep = ContentCreationStep.STORYBOARD_GENERATION,
                    isProcessing = true,
                    progress = 0.5f
                )
            }

            try {
                // Generate storyboard frames from script
                val storyboardFrames = generateStoryboardFromScript(_state.value.script)

                updateState {
                    it.copy(
                        storyboard = storyboardFrames,
                        isProcessing = false,
                        progress = 0.6f
                    )
                }
            } catch (e: Exception) {
                addError("Storyboard generation error: ${e.message}")
                updateState { it.copy(isProcessing = false) }
            }
        }
    }

    fun generateScenes() {
        viewModelScope.launch {
            updateState {
                it.copy(
                    currentStep = ContentCreationStep.SCENE_GENERATION,
                    isProcessing = true,
                    progress = 0.7f
                )
            }

            try {
                val scenes = mutableListOf<SceneData>()
                val currentState = _state.value

                currentState.storyboard.forEach { frame ->
                    val dialogue = extractDialogueForFrame(currentState.script, frame.sceneNumber)
                    val characterIds = currentState.characters.map { it.id }

                    aiServiceManager.generateVideo(dialogue, characterIds.firstOrNull() ?: "").fold(
                        onSuccess = { videoUrl ->
                            scenes.add(
                                SceneData(
                                    id = "scene_${Clock.System.now().toEpochMilliseconds()}",
                                    frameId = frame.id,
                                    characterIds = characterIds,
                                    dialogue = dialogue,
                                    visualDescription = frame.description,
                                    videoUrl = videoUrl,
                                    imageUrl = "placeholder_image_url",
                                    audioUrl = "placeholder_audio_url",
                                    text = dialogue
                                )
                            )
                        },
                        onFailure = { error ->
                            addError("Scene generation failed: ${error.message}")
                        }
                    )
                }

                updateState {
                    it.copy(
                        scenes = scenes,
                        isProcessing = false,
                        progress = 0.8f
                    )
                }
            } catch (e: Exception) {
                addError("Scene generation error: ${e.message}")
                updateState { it.copy(isProcessing = false) }
            }
        }
    }

    fun generateAudio() {
        viewModelScope.launch {
            updateState {
                it.copy(
                    currentStep = ContentCreationStep.AUDIO_GENERATION,
                    isProcessing = true,
                    progress = 0.85f
                )
            }

            try {
                val audioTracks = mutableListOf<AudioTrack>()

                // Generate dialogue tracks
                _state.value.scenes.forEach { scene ->
                    audioTracks.add(
                        AudioTrack(
                            id = "audio_${Clock.System.now().toEpochMilliseconds()}",
                            type = "dialogue",
                            content = scene.dialogue,
                            audioUrl = scene.audioUrl,
                            duration = 5f // Default duration
                        )
                    )
                }

                // Add background music
                audioTracks.add(
                    AudioTrack(
                        id = "music_${Clock.System.now().toEpochMilliseconds()}",
                        type = "music",
                        content = "Background Music",
                        audioUrl = "placeholder_music_url",
                        duration = calculateTotalDuration()
                    )
                )

                updateState {
                    it.copy(
                        audioTracks = audioTracks,
                        isProcessing = false,
                        progress = 0.9f
                    )
                }
            } catch (e: Exception) {
                addError("Audio generation error: ${e.message}")
                updateState { it.copy(isProcessing = false) }
            }
        }
    }

    fun assembleVideo() {
        viewModelScope.launch {
            updateState {
                it.copy(
                    currentStep = ContentCreationStep.VIDEO_ASSEMBLY,
                    isProcessing = true,
                    progress = 0.95f
                )
            }

            try {
                val finalVideo = VideoData(
                    id = "video_${Clock.System.now().toEpochMilliseconds()}",
                    title = "Generated Content",
                    duration = calculateTotalDuration(),
                    videoUrl = "final_video_url",
                    thumbnailUrl = "thumbnail_url"
                )

                updateState {
                    it.copy(
                        finalVideo = finalVideo,
                        isProcessing = false,
                        progress = 1.0f
                    )
                }
            } catch (e: Exception) {
                addError("Video assembly error: ${e.message}")
                updateState { it.copy(isProcessing = false) }
            }
        }
    }

    fun exportVideo(format: String = "MP4", resolution: String = "1080p") {
        viewModelScope.launch {
            updateState {
                it.copy(
                    currentStep = ContentCreationStep.EXPORT,
                    isProcessing = true
                )
            }

            try {
                val exportedVideo = _state.value.finalVideo?.copy(
                    format = format,
                    resolution = resolution
                )

                updateState {
                    it.copy(
                        finalVideo = exportedVideo,
                        isProcessing = false
                    )
                }
            } catch (e: Exception) {
                addError("Video export error: ${e.message}")
                updateState { it.copy(isProcessing = false) }
            }
        }
    }

    fun resetCreation() {
        _state.value = ContentCreationState()
    }

    fun clearErrors() {
        updateState { it.copy(errors = emptyList()) }
    }

    fun updateState(update: (ContentCreationState) -> ContentCreationState) {
        _state.value = update(_state.value)
    }

    private fun addError(error: String) {
        updateState {
            it.copy(errors = it.errors + error)
        }
    }

    private fun generateStoryboardFromScript(script: String): List<StoryboardFrame> {
        val lines = script.split("\n").filter { it.isNotBlank() }
        return lines.mapIndexed { index, line ->
            val sceneId = "scene_${Clock.System.now().toEpochMilliseconds()}_$index"
            StoryboardFrame(
                id = "frame_${Clock.System.now().toEpochMilliseconds()}_$index",
                sceneNumber = index + 1,
                frameNumber = index + 1,
                description = line.take(100),
                duration = 5f + (index * 2f),
                sceneData = SceneData(
                    id = sceneId,
                    frameId = "frame_${index + 1}",
                    characterIds = emptyList(),
                    dialogue = line,
                    visualDescription = "Visual for scene ${index + 1}",
                    imageUrl = "placeholder_image_${index}.jpg",
                    audioUrl = "placeholder_audio_${index}.mp3",
                    videoUrl = "placeholder_video_${index}.mp4",
                    text = line
                )
            )
        }
    }

    private fun extractDialogueForFrame(script: String, sceneNumber: Int): String {
        val lines = script.split("\n").filter { it.isNotBlank() }
        return if (sceneNumber <= lines.size) {
            lines[sceneNumber - 1]
        } else {
            "Generated dialogue for scene $sceneNumber"
        }
    }

    private fun calculateTotalDuration(): Float {
        return _state.value.storyboard.sumOf { it.duration.toDouble() }.toFloat()
    }
}

// Default AI Service Manager Implementation
class DefaultAIServiceManager : AIServiceManager {
    private var currentProvider = "mock"
    private val availableProviders = listOf("mock", "claude", "heygen")

    override suspend fun generateContent(request: ContentGenerationRequest): Result<ContentGenerationResult> {
        return try {
            Result.success(
                ContentGenerationResult(
                    content = "Generated content for: ${request.prompt}",
                    metadata = mapOf("provider" to currentProvider),
                    success = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateScript(prompt: String, type: String): Result<String> {
        return try {
            Result.success(
                """
                Scene 1: Introduction
                ${prompt.take(50)}...
                
                Scene 2: Development
                The story continues with engaging content that captures the audience's attention.
                
                Scene 3: Conclusion
                A satisfying ending that delivers on the promise of the introduction.
                """.trimIndent()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateAvatar(characterDescription: String): Result<String> {
        return try {
            Result.success("https://placeholder.avatar.url/${characterDescription.hashCode()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateVideo(script: String, avatarId: String): Result<String> {
        return try {
            Result.success("https://placeholder.video.url/${script.hashCode()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAvailableProviders(): List<String> = availableProviders

    override fun switchProvider(provider: String): Boolean {
        return if (provider in availableProviders) {
            currentProvider = provider
            true
        } else {
            false
        }
    }

    override fun getCurrentProvider(): String = currentProvider
}