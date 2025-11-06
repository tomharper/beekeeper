// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/data/repository/CharacterRepositoryImpl.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.data.api.ApiConfig
import com.beekeeper.app.data.api.ApiService
import com.beekeeper.app.domain.model.*
// Removed local factory imports - now uses API only
import com.beekeeper.app.utils.getCurrentTimeMillis
import kotlinx.datetime.Clock

/**
 * Implementation of CharacterRepository with backend API integration
 * Falls back to local factories in offline mode
 */
class CharacterRepositoryImpl(
    private val apiService: ApiService = ApiService()
) : CharacterRepository {

    // In-memory cache for characters
    private val charactersCache = mutableMapOf<String, MutableList<CharacterProfile>>()
    private val favoritesCache = mutableSetOf<String>()

    override suspend fun getCharacters(projectId: String): List<CharacterProfile> {
        println("CharacterRepository.getCharacters called with projectId: '$projectId'")
        println("Offline mode: ${ApiConfig.offlineMode}")

        // Try API first (unless in offline mode)
        if (!ApiConfig.offlineMode) {
            apiService.getCharacters(projectId).fold(
                onSuccess = { response ->
                    if (response != null) {
                        println("API SUCCESS: Loaded ${response.characters.size} characters from API")
                        // Update cache with API data
                        charactersCache[projectId] = response.characters.toMutableList()
                        return response.characters
                    }
                },
                onFailure = { error ->
                    println("API call failed, falling back to local data: ${error.message}")
                    error.printStackTrace()
                    // Fall through to local data
                }
            )
        } else {
            println("Offline mode enabled, using factories")
        }

        // Return cached characters if available, otherwise empty list
        val characters = charactersCache[projectId] ?: emptyList()
        println("Returning ${characters.size} cached characters")
        return characters
    }

    override suspend fun getCharacter(characterId: String): CharacterProfile? {
        // Search through all cached characters
        charactersCache.values.forEach { projectCharacters ->
            projectCharacters.find { it.id == characterId }?.let {
                return it
            }
        }
        return null
    }

    override suspend fun createCharacter(character: CharacterProfile): String {
        val projectCharacters = charactersCache.getOrPut(character.projectId) { mutableListOf() }
        projectCharacters.add(character)
        return character.id
    }

    override suspend fun updateCharacter(character: CharacterProfile) {
        charactersCache[character.projectId]?.let { projectCharacters ->
            val index = projectCharacters.indexOfFirst { it.id == character.id }
            if (index != -1) {
                projectCharacters[index] = character
            }
        }
    }

    override suspend fun deleteCharacter(characterId: String) {
        charactersCache.forEach { (_, projectCharacters) ->
            val toRemove = projectCharacters.filter { it.id == characterId }
            projectCharacters.removeAll(toRemove)
        }
    }

    override suspend fun extractCharactersFromScript(
        scriptContent: String,
        projectId: String
    ): List<CharacterProfile> {
        val currentTime = Clock.System.now()

        // Mock AI extraction - in real implementation, call your AI service
        val extractedCharacters = listOf(
            CharacterProfile(
                id = "extracted_${getCurrentTimeMillis()}_1",
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
        )

        // Add to cache
        charactersCache.getOrPut(projectId) { mutableListOf() }.addAll(extractedCharacters)
        return extractedCharacters
    }

}