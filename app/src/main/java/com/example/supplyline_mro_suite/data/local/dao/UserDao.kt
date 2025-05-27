package com.example.supplyline_mro_suite.data.local.dao

import androidx.room.*
import com.example.supplyline_mro_suite.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE employee_number = :employeeNumber")
    suspend fun getUserByEmployeeNumber(employeeNumber: String): User?

    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveUsers(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT COUNT(*) FROM users WHERE is_active = 1")
    suspend fun getActiveUserCount(): Int

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT COUNT(*) FROM users WHERE department = :department AND is_active = 1")
    suspend fun getUserCountByDepartment(department: String): Int
}
