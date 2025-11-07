package com.beekeeper.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: LocalDateTime
)

enum class MessageRole {
    USER,
    ASSISTANT
}
