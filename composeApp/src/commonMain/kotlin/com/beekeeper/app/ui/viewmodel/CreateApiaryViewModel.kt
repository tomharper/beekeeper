package com.beekeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.api.CreateApiaryRequest
import com.beekeeper.app.location.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateApiaryUiState(
    val name: String = "",
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val capturingLocation: Boolean = false,
    val creating: Boolean = false,
    val error: String? = null,
    val created: Boolean = false,
)

class CreateApiaryViewModel(
    private val apiClient: ApiClient,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateApiaryUiState())
    val state: StateFlow<CreateApiaryUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onLocationChange(value: String) = _state.update { it.copy(location = value, error = null) }

    /** Capture the device's current coordinates (call after permission is granted). */
    fun captureLocation() {
        viewModelScope.launch {
            _state.update { it.copy(capturingLocation = true, error = null) }
            val coords = locationProvider.current()
            _state.update { s ->
                if (coords == null) {
                    s.copy(
                        capturingLocation = false,
                        error = "Couldn't get your location — is location turned on and permission granted?",
                    )
                } else {
                    s.copy(
                        capturingLocation = false,
                        latitude = coords.latitude,
                        longitude = coords.longitude,
                        // Fill the label with coords if the user hasn't typed one.
                        location = if (s.location.isBlank())
                            "${format5(coords.latitude)}, ${format5(coords.longitude)}"
                        else s.location,
                    )
                }
            }
        }
    }

    fun create() {
        val s = _state.value
        if (s.name.isBlank() || s.location.isBlank()) {
            _state.update { it.copy(error = "Name and location are required.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(creating = true, error = null) }
            try {
                apiClient.createApiary(
                    CreateApiaryRequest(
                        name = s.name.trim(),
                        location = s.location.trim(),
                        latitude = s.latitude,
                        longitude = s.longitude,
                    )
                )
                _state.update { it.copy(creating = false, created = true) }
            } catch (e: Exception) {
                _state.update { it.copy(creating = false, error = e.message ?: "Failed to create apiary.") }
            }
        }
    }
}

/** 5-decimal coordinate formatting without java.lang.String.format (not in commonMain). */
private fun format5(value: Double): String {
    val scaled = kotlin.math.round(value * 100_000.0) / 100_000.0
    return scaled.toString()
}
