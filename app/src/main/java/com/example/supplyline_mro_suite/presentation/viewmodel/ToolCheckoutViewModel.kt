package com.example.supplyline_mro_suite.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supplyline_mro_suite.data.repository.ToolRepository
import com.example.supplyline_mro_suite.data.repository.UserRepository
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolCheckout
import com.example.supplyline_mro_suite.data.model.CheckoutRequest
import com.example.supplyline_mro_suite.data.model.ReturnRequest
import com.example.supplyline_mro_suite.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ToolCheckoutViewModel @Inject constructor(
    private val toolRepository: ToolRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolCheckoutUiState())
    val uiState: StateFlow<ToolCheckoutUiState> = _uiState.asStateFlow()

    fun loadToolForCheckout(toolId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val tool = toolRepository.getToolById(toolId)
                if (tool != null) {
                    val activeCheckout = toolRepository.getActiveCheckoutForTool(toolId)
                    val checkedOutUser = activeCheckout?.let {
                        toolRepository.getUserById(it.userId)
                    }

                    _uiState.value = _uiState.value.copy(
                        tool = tool,
                        activeCheckout = activeCheckout,
                        checkedOutUser = checkedOutUser,
                        isLoading = false,
                        error = null,
                        // Set default return date to 7 days from now
                        expectedReturnDate = LocalDate.now().plusDays(7).toString()
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
                    error = e.message ?: "Failed to load tool"
                )
            }
        }
    }

    fun loadToolByQRCode(qrCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Try to find tool by tool number or serial number
                val tool = toolRepository.getToolByNumber(qrCode)
                    ?: toolRepository.getToolBySerialNumber(qrCode)

                if (tool != null) {
                    loadToolForCheckout(tool.id)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Tool not found with QR code: $qrCode"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to find tool"
                )
            }
        }
    }

    fun updateExpectedReturnDate(date: String) {
        _uiState.value = _uiState.value.copy(expectedReturnDate = date)
    }

    fun updateCheckoutNotes(notes: String) {
        _uiState.value = _uiState.value.copy(checkoutNotes = notes)
    }

    fun updateReturnCondition(condition: String) {
        _uiState.value = _uiState.value.copy(returnCondition = condition)
    }

    fun updateReturnNotes(notes: String) {
        _uiState.value = _uiState.value.copy(returnNotes = notes)
    }

    fun checkoutTool() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val tool = currentState.tool

            if (tool == null) {
                _uiState.value = currentState.copy(error = "No tool selected")
                return@launch
            }

            if (tool.status != "Available") {
                _uiState.value = currentState.copy(error = "Tool is not available for checkout")
                return@launch
            }

            _uiState.value = currentState.copy(isProcessing = true)

            try {
                // Get current user
                val currentUser = userRepository.getCurrentUser().first()
                if (currentUser == null) {
                    _uiState.value = currentState.copy(
                        isProcessing = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }

                val checkoutRequest = CheckoutRequest(
                    toolId = tool.id,
                    userId = currentUser.id,
                    expectedReturnDate = currentState.expectedReturnDate,
                    notes = currentState.checkoutNotes.takeIf { it.isNotBlank() }
                )

                toolRepository.checkoutTool(checkoutRequest).collect { result ->
                    result.fold(
                        onSuccess = { checkout ->
                            _uiState.value = currentState.copy(
                                isProcessing = false,
                                checkoutSuccess = true,
                                activeCheckout = checkout,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = currentState.copy(
                                isProcessing = false,
                                error = error.message ?: "Failed to checkout tool"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to checkout tool"
                )
            }
        }
    }

    fun returnTool() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val tool = currentState.tool
            val activeCheckout = currentState.activeCheckout

            if (tool == null || activeCheckout == null) {
                _uiState.value = currentState.copy(error = "No active checkout found")
                return@launch
            }

            _uiState.value = currentState.copy(isProcessing = true)

            try {
                // Get current user
                val currentUser = userRepository.getCurrentUser().first()
                if (currentUser == null) {
                    _uiState.value = currentState.copy(
                        isProcessing = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }

                val returnRequest = ReturnRequest(
                    checkoutId = activeCheckout.id,
                    condition = currentState.returnCondition.takeIf { it.isNotBlank() } ?: "Good",
                    notes = currentState.returnNotes.takeIf { it.isNotBlank() }
                )

                toolRepository.returnTool(returnRequest).collect { result ->
                    result.fold(
                        onSuccess = { checkout ->
                            _uiState.value = currentState.copy(
                                isProcessing = false,
                                returnSuccess = true,
                                activeCheckout = null,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = currentState.copy(
                                isProcessing = false,
                                error = error.message ?: "Failed to return tool"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to return tool"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = ToolCheckoutUiState()
    }

    fun getAvailableConditions(): List<String> {
        return listOf("Good", "Fair", "Poor", "Damaged", "Needs Maintenance")
    }

    fun isValidReturnDate(date: String): Boolean {
        return try {
            val returnDate = LocalDate.parse(date)
            val today = LocalDate.now()
            !returnDate.isBefore(today)
        } catch (e: Exception) {
            false
        }
    }
}

data class ToolCheckoutUiState(
    val tool: Tool? = null,
    val activeCheckout: ToolCheckout? = null,
    val checkedOutUser: User? = null,
    val expectedReturnDate: String = "",
    val checkoutNotes: String = "",
    val returnCondition: String = "",
    val returnNotes: String = "",
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val returnSuccess: Boolean = false,
    val error: String? = null
)
