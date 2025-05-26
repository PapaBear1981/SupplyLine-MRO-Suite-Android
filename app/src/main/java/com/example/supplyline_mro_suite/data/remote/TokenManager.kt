package com.example.supplyline_mro_suite.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages authentication tokens and provides token refresh functionality
 */
@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_EXPIRY_KEY = stringPreferencesKey("token_expiry")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val EMPLOYEE_NUMBER_KEY = stringPreferencesKey("employee_number")
    }

    /**
     * Get the current auth token
     */
    suspend fun getAuthToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }.first()
    }

    /**
     * Get the current auth token synchronously (for interceptors)
     */
    fun getAuthTokenSync(): String? {
        return runBlocking {
            getAuthToken()
        }
    }

    /**
     * Get the refresh token
     */
    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }.first()
    }

    /**
     * Save authentication tokens
     */
    suspend fun saveTokens(
        authToken: String,
        refreshToken: String? = null,
        expiryTime: String? = null
    ) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = authToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            expiryTime?.let { preferences[TOKEN_EXPIRY_KEY] = it }
        }
    }

    /**
     * Save user information
     */
    suspend fun saveUserInfo(userId: String, employeeNumber: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[EMPLOYEE_NUMBER_KEY] = employeeNumber
        }
    }

    /**
     * Clear all stored tokens and user info
     */
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(TOKEN_EXPIRY_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(EMPLOYEE_NUMBER_KEY)
        }
    }

    /**
     * Check if user is logged in (has valid token)
     */
    suspend fun isLoggedIn(): Boolean {
        val token = getAuthToken()
        return !token.isNullOrEmpty()
    }

    /**
     * Get stored user ID
     */
    suspend fun getUserId(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }.first()
    }

    /**
     * Get stored employee number
     */
    suspend fun getEmployeeNumber(): String? {
        return dataStore.data.map { preferences ->
            preferences[EMPLOYEE_NUMBER_KEY]
        }.first()
    }

    /**
     * Check if token is expired (if expiry time is stored)
     */
    suspend fun isTokenExpired(): Boolean {
        val expiryTime = dataStore.data.map { preferences ->
            preferences[TOKEN_EXPIRY_KEY]
        }.first()
        
        return if (expiryTime != null) {
            try {
                val expiry = expiryTime.toLong()
                val currentTime = System.currentTimeMillis() / 1000
                currentTime >= expiry
            } catch (e: NumberFormatException) {
                true // Consider expired if we can't parse the time
            }
        } else {
            false // If no expiry time stored, assume not expired
        }
    }

    /**
     * Get authorization header value
     */
    suspend fun getAuthorizationHeader(): String? {
        val token = getAuthToken()
        return if (!token.isNullOrEmpty()) {
            "Bearer $token"
        } else {
            null
        }
    }

    /**
     * Get authorization header value synchronously
     */
    fun getAuthorizationHeaderSync(): String? {
        return runBlocking {
            getAuthorizationHeader()
        }
    }
}
