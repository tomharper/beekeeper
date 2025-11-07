package com.beekeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.repository.InspectionRepository
import com.beekeeper.app.domain.model.Inspection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InspectionsUiState(
    val inspections: List<Inspection> = emptyList(),
    val selectedHiveId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class InspectionsViewModel(
    private val inspectionRepository: InspectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InspectionsUiState())
    val uiState: StateFlow<InspectionsUiState> = _uiState.asStateFlow()

    init {
        loadInspections()
    }

    fun loadInspections(hiveId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedHiveId = hiveId
            )

            inspectionRepository.getInspections(hiveId = hiveId).fold(
                onSuccess = { inspections ->
                    _uiState.value = _uiState.value.copy(
                        inspections = inspections,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load inspections"
                    )
                }
            )
        }
    }

    fun loadRecentInspections(limit: Int = 10) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            inspectionRepository.getRecentInspections(limit).fold(
                onSuccess = { inspections ->
                    _uiState.value = _uiState.value.copy(
                        inspections = inspections,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load recent inspections"
                    )
                }
            )
        }
    }

    fun deleteInspection(inspectionId: String) {
        viewModelScope.launch {
            inspectionRepository.deleteInspection(inspectionId).fold(
                onSuccess = {
                    loadInspections(_uiState.value.selectedHiveId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete inspection"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
