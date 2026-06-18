package com.beekeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.repository.ApiaryRepository
import com.beekeeper.app.domain.model.Apiary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ApiaryListUiState(
    val apiaries: List<Apiary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ApiaryListViewModel(
    private val apiaryRepository: ApiaryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiaryListUiState())
    val uiState: StateFlow<ApiaryListUiState> = _uiState.asStateFlow()

    init {
        loadApiaries()
    }

    fun loadApiaries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            apiaryRepository.getApiaries().fold(
                onSuccess = { apiaries ->
                    _uiState.value = _uiState.value.copy(apiaries = apiaries, isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load apiaries",
                    )
                },
            )
        }
    }
}
