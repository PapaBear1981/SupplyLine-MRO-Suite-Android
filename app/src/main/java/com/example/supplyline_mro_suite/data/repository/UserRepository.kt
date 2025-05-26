package com.example.supplyline_mro_suite.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.supplyline_mro_suite.data.local.dao.UserDao
import com.example.supplyline_mro_suite.data.model.User
import com.example.supplyline_mro_suite.data.model.LoginRequest
import com.example.supplyline_mro_suite.data.model.LoginResponse
import com.example.supplyline_mro_suite.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val EMPLOYEE_NUMBER_KEY = stringPreferencesKey("employee_number")
    }
    
    // Authentication
    suspend fun login(employeeNumber: String, password: String): Flow<Result<LoginResponse>> = flow {
        try {
            val request = LoginRequest(employeeNumber, password)
            val response = apiService.login(request)
            
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                
                if (loginResponse.success && loginResponse.user != null) {
                    // Save user data locally
                    userDao.insertUser(loginResponse.user)
                    
                    // Save auth token and user info
                    dataStore.edit { preferences ->
                        loginResponse.token?.let { preferences[AUTH_TOKEN_KEY] = it }
                        preferences[USER_ID_KEY] = loginResponse.user.id.toString()
                        preferences[EMPLOYEE_NUMBER_KEY] = loginResponse.user.employeeNumber
                    }
                    
                    emit(Result.success(loginResponse))
                } else {
                    emit(Result.failure(Exception(loginResponse.message)))
                }
            } else {
                emit(Result.failure(Exception("Login failed: ${response.message()}")))
            }
        } catch (e: Exception) {
            // Offline login - check local database
            val user = userDao.getUserByEmployeeNumber(employeeNumber)
            if (user != null) {
                // In a real app, you'd verify the password hash
                val offlineResponse = LoginResponse(
                    success = true,
                    message = "Offline login successful",
                    user = user,
                    token = null
                )
                
                dataStore.edit { preferences ->
                    preferences[USER_ID_KEY] = user.id.toString()
                    preferences[EMPLOYEE_NUMBER_KEY] = user.employeeNumber
                }
                
                emit(Result.success(offlineResponse))
            } else {
                emit(Result.failure(Exception("Offline login failed: User not found")))
            }
        }
    }
    
    suspend fun logout(): Flow<Result<Unit>> = flow {
        try {
            apiService.logout()
            
            // Clear local auth data
            dataStore.edit { preferences ->
                preferences.remove(AUTH_TOKEN_KEY)
                preferences.remove(USER_ID_KEY)
                preferences.remove(EMPLOYEE_NUMBER_KEY)
            }
            
            emit(Result.success(Unit))
        } catch (e: Exception) {
            // Clear local data even if server call fails
            dataStore.edit { preferences ->
                preferences.remove(AUTH_TOKEN_KEY)
                preferences.remove(USER_ID_KEY)
                preferences.remove(EMPLOYEE_NUMBER_KEY)
            }
            
            emit(Result.success(Unit))
        }
    }
    
    suspend fun getCurrentUser(): Flow<User?> {
        return dataStore.data.map { preferences ->
            val userId = preferences[USER_ID_KEY]?.toIntOrNull()
            userId?.let { userDao.getUserById(it) }
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[USER_ID_KEY] != null
    }
    
    suspend fun getAuthToken(): String? {
        val preferences = dataStore.data.first()
        return preferences[AUTH_TOKEN_KEY]
    }
    
    // User management
    fun getAllActiveUsers(): Flow<List<User>> = userDao.getAllActiveUsers()
    
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    
    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)
    
    suspend fun getUserByEmployeeNumber(employeeNumber: String): User? = 
        userDao.getUserByEmployeeNumber(employeeNumber)
    
    suspend fun syncUsers(): Flow<Result<List<User>>> = flow {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                userDao.deleteAllUsers()
                userDao.insertUsers(users)
                emit(Result.success(users))
            } else {
                emit(Result.failure(Exception("Failed to sync users: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun createUser(user: User): Flow<Result<User>> = flow {
        try {
            val response = apiService.createUser(user)
            if (response.isSuccessful) {
                val createdUser = response.body()!!
                userDao.insertUser(createdUser)
                emit(Result.success(createdUser))
            } else {
                emit(Result.failure(Exception("Failed to create user: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun updateUser(user: User): Flow<Result<User>> = flow {
        try {
            val response = apiService.updateUser(user.id, user)
            if (response.isSuccessful) {
                val updatedUser = response.body()!!
                userDao.updateUser(updatedUser)
                emit(Result.success(updatedUser))
            } else {
                emit(Result.failure(Exception("Failed to update user: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    // Statistics
    suspend fun getUserStats(): UserStats {
        val totalUsers = userDao.getActiveUserCount()
        val maintenanceUsers = userDao.getUserCountByDepartment("Maintenance")
        val materialsUsers = userDao.getUserCountByDepartment("Materials")
        val adminUsers = userDao.getUserCountByDepartment("Admin")
        
        return UserStats(
            totalUsers = totalUsers,
            maintenanceUsers = maintenanceUsers,
            materialsUsers = materialsUsers,
            adminUsers = adminUsers
        )
    }
    
    suspend fun hasLocalData(): Boolean {
        return userDao.getActiveUserCount() > 0
    }
}

data class UserStats(
    val totalUsers: Int,
    val maintenanceUsers: Int,
    val materialsUsers: Int,
    val adminUsers: Int
)
