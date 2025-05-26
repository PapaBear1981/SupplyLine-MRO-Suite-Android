package com.example.supplyline_mro_suite.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supplyline_mro_suite.data.local.SupplyLineDatabase
import com.example.supplyline_mro_suite.data.model.Tool
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ToolDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: SupplyLineDatabase
    private lateinit var toolDao: ToolDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SupplyLineDatabase::class.java
        ).allowMainThreadQueries().build()

        toolDao = database.toolDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertTool_and_getToolById() = runTest {
        // Given
        val tool = Tool(
            id = 1,
            toolNumber = "T001",
            serialNumber = "SN001",
            description = "Test Tool",
            category = "General",
            location = "A1",
            status = "Available"
        )

        // When
        toolDao.insertTool(tool)
        val retrievedTool = toolDao.getToolById(1)

        // Then
        assertNotNull(retrievedTool)
        assertEquals(tool.toolNumber, retrievedTool?.toolNumber)
        assertEquals(tool.serialNumber, retrievedTool?.serialNumber)
        assertEquals(tool.description, retrievedTool?.description)
    }

    @Test
    fun getToolByNumber() = runTest {
        // Given
        val tool = Tool(
            id = 1,
            toolNumber = "T001",
            serialNumber = "SN001",
            description = "Test Tool",
            category = "General",
            location = "A1",
            status = "Available"
        )

        // When
        toolDao.insertTool(tool)
        val retrievedTool = toolDao.getToolByNumber("T001")

        // Then
        assertNotNull(retrievedTool)
        assertEquals(tool.id, retrievedTool?.id)
        assertEquals(tool.description, retrievedTool?.description)
    }

    @Test
    fun getToolsByStatus() = runTest {
        // Given
        val tools = listOf(
            Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available"),
            Tool(2, "T002", "SN002", "Tool 2", "General", "A2", "Available"),
            Tool(3, "T003", "SN003", "Tool 3", "General", "A3", "Checked Out")
        )

        // When
        toolDao.insertTools(tools)
        val availableTools = toolDao.getToolsByStatus("Available").first()

        // Then
        assertEquals(2, availableTools.size)
        assertTrue(availableTools.all { it.status == "Available" })
    }

    @Test
    fun getToolsByCategory() = runTest {
        // Given
        val tools = listOf(
            Tool(1, "T001", "SN001", "Tool 1", "CL415", "A1", "Available"),
            Tool(2, "T002", "SN002", "Tool 2", "CL415", "A2", "Available"),
            Tool(3, "T003", "SN003", "Tool 3", "RJ85", "A3", "Available")
        )

        // When
        toolDao.insertTools(tools)
        val cl415Tools = toolDao.getToolsByCategory("CL415").first()

        // Then
        assertEquals(2, cl415Tools.size)
        assertTrue(cl415Tools.all { it.category == "CL415" })
    }

    @Test
    fun searchTools() = runTest {
        // Given
        val tools = listOf(
            Tool(1, "T001", "SN001", "Wrench Set", "General", "A1", "Available"),
            Tool(2, "T002", "SN002", "Screwdriver", "General", "A2", "Available"),
            Tool(3, "T003", "SN003", "Wrench Individual", "General", "A3", "Available")
        )

        // When
        toolDao.insertTools(tools)
        val searchResults = toolDao.searchTools("Wrench").first()

        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.all { it.description.contains("Wrench") })
    }

    @Test
    fun updateTool() = runTest {
        // Given
        val tool = Tool(
            id = 1,
            toolNumber = "T001",
            serialNumber = "SN001",
            description = "Test Tool",
            category = "General",
            location = "A1",
            status = "Available"
        )
        toolDao.insertTool(tool)

        // When
        val updatedTool = tool.copy(status = "Checked Out", location = "B1")
        toolDao.updateTool(updatedTool)
        val retrievedTool = toolDao.getToolById(1)

        // Then
        assertNotNull(retrievedTool)
        assertEquals("Checked Out", retrievedTool?.status)
        assertEquals("B1", retrievedTool?.location)
    }

    @Test
    fun getToolCountByStatus() = runTest {
        // Given
        val tools = listOf(
            Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available"),
            Tool(2, "T002", "SN002", "Tool 2", "General", "A2", "Available"),
            Tool(3, "T003", "SN003", "Tool 3", "General", "A3", "Checked Out"),
            Tool(4, "T004", "SN004", "Tool 4", "General", "A4", "Maintenance")
        )

        // When
        toolDao.insertTools(tools)
        val availableCount = toolDao.getToolCountByStatus("Available")
        val checkedOutCount = toolDao.getToolCountByStatus("Checked Out")

        // Then
        assertEquals(2, availableCount)
        assertEquals(1, checkedOutCount)
    }

    @Test
    fun getToolsDueSoonForCalibration() = runTest {
        // Given - Use dynamic dates to avoid time-dependent test failures
        val dueSoon = LocalDate.now().plusDays(5).toString()
        val farFuture = LocalDate.now().plusYears(1).toString()

        val tools = listOf(
            Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available",
                 requiresCalibration = true, calibrationDueDate = dueSoon),
            Tool(2, "T002", "SN002", "Tool 2", "General", "A2", "Available",
                 requiresCalibration = true, calibrationDueDate = farFuture),
            Tool(3, "T003", "SN003", "Tool 3", "General", "A3", "Available",
                 requiresCalibration = false)
        )

        // When
        toolDao.insertTools(tools)
        val dueSoonTools = toolDao.getToolsDueSoonForCalibration().first()

        // Then
        assertTrue(dueSoonTools.isNotEmpty())
        assertTrue(dueSoonTools.all { it.requiresCalibration })
    }
}
