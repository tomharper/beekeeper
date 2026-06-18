package com.beekeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.repository.ApiaryRepository
import com.beekeeper.app.data.repository.HiveRepository
import com.beekeeper.app.domain.model.Alert
import com.beekeeper.app.domain.model.Apiary
import com.beekeeper.app.domain.model.Hive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ApiaryDashboardUiState(
    val apiary: Apiary? = null,
    val hives: List<Hive> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ApiaryDashboardViewModel(
    private val apiaryRepository: ApiaryRepository,
    private val hiveRepository: HiveRepository,
    private val apiClient: ApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiaryDashboardUiState())
    val uiState: StateFlow<ApiaryDashboardUiState> = _uiState.asStateFlow()

    fun load(apiaryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val apiaryResult = apiaryRepository.getApiary(apiaryId)
            val hivesResult = hiveRepository.getHives(apiaryId)
            // Alerts are not vertical-scoped on the backend; best-effort, never blocks the screen.
            val alerts = try {
                apiClient.getActiveAlerts()
            } catch (e: Exception) {
                emptyList()
            }
            _uiState.value = _uiState.value.copy(
                apiary = apiaryResult.getOrNull(),
                hives = hivesResult.getOrDefault(emptyList()),
                alerts = alerts,
                isLoading = false,
                error = hivesResult.exceptionOrNull()?.message,
            )
        }
    }
}
