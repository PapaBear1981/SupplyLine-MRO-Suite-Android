package com.example.supplyline_mro_suite.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supplyline_mro_suite.data.auth.AuthRepository
import com.example.supplyline_mro_suite.data.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Check if user is already authenticated
     */
    suspend fun checkAuthenticationState(): Boolean {
        return authRepository.checkAuthenticationState()
    }

    /**
     * Authenticate user with credentials
     */
    fun authenticate(employeeNumber: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, result = null)
            
            val result = authRepository.authenticate(employeeNumber, password)
            
            _authState.value = _authState.value.copy(
                isLoading = false,
                result = result
            )
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState()
        }
    }

    /**
     * Clear authentication result
     */
    fun clearResult() {
        _authState.value = _authState.value.copy(result = null)
    }
}

/**
 * Represents the authentication UI state
 */
data class AuthState(
    val isLoading: Boolean = false,
    val result: AuthResult? = null
)
