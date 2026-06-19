package com.beekeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.repository.ApiaryRepository
import com.beekeeper.app.data.repository.HiveRepository
import com.beekeeper.app.domain.model.Apiary
import com.beekeeper.app.domain.model.Hive
import com.beekeeper.app.domain.model.Recommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HiveDetailsUiState(
    val hive: Hive? = null,
    val apiary: Apiary? = null,
    val recommendations: List<Recommendation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class HiveDetailsViewModel(
    private val hiveRepository: HiveRepository,
    private val apiaryRepository: ApiaryRepository,
    private val apiClient: ApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HiveDetailsUiState())
    val uiState: StateFlow<HiveDetailsUiState> = _uiState.asStateFlow()

    fun load(hiveId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val hiveResult = hiveRepository.getHive(hiveId)
            val hive = hiveResult.getOrNull()
            val apiary = hive?.let { apiaryRepository.getApiary(it.apiaryId).getOrNull() }
            val recommendations = try {
                apiClient.getRecommendations(hiveId)
            } catch (e: Exception) {
                emptyList()
            }
            _uiState.value = _uiState.value.copy(
                hive = hive,
                apiary = apiary,
                recommendations = recommendations,
                isLoading = false,
                error = hiveResult.exceptionOrNull()?.message,
            )
        }
    }
}
