package com.example.supplyline_mro_suite.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supplyline_mro_suite.data.repository.ToolRepository
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolCheckout
import com.example.supplyline_mro_suite.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToolDetailViewModel @Inject constructor(
    private val toolRepository: ToolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolDetailUiState())
    val uiState: StateFlow<ToolDetailUiState> = _uiState.asStateFlow()

    fun loadToolDetails(toolId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val tool = toolRepository.getToolById(toolId)
                if (tool != null) {
                    val checkoutHistory = toolRepository.getCheckoutHistoryForTool(toolId)
                    val activeCheckout = toolRepository.getActiveCheckoutForTool(toolId)
                    val checkedOutUser = activeCheckout?.let { 
                        toolRepository.getUserById(it.userId) 
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        tool = tool,
                        checkoutHistory = checkoutHistory,
                        activeCheckout = activeCheckout,
                        checkedOutUser = checkedOutUser,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Tool not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load tool details"
                )
            }
        }
    }

    fun refreshToolDetails(toolId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                // Sync with server first
                toolRepository.syncTools()
                loadToolDetails(toolId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh tool details"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun updateToolStatus(toolId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                val currentTool = _uiState.value.tool
                if (currentTool != null) {
                    val updatedTool = currentTool.copy(status = newStatus)
                    toolRepository.updateTool(updatedTool).collect { result ->
                        result.fold(
                            onSuccess = { tool ->
                                _uiState.value = _uiState.value.copy(
                                    tool = tool,
                                    error = null
                                )
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    error = error.message ?: "Failed to update tool status"
                                )
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update tool status"
                )
            }
        }
    }

    fun updateToolLocation(toolId: Int, newLocation: String) {
        viewModelScope.launch {
            try {
                val currentTool = _uiState.value.tool
                if (currentTool != null) {
                    val updatedTool = currentTool.copy(location = newLocation)
                    toolRepository.updateTool(updatedTool).collect { result ->
                        result.fold(
                            onSuccess = { tool ->
                                _uiState.value = _uiState.value.copy(
                                    tool = tool,
                                    error = null
                                )
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    error = error.message ?: "Failed to update tool location"
                                )
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update tool location"
                )
            }
        }
    }

    fun updateToolNotes(toolId: Int, newNotes: String) {
        viewModelScope.launch {
            try {
                val currentTool = _uiState.value.tool
                if (currentTool != null) {
                    val updatedTool = currentTool.copy(notes = newNotes)
                    toolRepository.updateTool(updatedTool).collect { result ->
                        result.fold(
                            onSuccess = { tool ->
                                _uiState.value = _uiState.value.copy(
                                    tool = tool,
                                    error = null
                                )
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    error = error.message ?: "Failed to update tool notes"
                                )
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update tool notes"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getMaintenanceStatus(): MaintenanceStatus {
        val tool = _uiState.value.tool ?: return MaintenanceStatus.UNKNOWN
        
        return when {
            tool.status == "Maintenance" -> MaintenanceStatus.IN_MAINTENANCE
            tool.requiresCalibration && tool.calibrationDueDate != null -> {
                val dueDate = tool.calibrationDueDate
                val today = java.time.LocalDate.now().toString()
                when {
                    dueDate < today -> MaintenanceStatus.OVERDUE_CALIBRATION
                    dueDate <= java.time.LocalDate.now().plusDays(30).toString() -> 
                        MaintenanceStatus.CALIBRATION_DUE_SOON
                    else -> MaintenanceStatus.GOOD
                }
            }
            else -> MaintenanceStatus.GOOD
        }
    }
}

data class ToolDetailUiState(
    val tool: Tool? = null,
    val checkoutHistory: List<ToolCheckout> = emptyList(),
    val activeCheckout: ToolCheckout? = null,
    val checkedOutUser: User? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

enum class MaintenanceStatus(val displayName: String, val color: androidx.compose.ui.graphics.Color) {
    GOOD("Good", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    CALIBRATION_DUE_SOON("Calibration Due Soon", androidx.compose.ui.graphics.Color(0xFFFF9800)),
    OVERDUE_CALIBRATION("Overdue Calibration", androidx.compose.ui.graphics.Color(0xFFF44336)),
    IN_MAINTENANCE("In Maintenance", androidx.compose.ui.graphics.Color(0xFF9C27B0)),
    UNKNOWN("Unknown", androidx.compose.ui.graphics.Color(0xFF757575))
}
