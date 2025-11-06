// File: shared/src/commonMain/kotlin/com/cinefiller/fillerapp/presentation/viewmodels/AuthViewModel.kt
package com.beekeeper.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.api.ApiService
import com.beekeeper.app.domain.auth.AuthManager
import com.beekeeper.app.domain.auth.AuthState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication
 * Manages login, logout, and token verification
 */
class AuthViewModel(
    private val authManager: AuthManager,
    private val apiService: ApiService
) : ViewModel() {

    val authState: StateFlow<AuthState> = authManager.authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Initialize auth manager and verify stored token
        viewModelScope.launch {
            authManager.initialize()

            // If we have a token, verify it
            val token = authManager.getCurrentToken()
            if (token != null) {
                apiService.setAuthToken(token)
                val isValid = apiService.verifyToken().getOrDefault(false)
                if (!isValid) {
                    // Token is invalid, clear it
                    authManager.clearToken()
                    apiService.setAuthToken(null)
                }
            }
        }
    }

    /**
     * Login with username and password
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            apiService.login(username, password).fold(
                onSuccess = { response ->
                    // Save token to auth manager
                    authManager.setToken(response.access_token)
                    // Token is already set in ApiService by the login method

                    // Clear ETag cache to force fresh data fetch after login
                    com.beekeeper.app.data.api.ETagCache.clear()
                    println("🔄 [AuthViewModel] Cleared ETag cache for fresh data fetch")

                    _isLoading.value = false

                    // Sync data from server after successful login
                    syncDataFromServer()
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Invalid username or password"
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * Sync data from server after login
     * Explicitly calls repository sync to fetch fresh data
     */
    private fun syncDataFromServer() {
        viewModelScope.launch {
            _isSyncing.value = true
            println("🔄 [AuthViewModel] Starting server sync...")
            println("🔄 [AuthViewModel] Auth token is set: ${apiService.getAuthToken() != null}")

            try {
                // Get the project repository
                val projectRepo = com.beekeeper.app.domain.repository.RepositoryManager.projectRepository
                println("🔄 [AuthViewModel] Got project repository: ${projectRepo::class.simpleName}")

                // Cast to implementation to access syncFromServer method
                if (projectRepo is com.beekeeper.app.domain.repository.ProjectRepositoryImpl) {
                    projectRepo.syncFromServer().fold(
                        onSuccess = { count ->
                            println("✅ [AuthViewModel] Server sync complete: $count projects synced")
                            _isSyncing.value = false
                        },
                        onFailure = { error ->
                            println("❌ [AuthViewModel] Sync failed: ${error.message}")
                            _isSyncing.value = false
                        }
                    )
                } else {
                    println("⚠️ [AuthViewModel] Repository doesn't support sync")
                    _isSyncing.value = false
                }
            } catch (e: Exception) {
                println("❌ [AuthViewModel] Error during server sync: ${e.message}")
                e.printStackTrace()
                _isSyncing.value = false
            }
        }
    }

    /**
     * Logout and clear authentication
     */
    fun logout() {
        viewModelScope.launch {
            authManager.clearToken()
            apiService.setAuthToken(null)
        }
    }

    /**
     * Verify current token is valid
     */
    suspend fun verifyToken(): Boolean {
        val token = authManager.getCurrentToken() ?: return false
        apiService.setAuthToken(token)
        return apiService.verifyToken().getOrDefault(false)
    }
}
