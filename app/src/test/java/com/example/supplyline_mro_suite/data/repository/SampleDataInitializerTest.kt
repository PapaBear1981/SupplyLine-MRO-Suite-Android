package com.example.supplyline_mro_suite.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.supplyline_mro_suite.data.local.dao.ToolDao
import com.example.supplyline_mro_suite.data.local.dao.UserDao
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.User
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SampleDataInitializerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val toolDao = mockk<ToolDao>()
    private val userDao = mockk<UserDao>()
    private lateinit var sampleDataInitializer: SampleDataInitializer

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock DAO methods
        coEvery { toolDao.getToolCount() } returns 0
        coEvery { userDao.getUserCount() } returns 0
        coEvery { toolDao.insertTools(any()) } just Runs
        coEvery { userDao.insertUsers(any()) } just Runs
        
        sampleDataInitializer = SampleDataInitializer(toolDao, userDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initializeSampleData should insert tools when database is empty`() = runTest {
        // Given - empty database (mocked above)
        
        // When
        sampleDataInitializer.initializeSampleData()
        advanceUntilIdle()
        
        // Then
        coVerify { toolDao.getToolCount() }
        coVerify { toolDao.insertTools(any()) }
        
        // Verify that 12 tools are inserted
        val toolsSlot = slot<List<Tool>>()
        coVerify { toolDao.insertTools(capture(toolsSlot)) }
        assertEquals(12, toolsSlot.captured.size)
        
        // Verify some specific tools
        val tools = toolsSlot.captured
        assertTrue(tools.any { it.toolNumber == "HT001" && it.description.contains("Torque Wrench") })
        assertTrue(tools.any { it.toolNumber == "CL001" && it.category == "CL415" })
        assertTrue(tools.any { it.toolNumber == "ENG001" && it.category == "Engine" })
        assertTrue(tools.any { it.toolNumber == "RJ001" && it.category == "RJ85" })
        assertTrue(tools.any { it.toolNumber == "Q001" && it.category == "Q400" })
    }

    @Test
    fun `initializeSampleData should insert users when database is empty`() = runTest {
        // Given - empty database (mocked above)
        
        // When
        sampleDataInitializer.initializeSampleData()
        advanceUntilIdle()
        
        // Then
        coVerify { userDao.getUserCount() }
        coVerify { userDao.insertUsers(any()) }
        
        // Verify that 5 users are inserted
        val usersSlot = slot<List<User>>()
        coVerify { userDao.insertUsers(capture(usersSlot)) }
        assertEquals(5, usersSlot.captured.size)
        
        // Verify some specific users
        val users = usersSlot.captured
        assertTrue(users.any { it.employeeNumber == "EMP001" && it.name == "John Smith" })
        assertTrue(users.any { it.employeeNumber == "EMP002" && it.department == "Avionics" })
        assertTrue(users.any { it.employeeNumber == "EMP003" && it.isAdmin })
    }

    @Test
    fun `initializeSampleData should not insert data when database already has data`() = runTest {
        // Given - database with existing data
        coEvery { toolDao.getToolCount() } returns 5
        coEvery { userDao.getUserCount() } returns 3
        
        // When
        sampleDataInitializer.initializeSampleData()
        advanceUntilIdle()
        
        // Then
        coVerify { toolDao.getToolCount() }
        coVerify { userDao.getUserCount() }
        coVerify(exactly = 0) { toolDao.insertTools(any()) }
        coVerify(exactly = 0) { userDao.insertUsers(any()) }
    }

    @Test
    fun `sample tools should have correct categories and statuses`() = runTest {
        // When
        sampleDataInitializer.initializeSampleData()
        advanceUntilIdle()
        
        // Then
        val toolsSlot = slot<List<Tool>>()
        coVerify { toolDao.insertTools(capture(toolsSlot)) }
        val tools = toolsSlot.captured
        
        // Verify categories
        val categories = tools.map { it.category }.distinct()
        assertTrue(categories.contains("General"))
        assertTrue(categories.contains("CL415"))
        assertTrue(categories.contains("RJ85"))
        assertTrue(categories.contains("Q400"))
        assertTrue(categories.contains("Engine"))
        assertTrue(categories.contains("Sheetmetal"))
        assertTrue(categories.contains("CNC"))
        
        // Verify statuses
        val statuses = tools.map { it.status }.distinct()
        assertTrue(statuses.contains("Available"))
        assertTrue(statuses.contains("Checked Out"))
        assertTrue(statuses.contains("Maintenance"))
        
        // Verify some tools require calibration
        assertTrue(tools.any { it.requiresCalibration })
        assertTrue(tools.any { !it.requiresCalibration })
        
        // Verify calibration due dates are set for tools that require calibration
        val calibrationTools = tools.filter { it.requiresCalibration }
        assertTrue(calibrationTools.all { it.calibrationDueDate != null })
    }

    @Test
    fun `sample users should have correct departments and roles`() = runTest {
        // When
        sampleDataInitializer.initializeSampleData()
        advanceUntilIdle()
        
        // Then
        val usersSlot = slot<List<User>>()
        coVerify { userDao.insertUsers(capture(usersSlot)) }
        val users = usersSlot.captured
        
        // Verify departments
        val departments = users.map { it.department }.distinct()
        assertTrue(departments.contains("Maintenance"))
        assertTrue(departments.contains("Avionics"))
        assertTrue(departments.contains("Engine Shop"))
        assertTrue(departments.contains("Sheetmetal"))
        assertTrue(departments.contains("Quality Control"))
        
        // Verify at least one admin user
        assertTrue(users.any { it.isAdmin })
        assertTrue(users.any { !it.isAdmin })
        
        // Verify all users are active
        assertTrue(users.all { it.isActive })
    }
}
