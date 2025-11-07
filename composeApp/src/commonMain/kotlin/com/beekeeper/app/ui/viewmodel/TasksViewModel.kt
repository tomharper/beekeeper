package com.beekeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeper.app.data.repository.TaskRepository
import com.beekeeper.app.domain.model.Task
import com.beekeeper.app.domain.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val selectedFilter: TaskStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TasksViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            taskRepository.getTasks().fold(
                onSuccess = { tasks ->
                    _uiState.value = _uiState.value.copy(
                        tasks = tasks,
                        filteredTasks = filterTasks(tasks, _uiState.value.selectedFilter),
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load tasks"
                    )
                }
            )
        }
    }

    fun filterByStatus(status: TaskStatus?) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = status,
            filteredTasks = filterTasks(_uiState.value.tasks, status)
        )
    }

    private fun filterTasks(tasks: List<Task>, status: TaskStatus?): List<Task> {
        return if (status == null) {
            tasks
        } else {
            tasks.filter { it.status == status }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.completeTask(taskId).fold(
                onSuccess = {
                    loadTasks()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to complete task"
                    )
                }
            )
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId).fold(
                onSuccess = {
                    loadTasks()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete task"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
