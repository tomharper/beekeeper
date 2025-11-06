// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/domain/repository/CharacterRepository.kt
package com.beekeeper.app.domain.repository

import com.beekeeper.app.domain.model.CharacterProfile

/**
 * Repository interface for character operations
 */
interface CharacterRepository {
    /**
     * Get all characters for a specific project
     */
    suspend fun getCharacters(projectId: String): List<CharacterProfile>
    
    /**
     * Get a specific character by ID
     */
    suspend fun getCharacter(characterId: String): CharacterProfile?
    
    /**
     * Create a new character
     * @return The ID of the created character
     */
    suspend fun createCharacter(character: CharacterProfile): String
    
    /**
     * Update an existing character
     */
    suspend fun updateCharacter(character: CharacterProfile)
    
    /**
     * Delete a character by ID
     */
    suspend fun deleteCharacter(characterId: String)
    
    /**
     * Extract characters from a script using AI
     * @return List of extracted characters
     */
    suspend fun extractCharactersFromScript(scriptContent: String, projectId: String): List<CharacterProfile>
}

