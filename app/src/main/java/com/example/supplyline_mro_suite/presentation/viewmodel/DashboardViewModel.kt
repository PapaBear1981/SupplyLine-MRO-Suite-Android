package com.example.supplyline_mro_suite.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supplyline_mro_suite.data.repository.ToolRepository
import com.example.supplyline_mro_suite.data.repository.ToolStats
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolCheckout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val toolRepository: ToolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboardData()
        observeDataChanges()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load tool statistics
                val stats = toolRepository.getToolStats()

                // Load recent activity (overdue checkouts, etc.)
                val overdueCheckouts = toolRepository.getOverdueCheckouts().first()
                val checkoutsDueSoon = toolRepository.getCheckoutsDueSoon().first()
                val calibrationDue = toolRepository.getToolsDueSoonForCalibration().first()
                val calibrationOverdue = toolRepository.getOverdueCalibrationTools().first()

                val alerts = buildList {
                    if (overdueCheckouts.isNotEmpty()) {
                        add(DashboardAlert(
                            id = "overdue_checkouts",
                            title = "${overdueCheckouts.size} Tools Overdue",
                            description = "Tools need to be returned",
                            type = AlertType.ERROR,
                            count = overdueCheckouts.size
                        ))
                    }

                    if (checkoutsDueSoon.isNotEmpty()) {
                        add(DashboardAlert(
                            id = "due_soon",
                            title = "${checkoutsDueSoon.size} Tools Due Soon",
                            description = "Tools due within 3 days",
                            type = AlertType.WARNING,
                            count = checkoutsDueSoon.size
                        ))
                    }

                    if (calibrationDue.isNotEmpty()) {
                        add(DashboardAlert(
                            id = "calibration_due",
                            title = "${calibrationDue.size} Tools Need Calibration",
                            description = "Calibration due within 30 days",
                            type = AlertType.INFO,
                            count = calibrationDue.size
                        ))
                    }

                    if (calibrationOverdue.isNotEmpty()) {
                        add(DashboardAlert(
                            id = "calibration_overdue",
                            title = "${calibrationOverdue.size} Calibrations Overdue",
                            description = "Tools past calibration due date",
                            type = AlertType.ERROR,
                            count = calibrationOverdue.size
                        ))
                    }
                }

                // Generate recent activity
                val recentActivity = generateRecentActivity(overdueCheckouts, checkoutsDueSoon)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    toolStats = stats,
                    alerts = alerts,
                    recentActivity = recentActivity,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun observeDataChanges() {
        viewModelScope.launch {
            // Observe tool changes and update stats
            toolRepository.getAllTools()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { tools ->
                    if (!_uiState.value.isLoading) {
                        updateToolStats()
                    }
                }
        }

        viewModelScope.launch {
            // Observe checkout changes
            toolRepository.getAllActiveCheckouts()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { checkouts ->
                    if (!_uiState.value.isLoading) {
                        updateAlerts()
                    }
                }
        }
    }

    private suspend fun updateToolStats() {
        try {
            val stats = toolRepository.getToolStats()
            _uiState.value = _uiState.value.copy(toolStats = stats)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    private suspend fun updateAlerts() {
        try {
            val overdueCheckouts = toolRepository.getOverdueCheckouts().first()
            val checkoutsDueSoon = toolRepository.getCheckoutsDueSoon().first()
            val calibrationDue = toolRepository.getToolsDueSoonForCalibration().first()
            val calibrationOverdue = toolRepository.getOverdueCalibrationTools().first()

            val alerts = buildList {
                if (overdueCheckouts.isNotEmpty()) {
                    add(DashboardAlert(
                        id = "overdue_checkouts",
                        title = "${overdueCheckouts.size} Tools Overdue",
                        description = "Tools need to be returned",
                        type = AlertType.ERROR,
                        count = overdueCheckouts.size
                    ))
                }

                if (checkoutsDueSoon.isNotEmpty()) {
                    add(DashboardAlert(
                        id = "due_soon",
                        title = "${checkoutsDueSoon.size} Tools Due Soon",
                        description = "Tools due within 3 days",
                        type = AlertType.WARNING,
                        count = checkoutsDueSoon.size
                    ))
                }

                if (calibrationDue.isNotEmpty()) {
                    add(DashboardAlert(
                        id = "calibration_due",
                        title = "${calibrationDue.size} Tools Need Calibration",
                        description = "Calibration due within 30 days",
                        type = AlertType.INFO,
                        count = calibrationDue.size
                    ))
                }

                if (calibrationOverdue.isNotEmpty()) {
                    add(DashboardAlert(
                        id = "calibration_overdue",
                        title = "${calibrationOverdue.size} Calibrations Overdue",
                        description = "Tools past calibration due date",
                        type = AlertType.ERROR,
                        count = calibrationOverdue.size
                    ))
                }
            }

            _uiState.value = _uiState.value.copy(alerts = alerts)

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                // Sync data from server
                toolRepository.syncTools().collect { result ->
                    result.onSuccess {
                        loadDashboardData()
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }

                toolRepository.syncCheckouts().collect { result ->
                    result.onFailure { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun dismissAlert(alertId: String) {
        val updatedAlerts = _uiState.value.alerts.filter { it.id != alertId }
        _uiState.value = _uiState.value.copy(alerts = updatedAlerts)
    }

    private fun generateRecentActivity(
        overdueCheckouts: List<ToolCheckout>,
        checkoutsDueSoon: List<ToolCheckout>
    ): List<ActivityItem> {
        return buildList {
            // Add overdue items
            overdueCheckouts.take(3).forEach { checkout ->
                add(ActivityItem(
                    id = "overdue_${checkout.id}",
                    title = "Tool Overdue",
                    description = "Tool ${checkout.toolId} is overdue for return",
                    timestamp = checkout.expectedReturnDate,
                    type = ActivityType.OVERDUE
                ))
            }

            // Add due soon items
            checkoutsDueSoon.take(2).forEach { checkout ->
                add(ActivityItem(
                    id = "due_soon_${checkout.id}",
                    title = "Tool Due Soon",
                    description = "Tool ${checkout.toolId} due for return",
                    timestamp = checkout.expectedReturnDate,
                    type = ActivityType.DUE_SOON
                ))
            }
        }.sortedByDescending { it.timestamp }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val toolStats: ToolStats? = null,
    val alerts: List<DashboardAlert> = emptyList(),
    val recentActivity: List<ActivityItem> = emptyList(),
    val error: String? = null
)

data class DashboardAlert(
    val id: String,
    val title: String,
    val description: String,
    val type: AlertType,
    val count: Int = 0
)

enum class AlertType {
    INFO, WARNING, ERROR
}

data class ActivityItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val type: ActivityType
)

enum class ActivityType {
    CHECKOUT, RETURN, OVERDUE, DUE_SOON, CALIBRATION
}
