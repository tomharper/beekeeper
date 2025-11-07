package com.beekeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.repository.AIAdvisorRepository
import com.beekeeper.app.domain.model.Alert
import com.beekeeper.app.domain.model.ChatMessage
import com.beekeeper.app.domain.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class AIAdvisorUiState(
    val alerts: List<Alert> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val isLoadingAlerts: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AIAdvisorViewModel(
    private val repository: AIAdvisorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAdvisorUiState(
        messages = listOf(
            ChatMessage(
                id = "welcome",
                content = "Hello! I'm your AI beekeeping advisor. I can help you with questions about hive management, colony health, pest control, seasonal tasks, and more. What would you like to know?",
                role = MessageRole.ASSISTANT,
                timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
        )
    ))
    val uiState: StateFlow<AIAdvisorUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAlerts = true)

            repository.getAlerts().fold(
                onSuccess = { alerts ->
                    _uiState.value = _uiState.value.copy(
                        alerts = alerts,
                        isLoadingAlerts = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAlerts = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val userMessage = ChatMessage(
            id = "user-${System.currentTimeMillis()}",
            content = content,
            role = MessageRole.USER,
            timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )

        // Add user message immediately
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            repository.sendMessage(content).fold(
                onSuccess = { assistantMessage ->
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send message"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
