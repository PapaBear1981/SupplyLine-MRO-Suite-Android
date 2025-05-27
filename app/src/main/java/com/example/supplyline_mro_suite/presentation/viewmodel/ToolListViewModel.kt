package com.example.supplyline_mro_suite.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supplyline_mro_suite.data.repository.ToolRepository
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolWithCheckout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class ToolListViewModel @Inject constructor(
    private val toolRepository: ToolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolListUiState())
    val uiState: StateFlow<ToolListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(ToolFilter.ALL)
    private val _selectedSort = MutableStateFlow(ToolSort.NAME_ASC)

    init {
        observeSearchAndFilters()
        loadTools()
    }

    private fun observeSearchAndFilters() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _selectedFilter,
                _selectedSort
            ) { query, filter, sort ->
                Triple(query, filter, sort)
            }.collect { (query, filter, sort) ->
                loadFilteredTools(query, filter, sort)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateFilter(filter: ToolFilter) {
        _selectedFilter.value = filter
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun updateSort(sort: ToolSort) {
        _selectedSort.value = sort
        _uiState.value = _uiState.value.copy(selectedSort = sort)
    }

    fun refreshTools() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                toolRepository.syncTools()
                loadFilteredTools(_searchQuery.value, _selectedFilter.value, _selectedSort.value)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh tools"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    private fun loadTools() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadFilteredTools(_searchQuery.value, _selectedFilter.value, _selectedSort.value)
        }
    }

    private fun loadFilteredTools(query: String, filter: ToolFilter, sort: ToolSort) {
        viewModelScope.launch {
            try {
                val toolsFlow = when {
                    query.isNotEmpty() -> toolRepository.searchTools(query)
                    filter != ToolFilter.ALL -> when (filter) {
                        ToolFilter.AVAILABLE -> toolRepository.getToolsByStatus("Available")
                        ToolFilter.CHECKED_OUT -> toolRepository.getToolsByStatus("Checked Out")
                        ToolFilter.MAINTENANCE -> toolRepository.getToolsByStatus("Maintenance")
                        ToolFilter.CALIBRATION_DUE -> toolRepository.getToolsDueSoonForCalibration()
                        ToolFilter.OVERDUE_CALIBRATION -> toolRepository.getOverdueCalibrationTools()
                        else -> toolRepository.getAllTools()
                    }
                    else -> toolRepository.getAllTools()
                }

                toolsFlow.collect { tools ->
                    val sortedTools = sortTools(tools, sort)
                    val toolsWithCheckout = loadToolsWithCheckoutInfo(sortedTools)
                    
                    _uiState.value = _uiState.value.copy(
                        tools = toolsWithCheckout,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load tools"
                )
            }
        }
    }

    private suspend fun loadToolsWithCheckoutInfo(tools: List<Tool>): List<ToolWithCheckout> {
        return tools.map { tool ->
            val checkout = if (tool.status == "Checked Out") {
                toolRepository.getActiveCheckoutForTool(tool.id)
            } else null
            
            val user = checkout?.let { 
                toolRepository.getUserById(it.userId) 
            }
            
            ToolWithCheckout(tool, checkout, user)
        }
    }

    private fun sortTools(tools: List<Tool>, sort: ToolSort): List<Tool> {
        return when (sort) {
            ToolSort.NAME_ASC -> tools.sortedBy { it.toolNumber }
            ToolSort.NAME_DESC -> tools.sortedByDescending { it.toolNumber }
            ToolSort.STATUS_ASC -> tools.sortedBy { it.status }
            ToolSort.STATUS_DESC -> tools.sortedByDescending { it.status }
            ToolSort.CATEGORY_ASC -> tools.sortedBy { it.category }
            ToolSort.CATEGORY_DESC -> tools.sortedByDescending { it.category }
            ToolSort.LOCATION_ASC -> tools.sortedBy { it.location }
            ToolSort.LOCATION_DESC -> tools.sortedByDescending { it.location }
            ToolSort.CALIBRATION_DUE -> tools.sortedBy { it.calibrationDueDate ?: "9999-12-31" }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getToolCategories(): List<String> {
        return listOf("CL415", "RJ85", "Q400", "Engine", "CNC", "Sheetmetal", "General")
    }

    fun getToolStatuses(): List<String> {
        return listOf("Available", "Checked Out", "Maintenance", "Retired")
    }
}

data class ToolListUiState(
    val tools: List<ToolWithCheckout> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: ToolFilter = ToolFilter.ALL,
    val selectedSort: ToolSort = ToolSort.NAME_ASC
)

enum class ToolFilter(val displayName: String) {
    ALL("All Tools"),
    AVAILABLE("Available"),
    CHECKED_OUT("Checked Out"),
    MAINTENANCE("Maintenance"),
    CALIBRATION_DUE("Calibration Due"),
    OVERDUE_CALIBRATION("Overdue Calibration")
}

enum class ToolSort(val displayName: String) {
    NAME_ASC("Name A-Z"),
    NAME_DESC("Name Z-A"),
    STATUS_ASC("Status A-Z"),
    STATUS_DESC("Status Z-A"),
    CATEGORY_ASC("Category A-Z"),
    CATEGORY_DESC("Category Z-A"),
    LOCATION_ASC("Location A-Z"),
    LOCATION_DESC("Location Z-A"),
    CALIBRATION_DUE("Calibration Due")
}
