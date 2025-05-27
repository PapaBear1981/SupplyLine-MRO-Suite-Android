package com.example.supplyline_mro_suite.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling authentication operations
 */
@Singleton
class AuthRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val validator: AuthenticationValidator
) {

    companion object {
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
        private val EMPLOYEE_NUMBER_KEY = stringPreferencesKey("employee_number")
    }

    /**
     * Flow that emits the current authentication state
     */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

    /**
     * Check if user is currently authenticated
     */
    suspend fun checkAuthenticationState(): Boolean {
        return isLoggedIn.first()
    }

    /**
     * Authenticate user with credentials
     */
    suspend fun authenticate(employeeNumber: String, password: String): AuthResult {
        // Validate credentials first
        val validationResult = validator.validateCredentials(employeeNumber, password)
        if (validationResult is ValidationResult.Error) {
            return AuthResult.Error(validationResult.message)
        }

        return try {
            // For demo purposes, use hardcoded credentials
            // In production, this would make an API call
            if (employeeNumber == "ADMIN001" && password == "Password123!") {
                // Store authentication state
                dataStore.edit { preferences ->
                    preferences[IS_LOGGED_IN_KEY] = true
                    preferences[USER_TOKEN_KEY] = "demo_token_${System.currentTimeMillis()}"
                    preferences[EMPLOYEE_NUMBER_KEY] = employeeNumber
                }
                AuthResult.Success("Authentication successful")
            } else {
                AuthResult.Error("Invalid credentials")
            }
        } catch (e: Exception) {
            AuthResult.Error("Authentication failed. Please try again.")
        }
    }

    /**
     * Logout user and clear stored data
     */
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Get stored user token
     */
    suspend fun getUserToken(): String? {
        return dataStore.data.first()[USER_TOKEN_KEY]
    }

    /**
     * Get stored employee number
     */
    suspend fun getEmployeeNumber(): String? {
        return dataStore.data.first()[EMPLOYEE_NUMBER_KEY]
    }
}

/**
 * Represents the result of an authentication operation
 */
sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
