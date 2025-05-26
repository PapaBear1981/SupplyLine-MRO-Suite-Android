package com.example.supplyline_mro_suite.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supplyline_mro_suite.data.local.SupplyLineDatabase
import com.example.supplyline_mro_suite.data.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: SupplyLineDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SupplyLineDatabase::class.java
        ).allowMainThreadQueries().build()
        
        userDao = database.userDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertUser_and_getUserById() = runTest {
        // Given
        val user = User(
            id = 1,
            employeeNumber = "EMP001",
            name = "John Doe",
            department = "Maintenance",
            isAdmin = false
        )

        // When
        userDao.insertUser(user)
        val retrievedUser = userDao.getUserById(1)

        // Then
        assertNotNull(retrievedUser)
        assertEquals(user.employeeNumber, retrievedUser?.employeeNumber)
        assertEquals(user.name, retrievedUser?.name)
        assertEquals(user.department, retrievedUser?.department)
    }

    @Test
    fun getUserByEmployeeNumber() = runTest {
        // Given
        val user = User(
            id = 1,
            employeeNumber = "EMP001",
            name = "John Doe",
            department = "Maintenance"
        )

        // When
        userDao.insertUser(user)
        val retrievedUser = userDao.getUserByEmployeeNumber("EMP001")

        // Then
        assertNotNull(retrievedUser)
        assertEquals(user.id, retrievedUser?.id)
        assertEquals(user.name, retrievedUser?.name)
    }

    @Test
    fun getAllActiveUsers() = runTest {
        // Given
        val users = listOf(
            User(1, "EMP001", "John Doe", "Maintenance", isActive = true),
            User(2, "EMP002", "Jane Smith", "Materials", isActive = true),
            User(3, "EMP003", "Bob Johnson", "Admin", isActive = false)
        )

        // When
        userDao.insertUsers(users)
        val activeUsers = userDao.getAllActiveUsers().first()

        // Then
        assertEquals(2, activeUsers.size)
        assertTrue(activeUsers.all { it.isActive })
    }

    @Test
    fun updateUser() = runTest {
        // Given
        val user = User(
            id = 1,
            employeeNumber = "EMP001",
            name = "John Doe",
            department = "Maintenance"
        )
        userDao.insertUser(user)

        // When
        val updatedUser = user.copy(name = "John Smith", department = "Materials")
        userDao.updateUser(updatedUser)
        val retrievedUser = userDao.getUserById(1)

        // Then
        assertNotNull(retrievedUser)
        assertEquals("John Smith", retrievedUser?.name)
        assertEquals("Materials", retrievedUser?.department)
    }

    @Test
    fun deleteUser() = runTest {
        // Given
        val user = User(
            id = 1,
            employeeNumber = "EMP001",
            name = "John Doe",
            department = "Maintenance"
        )
        userDao.insertUser(user)

        // When
        userDao.deleteUser(user)
        val retrievedUser = userDao.getUserById(1)

        // Then
        assertNull(retrievedUser)
    }

    @Test
    fun getUserCountByDepartment() = runTest {
        // Given
        val users = listOf(
            User(1, "EMP001", "John Doe", "Maintenance", isActive = true),
            User(2, "EMP002", "Jane Smith", "Maintenance", isActive = true),
            User(3, "EMP003", "Bob Johnson", "Materials", isActive = true),
            User(4, "EMP004", "Alice Brown", "Maintenance", isActive = false)
        )

        // When
        userDao.insertUsers(users)
        val maintenanceCount = userDao.getUserCountByDepartment("Maintenance")
        val materialsCount = userDao.getUserCountByDepartment("Materials")

        // Then
        assertEquals(2, maintenanceCount) // Only active users
        assertEquals(1, materialsCount)
    }

    @Test
    fun getActiveUserCount() = runTest {
        // Given
        val users = listOf(
            User(1, "EMP001", "John Doe", "Maintenance", isActive = true),
            User(2, "EMP002", "Jane Smith", "Materials", isActive = true),
            User(3, "EMP003", "Bob Johnson", "Admin", isActive = false)
        )

        // When
        userDao.insertUsers(users)
        val activeCount = userDao.getActiveUserCount()

        // Then
        assertEquals(2, activeCount)
    }
}
