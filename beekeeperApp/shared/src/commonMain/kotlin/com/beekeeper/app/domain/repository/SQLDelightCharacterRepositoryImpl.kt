// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/SQLDelightCharacterRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.data.api.ApiConfig
import com.beekeeper.app.data.api.ApiService
import com.beekeeper.app.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * SQLDelight-backed implementation of CharacterRepository with API integration
 * - When online: Fetches from API and caches to SQLDelight
 * - When offline: Reads from SQLDelight database cache
 * Uses in-memory cache for performance
 */
class SQLDelightCharacterRepositoryImpl(
    private val factoryRepository: SQLDelightProjectFactoryRepository,
    private val apiService: ApiService = ApiService()
) : CharacterRepository {

    // Cache for performance
    private val charactersCache = mutableMapOf<String, MutableList<CharacterProfile>>()
    private val favoritesCache = mutableSetOf<String>()

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
            println("👥 [CharacterRepository] Refreshing cache from ${basicInfo.size} factories")
            charactersCache.clear()

            // Load each factory individually to avoid large row issues
            basicInfo.forEach { info ->
                val projectId = info["project_id"] as? String ?: return@forEach
                try {
                    val factory = factoryRepository.getFactoryByProjectId(projectId)
                    if (factory != null) {
                        charactersCache[factory.projectId] = factory.characters.toMutableList()
                        println("   ✅ Loaded ${factory.characters.size} characters for ${factory.projectId}")
                    }
                } catch (e: Exception) {
                    println("   ⚠️ Skipping $projectId - factory too large: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("❌ Error refreshing character cache: ${e.message}")
        }
    }

    override suspend fun getCharacters(projectId: String): List<CharacterProfile> {
        println("SQLDelightCharacterRepository.getCharacters called for project: '$projectId'")
        println("Offline mode: ${ApiConfig.offlineMode}")

        // Try API first (unless in offline mode)
        if (!ApiConfig.offlineMode) {
            apiService.getCharacters(projectId).fold(
                onSuccess = { response ->
                    if (response != null) {
                        println("✅ API SUCCESS: Loaded ${response.characters.size} characters from API")

                        // Cache to SQLDelight database
                        try {
                            val factory = factoryRepository.getFactoryByProjectId(projectId)
                            if (factory != null) {
                                val updatedFactory = factory.copy(characters = response.characters)
                                val factoryType = when {
                                    factory.metadata.isSample -> "sample"
                                    factory.metadata.isTemplate -> "template"
                                    else -> "user"
                                }
                                factoryRepository.saveFactory(updatedFactory, factoryType)
                                println("✅ Cached ${response.characters.size} characters to SQLDelight")
                            }
                        } catch (e: Exception) {
                            println("⚠️ Failed to cache characters to SQLDelight: ${e.message}")
                        }

                        // Update in-memory cache
                        charactersCache[projectId] = response.characters.toMutableList()
                        return response.characters
                    }
                },
                onFailure = { error ->
                    println("⚠️ API call failed, falling back to cached data: ${error.message}")
                    // Fall through to cached data
                }
            )
        }

        // Try in-memory cache first
        val cached = charactersCache[projectId]
        if (cached != null) {
            println("✅ Returning ${cached.size} characters from memory cache")
            return cached
        }

        // Load from SQLDelight database
        try {
            println("Loading characters from SQLDelight database...")
            val factory = factoryRepository.getFactoryByProjectId(projectId)
            val characters = factory?.characters ?: emptyList()
            println("✅ Loaded ${characters.size} characters from SQLDelight")
            charactersCache[projectId] = characters.toMutableList()
            return characters
        } catch (e: Exception) {
            println("❌ Error loading characters for project $projectId: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun getCharacter(characterId: String): CharacterProfile? {
        // Search through all cached characters first
        charactersCache.values.forEach { projectCharacters ->
            projectCharacters.find { it.id == characterId }?.let {
                return it
            }
        }

        // If not in cache, search all factories
        try {
            val factories = factoryRepository.getAllFactories()
            factories.forEach { factory ->
                factory.characters.find { it.id == characterId }?.let { character ->
                    // Update cache
                    charactersCache.getOrPut(factory.projectId) { mutableListOf() }
                        .add(character)
                    return character
                }
            }
        } catch (e: Exception) {
            println("❌ Error finding character $characterId: ${e.message}")
        }

        return null
    }

    override suspend fun createCharacter(character: CharacterProfile): String {
        try {
            // Load existing factory
            val factory = factoryRepository.getFactoryByProjectId(character.projectId)

            if (factory != null) {
                // Add character to factory
                val updatedCharacters = factory.characters + character
                val updatedFactory = factory.copy(characters = updatedCharacters)

                // Determine factory type
                val factoryType = when {
                    factory.metadata.isSample -> "sample"
                    factory.metadata.isTemplate -> "template"
                    else -> "user"
                }

                // Save updated factory
                factoryRepository.saveFactory(updatedFactory, factoryType)

                // Update cache
                charactersCache.getOrPut(character.projectId) { mutableListOf() }
                    .add(character)
            } else {
                println("⚠️ No factory found for project ${character.projectId}")
                // Still add to cache for in-memory usage
                charactersCache.getOrPut(character.projectId) { mutableListOf() }
                    .add(character)
            }

            return character.id
        } catch (e: Exception) {
            println("❌ Error creating character: ${e.message}")
            return character.id
        }
    }

    override suspend fun updateCharacter(character: CharacterProfile) {
        try {
            // Load existing factory
            val factory = factoryRepository.getFactoryByProjectId(character.projectId)

            if (factory != null) {
                // Update character in factory
                val updatedCharacters = factory.characters.map {
                    if (it.id == character.id) character else it
                }
                val updatedFactory = factory.copy(characters = updatedCharacters)

                // Determine factory type
                val factoryType = when {
                    factory.metadata.isSample -> "sample"
                    factory.metadata.isTemplate -> "template"
                    else -> "user"
                }

                // Save updated factory
                factoryRepository.saveFactory(updatedFactory, factoryType)

                // Update cache
                charactersCache[character.projectId]?.let { projectCharacters ->
                    val index = projectCharacters.indexOfFirst { it.id == character.id }
                    if (index != -1) {
                        projectCharacters[index] = character
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error updating character: ${e.message}")
        }
    }

    override suspend fun deleteCharacter(characterId: String) {
        try {
            // Find which project this character belongs to
            val factories = factoryRepository.getAllFactories()

            factories.forEach { factory ->
                val characterToDelete = factory.characters.find { it.id == characterId }
                if (characterToDelete != null) {
                    // Remove character from factory
                    val updatedCharacters = factory.characters.filter { it.id != characterId }
                    val updatedFactory = factory.copy(characters = updatedCharacters)

                    // Determine factory type
                    val factoryType = when {
                        factory.metadata.isSample -> "sample"
                        factory.metadata.isTemplate -> "template"
                        else -> "user"
                    }

                    // Save updated factory
                    factoryRepository.saveFactory(updatedFactory, factoryType)

                    // Update cache
                    charactersCache[factory.projectId]?.removeAll { it.id == characterId }

                    return
                }
            }
        } catch (e: Exception) {
            println("❌ Error deleting character: ${e.message}")
        }
    }

    override suspend fun extractCharactersFromScript(
        scriptContent: String,
        projectId: String
    ): List<CharacterProfile> {
        val currentTime = Clock.System.now()

        // Mock AI extraction - in real implementation, call your AI service
        val extractedCharacter = CharacterProfile(
            id = "extracted_${currentTime.toEpochMilliseconds()}_1",
            projectId = projectId,
            name = "Extracted Character",
            role = CharacterRole.SUPPORTING,
            archetype = "The Helper",
            description = "Character extracted from script using AI analysis.",
            personality = PersonalityProfile(
                archetype = "The Helper",
                traits = listOf(PersonalityTrait("Supportive", 0.8f)),
                backstory = "Extracted from script",
                motivations = listOf("To be determined"),
                fears = listOf("To be determined")
            ),
            age = 30,
            height = "5'9\"",
            build = "Average",
            hairColor = "Brown",
            eyeColor = "Blue",
            distinctiveFeatures = emptyList(),
            physicalAttributes = PhysicalAttributes(
                height = "5'9\"",
                build = "Average",
                hairColor = "Brown",
                eyeColor = "Blue",
                distinctiveFeatures = emptyList()
            ),
            createdAt = currentTime,
            updatedAt = currentTime,
            gender = Gender.UNSPECIFIED
        )

        val extractedCharacters = listOf(extractedCharacter)

        // Add to database
        extractedCharacters.forEach { createCharacter(it) }

        return extractedCharacters
    }
}
