package com.example.supplyline_mro_suite.data.repository


import com.example.supplyline_mro_suite.data.local.dao.UserDao
import com.example.supplyline_mro_suite.data.model.User
import com.example.supplyline_mro_suite.data.model.LoginRequest
import com.example.supplyline_mro_suite.data.model.LoginResponse
import com.example.supplyline_mro_suite.data.remote.ApiService
import com.example.supplyline_mro_suite.data.remote.TokenManager
import com.example.supplyline_mro_suite.data.remote.NetworkErrorHandler
import com.example.supplyline_mro_suite.data.remote.NetworkResult
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
    private val tokenManager: TokenManager,
    private val networkErrorHandler: NetworkErrorHandler
) {



    // Authentication
    suspend fun login(employeeNumber: String, password: String): Flow<NetworkResult<LoginResponse>> = flow {
        emit(NetworkResult.Loading())

        val request = LoginRequest(employeeNumber, password)
        val result = networkErrorHandler.safeApiCall { apiService.login(request) }

        when (result) {
            is NetworkResult.Success -> {
                val loginResponse = result.data

                if (loginResponse.success && loginResponse.user != null) {
                    // Save user data locally
                    userDao.insertUser(loginResponse.user)

                    // Save auth token and user info using TokenManager
                    loginResponse.token?.let { token ->
                        tokenManager.saveTokens(token)
                        tokenManager.saveUserInfo(
                            loginResponse.user.id.toString(),
                            loginResponse.user.employeeNumber
                        )
                    }

                    emit(NetworkResult.Success(loginResponse))
                } else {
                    emit(NetworkResult.Error(
                        com.example.supplyline_mro_suite.data.remote.NetworkException.AuthenticationError(
                            loginResponse.message
                        )
                    ))
                }
            }
            is NetworkResult.Error -> {
                // Try offline login
                val user = userDao.getUserByEmployeeNumber(employeeNumber)
                if (user != null) {
                    // In a real app, you'd verify the password hash
                    val offlineResponse = LoginResponse(
                        success = true,
                        message = "Offline login successful",
                        user = user,
                        token = null
                    )

                    tokenManager.saveUserInfo(
                        user.id.toString(),
                        user.employeeNumber
                    )

                    emit(NetworkResult.Success(offlineResponse))
                } else {
                    emit(result) // Emit the original network error
                }
            }
            is NetworkResult.Loading -> { /* Already emitted loading */ }
        }
    }

    suspend fun logout(): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading())

        val result = networkErrorHandler.safeApiCall { apiService.logout() }

        // Clear local data regardless of server response
        tokenManager.clearTokens()

        when (result) {
            is NetworkResult.Success -> emit(NetworkResult.Success(Unit))
            is NetworkResult.Error -> emit(NetworkResult.Success(Unit)) // Still success since we cleared local data
            is NetworkResult.Loading -> { /* Already emitted loading */ }
        }
    }

    suspend fun getCurrentUser(): User? {
        val userId = tokenManager.getUserId()?.toIntOrNull()
        return userId?.let { userDao.getUserById(it) }
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    suspend fun getAuthToken(): String? {
        return tokenManager.getAuthToken()
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
