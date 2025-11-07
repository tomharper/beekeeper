package com.beekeeper.app.data.repository

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.domain.model.ChatMessage

class AIAdvisorRepository(
    private val apiClient: ApiClient
) {
    suspend fun sendMessage(message: String): Result<ChatMessage> {
        return try {
            val response = apiClient.sendChatMessage(message)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
