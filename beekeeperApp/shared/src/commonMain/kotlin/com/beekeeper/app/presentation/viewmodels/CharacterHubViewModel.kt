// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/CharacterProfileViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.domain.repository.CharacterRepository
import com.beekeeper.app.domain.repository.ProjectRepository
import com.beekeeper.app.domain.repository.CharacterRepositoryImpl
import com.beekeeper.app.domain.repository.ProjectRepositoryImpl
import com.beekeeper.app.domain.model.*
import com.beekeeper.app.domain.ai.interfaces.AvatarGenerationService
import com.beekeeper.app.domain.ai.interfaces.VoiceSynthesisService
import com.beekeeper.app.domain.ai.AvatarCreationRequest
import com.beekeeper.app.domain.ai.AvatarStylePreset
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

data class CharacterProfileUiState(
    val characters: List<CharacterProfile> = emptyList(),
    val selectedCharacterId: String? = null,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filterOptions: CharacterFilterOptions = CharacterFilterOptions()
)

data class CharacterFilterOptions(
    val showFavorites: Boolean = false,
    val archetypes: Set<String> = emptySet(),
    val relationshipTypes: Set<RelationshipType> = emptySet(),
    val ageRange: IntRange? = null
)

class CharacterProfileViewModel(
    private val characterRepository: CharacterRepository = CharacterRepositoryImpl(),
    private val projectRepository: ProjectRepository = ProjectRepositoryImpl(),
    private val avatarService: AvatarGenerationService? = null, // Injected when available
    private val voiceService: VoiceSynthesisService? = null // Injected when available
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterProfileUiState())
    val uiState: StateFlow<CharacterProfileUiState> = _uiState.asStateFlow()

    private val _characters = MutableStateFlow<List<CharacterProfile>>(emptyList())
    val characters: StateFlow<List<CharacterProfile>> = _characters.asStateFlow()

    private val _selectedCharacter = MutableStateFlow<CharacterProfile?>(null)
    val selectedCharacter: StateFlow<CharacterProfile?> = _selectedCharacter.asStateFlow()

    private val _project = MutableStateFlow<CreativeProject?>(null)
    val project: StateFlow<CreativeProject?> = _project.asStateFlow()

    private val _generatedAvatars = MutableStateFlow<List<Avatar>>(emptyList())
    val generatedAvatars: StateFlow<List<Avatar>> = _generatedAvatars.asStateFlow()

    private val _voiceProfiles = MutableStateFlow<List<VoiceProfile>>(emptyList())
    val voiceProfiles: StateFlow<List<VoiceProfile>> = _voiceProfiles.asStateFlow()

    private val _isGeneratingAvatar = MutableStateFlow(false)
    val isGeneratingAvatar: StateFlow<Boolean> = _isGeneratingAvatar.asStateFlow()

    var projectId: String? = null
        set(value) {
            field = value
            value?.let {
                loadProjectData(it)
                loadCharacters(it)
            }
        }

    /**
     * Initialize the ViewModel for a specific project
     */
    fun initializeForProject(projectId: String) {
        this.projectId = projectId
    }

    /**
     * Load all characters for the current project
     */
    fun loadCharacters(projectId: String) {

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val chars = characterRepository.getCharacters(projectId)
                _characters.value = chars
                _uiState.value = _uiState.value.copy(
                    characters = chars,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Load a specific character by ID
     */
    fun loadCharacter(characterId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val character = characterRepository.getCharacter(characterId)
                _selectedCharacter.value = character

                // Load related data
                character?.let {
                    if (it.assignedAvatarId != null) {
                        // Load avatar data if needed
                    }
                    if (it.voiceProfile != null) {
                        // Load voice profiles if needed
                    }
                }

                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Select a character for viewing/editing
     */
    fun selectCharacter(character: CharacterProfile) {
        _selectedCharacter.value = character
        _uiState.value = _uiState.value.copy(selectedCharacterId = character.id)
    }

    /**
     * Create a new character
     */
    suspend fun createCharacter(character: CharacterProfile): CharacterProfile {
        return try {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Save to repository - returns the character ID
            val createdCharacterId = characterRepository.createCharacter(character)

            // Create the character with the returned ID
            val createdCharacter = character.copy(id = createdCharacterId)

            // Update local state
            _characters.value = _characters.value + createdCharacter
            _selectedCharacter.value = createdCharacter

            _uiState.value = _uiState.value.copy(isLoading = false, error = null)

            createdCharacter
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message,
                isLoading = false
            )
            throw e
        }
    }

    /**
     * Update an existing character
     */
    fun updateCharacter(character: CharacterProfile) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Update in repository
                characterRepository.updateCharacter(character)

                // Update local state
                _characters.value = _characters.value.map {
                    if (it.id == character.id) character else it
                }

                if (_selectedCharacter.value?.id == character.id) {
                    _selectedCharacter.value = character
                }

                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Delete a character
     */
    fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Delete from repository
                characterRepository.deleteCharacter(characterId)

                // Update local state
                _characters.value = _characters.value.filter { it.id != characterId }

                if (_selectedCharacter.value?.id == characterId) {
                    _selectedCharacter.value = null
                }

                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Generate avatars for a character based on their traits
     */
    fun generateAvatarsForCharacter(characterId: String) {
        if (avatarService == null) {
            // Mock implementation for now
            generateMockAvatars(characterId)
            return
        }

        viewModelScope.launch {
            _isGeneratingAvatar.value = true
            try {
                val character = _characters.value.find { it.id == characterId } ?: return@launch

                // Create avatar generation request using domain model
                val avatarRequest = AvatarGenerationRequest(
                    prompt = buildAvatarPrompt(character),
                    style = getAvatarStyleFromMetadata(character.metadata["avatarStyle"]),
                    gender = character.gender,
                    age = getAgeRangeFromAge(character.age),
                    ethnicity = character.metadata["ethnicity"],
                    platform = GenerationPlatform.HEYGEN,
                    parameters = mapOf(
                        "characterName" to character.name,
                        "physicalAttributes" to character.physicalAttributes
                    )
                )

                // Generate multiple avatar variations using the service
                val avatars = mutableListOf<Avatar>()
                repeat(4) { index ->
                    val result = avatarService.createAvatar(
                        AvatarCreationRequest(
                            name = "${character.name} - Variation ${index + 1}",
                            style = mapToAvatarStylePreset(avatarRequest.style)
                        )
                    )

                    result.onSuccess { creationResult ->
                        val avatar = Avatar(
                            id = creationResult.avatarId,
                            name = "${character.name} - Variation ${index + 1}",
                            description = "AI generated avatar for ${character.name}",
                            thumbnailUrl = creationResult.previewUrl,
                            fullImageUrl = creationResult.previewUrl,
                            style = avatarRequest.style,
                            generationPlatform = avatarRequest.platform,
                            generationParams = avatarRequest.parameters,
                            tags = listOf(character.archetype, character.role.toString()),
                            createdAt = Clock.System.now(),
                            updatedAt = Clock.System.now(),
                            createdBy = "system",
                            isPublic = false
                        )
                        avatars.add(avatar)
                    }
                }

                _generatedAvatars.value = avatars

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _isGeneratingAvatar.value = false
            }
        }
    }

    /**
     * Generate voice profiles for a character
     */
    fun generateVoiceProfiles(characterId: String) {
        if (voiceService == null) {
            // Mock implementation for now
            generateMockVoiceProfiles(characterId)
            return
        }

        viewModelScope.launch {
            try {
                val character = _characters.value.find { it.id == characterId } ?: return@launch

                // Get available voices from the voice service
                val availableVoicesResult = voiceService.listVoices()

                availableVoicesResult.onSuccess { availableVoices ->
                    // Filter voices based on character attributes
                    val matchingVoices = availableVoices.filter { voice ->
                        matchesCharacterVoice(character, voice)
                    }.take(3) // Get top 3 matches

                    _voiceProfiles.value = matchingVoices
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Assign an avatar to a character
     */
    fun assignAvatarToCharacter(characterId: String, avatarId: String) {
        viewModelScope.launch {
            try {
                val character = _characters.value.find { it.id == characterId } ?: return@launch
                val updatedCharacter = character.copy(assignedAvatarId = avatarId)

                characterRepository.updateCharacter(updatedCharacter)

                // Update local state
                _characters.value = _characters.value.map {
                    if (it.id == characterId) updatedCharacter else it
                }

                if (_selectedCharacter.value?.id == characterId) {
                    _selectedCharacter.value = updatedCharacter
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Clear current selection
     */
    fun clearSelection() {
        _selectedCharacter.value = null
        _generatedAvatars.value = emptyList()
        _voiceProfiles.value = emptyList()
        _uiState.value = _uiState.value.copy(selectedCharacterId = null)
    }

    /**
     * Clear avatar selection
     */
    fun clearAvatarSelection() {
        _generatedAvatars.value = emptyList()
    }

    // Private helper functions

    private fun loadProjectData(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.getProject(projectId).fold(
                    onSuccess = { project ->
                        _project.value = project
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(error = exception.message)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun buildAvatarPrompt(character: CharacterProfile): String {
        return buildString {
            append("${character.name}, ")
            append("${character.age} years old, ")
            append("${character.archetype}, ")
            append("${character.physicalAttributes.hairColor} hair, ")
            append("${character.physicalAttributes.eyeColor} eyes, ")
            append("${character.physicalAttributes.build} build")
        }
    }

    private fun getAvatarStyleFromMetadata(style: String?): AvatarStyle {
        return when (style?.lowercase()) {
            "realistic" -> AvatarStyle.REALISTIC
            "cartoon" -> AvatarStyle.CARTOON
            "anime" -> AvatarStyle.ANIME
            "pixar" -> AvatarStyle.PIXAR
            "corporate" -> AvatarStyle.CORPORATE
            else -> AvatarStyle.REALISTIC
        }
    }

    private fun getAgeRangeFromAge(age: Int): AgeRange? {
        return when (age) {
            in 0..12 -> AgeRange.CHILD
            in 13..17 -> AgeRange.TEEN
            in 18..25 -> AgeRange.YOUNG
            in 26..45 -> AgeRange.ADULT
            in 46..65 -> AgeRange.MIDDLE_AGE
            in 66..Int.MAX_VALUE -> AgeRange.SENIOR
            else -> AgeRange.UNSPECIFIED
        }
    }

    private fun mapToAvatarStylePreset(style: AvatarStyle): AvatarStylePreset {
        return when (style) {
            AvatarStyle.REALISTIC -> AvatarStylePreset.REALISTIC
            AvatarStyle.CARTOON -> AvatarStylePreset.CARTOON
            AvatarStyle.ANIME -> AvatarStylePreset.ANIME
            AvatarStyle.CORPORATE -> AvatarStylePreset.PROFESSIONAL
            else -> AvatarStylePreset.REALISTIC
        }
    }

    private fun matchesCharacterVoice(character: CharacterProfile, voice: VoiceProfile): Boolean {
        val genderMatch = when (character.gender) {
            Gender.MALE -> voice.voiceModelType?.contains("male", ignoreCase = true) == true
            Gender.FEMALE -> voice.voiceModelType?.contains("female", ignoreCase = true) == true
            else -> true
        }

        val accentMatch = voice.accent == character.metadata["accent"] ||
                voice.accent == "neutral"

        return genderMatch && accentMatch
    }

    // Mock implementations for testing without services

    private fun generateMockAvatars(characterId: String) {
        viewModelScope.launch {
            _isGeneratingAvatar.value = true
            delay(2000) // Simulate generation time

            val character = _characters.value.find { it.id == characterId }
            val mockAvatars = (1..4).map { index ->
                Avatar(
                    id = "avatar_${characterId}_$index",
                    name = "${character?.name ?: "Character"} - Style $index",
                    description = "AI generated avatar variation $index",
                    thumbnailUrl = "https://placeholder.com/avatar_$index.jpg",
                    fullImageUrl = "https://placeholder.com/avatar_${index}_full.jpg",
                    style = AvatarStyle.values()[index % AvatarStyle.values().size],
                    generationPlatform = GenerationPlatform.HEYGEN,
                    generationParams = emptyMap(),
                    tags = listOf("generated", "character"),
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                    createdBy = "system",
                    isPublic = false
                )
            }

            _generatedAvatars.value = mockAvatars
            _isGeneratingAvatar.value = false
        }
    }

    private fun generateMockVoiceProfiles(characterId: String) {
        viewModelScope.launch {
            delay(1000) // Simulate API call

            val mockVoices = listOf(
                VoiceProfile(
                    voiceId = "voice_1",
                    voiceModelType = "elevenlabs_female_1",
                    pitch = 1.0f,
                    speed = 1.0f,
                    volume = 1.0f,
                    accent = "neutral",
                    emotionalTone = "warm",
                    speakingStyle = "conversational"
                ),
                VoiceProfile(
                    voiceId = "voice_2",
                    voiceModelType = "azure_female_2",
                    pitch = 0.9f,
                    speed = 1.1f,
                    volume = 1.0f,
                    accent = "british",
                    emotionalTone = "professional",
                    speakingStyle = "narrative"
                ),
                VoiceProfile(
                    voiceId = "voice_3",
                    voiceModelType = "heygen_female_3",
                    pitch = 1.1f,
                    speed = 0.95f,
                    volume = 1.0f,
                    accent = "american",
                    emotionalTone = "friendly",
                    speakingStyle = "dramatic"
                )
            )

            _voiceProfiles.value = mockVoices
        }
    }
}