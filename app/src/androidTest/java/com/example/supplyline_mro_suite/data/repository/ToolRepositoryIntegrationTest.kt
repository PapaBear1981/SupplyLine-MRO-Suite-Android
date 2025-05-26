package com.example.supplyline_mro_suite.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supplyline_mro_suite.data.local.SupplyLineDatabase
import com.example.supplyline_mro_suite.data.local.dao.ToolCheckoutDao
import com.example.supplyline_mro_suite.data.local.dao.ToolDao
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolCheckout
import com.example.supplyline_mro_suite.data.model.User
import com.example.supplyline_mro_suite.data.remote.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
// Removed Mockito imports - using fake implementation instead

@RunWith(AndroidJUnit4::class)
class ToolRepositoryIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: SupplyLineDatabase
    private lateinit var toolDao: ToolDao
    private lateinit var toolCheckoutDao: ToolCheckoutDao
    private lateinit var repository: ToolRepository

    // Using a fake implementation instead of mock - only implementing methods needed for testing
    private val apiService = object : ApiService {
        override suspend fun login(request: com.example.supplyline_mro_suite.data.model.LoginRequest) = throw NotImplementedError()
        override suspend fun logout() = throw NotImplementedError()
        override suspend fun getCurrentUser() = throw NotImplementedError()
        override suspend fun getUsers() = throw NotImplementedError()
        override suspend fun getUser(id: Int) = throw NotImplementedError()
        override suspend fun createUser(user: com.example.supplyline_mro_suite.data.model.User) = throw NotImplementedError()
        override suspend fun updateUser(id: Int, user: com.example.supplyline_mro_suite.data.model.User) = throw NotImplementedError()
        override suspend fun deleteUser(id: Int) = throw NotImplementedError()
        override suspend fun getTools() = throw NotImplementedError()
        override suspend fun getTool(id: Int) = throw NotImplementedError()
        override suspend fun createTool(tool: com.example.supplyline_mro_suite.data.model.Tool) = throw NotImplementedError()
        override suspend fun updateTool(id: Int, tool: com.example.supplyline_mro_suite.data.model.Tool) = throw NotImplementedError()
        override suspend fun deleteTool(id: Int) = throw NotImplementedError()
        override suspend fun searchTools(query: String) = throw NotImplementedError()
        override suspend fun getToolsByCategory(category: String) = throw NotImplementedError()
        override suspend fun getToolsByStatus(status: String) = throw NotImplementedError()
        override suspend fun getCheckouts() = throw NotImplementedError()
        override suspend fun getActiveCheckouts() = throw NotImplementedError()
        override suspend fun getCheckoutsForUser(userId: Int) = throw NotImplementedError()
        override suspend fun getCheckoutsForTool(toolId: Int) = throw NotImplementedError()
        override suspend fun createCheckout(request: com.example.supplyline_mro_suite.data.model.CheckoutRequest) = throw NotImplementedError()
        override suspend fun returnTool(id: Int, request: com.example.supplyline_mro_suite.data.model.ReturnRequest) = throw NotImplementedError()
        override suspend fun getOverdueCheckouts() = throw NotImplementedError()
        override suspend fun getChemicals() = throw NotImplementedError()
        override suspend fun getChemical(id: Int) = throw NotImplementedError()
        override suspend fun createChemical(chemical: com.example.supplyline_mro_suite.data.model.Chemical) = throw NotImplementedError()
        override suspend fun updateChemical(id: Int, chemical: com.example.supplyline_mro_suite.data.model.Chemical) = throw NotImplementedError()
        override suspend fun deleteChemical(id: Int) = throw NotImplementedError()
        override suspend fun searchChemicals(query: String) = throw NotImplementedError()
        override suspend fun getChemicalsByCategory(category: String) = throw NotImplementedError()
        override suspend fun getChemicalsByStatus(status: String) = throw NotImplementedError()
        override suspend fun getExpiringChemicals() = throw NotImplementedError()
        override suspend fun getLowStockChemicals() = throw NotImplementedError()
        override suspend fun getIssuances() = throw NotImplementedError()
        override suspend fun getIssuancesForChemical(chemicalId: Int) = throw NotImplementedError()
        override suspend fun getIssuancesForUser(userId: Int) = throw NotImplementedError()
        override suspend fun createIssuance(request: com.example.supplyline_mro_suite.data.model.IssueRequest) = throw NotImplementedError()
        override suspend fun getDashboardStats() = throw NotImplementedError()
        override suspend fun getToolAnalytics() = throw NotImplementedError()
        override suspend fun getChemicalAnalytics() = throw NotImplementedError()
    }

    @Before
    fun setup() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SupplyLineDatabase::class.java
        ).allowMainThreadQueries().build()

        toolDao = database.toolDao()
        toolCheckoutDao = database.toolCheckoutDao()

        repository = ToolRepository(toolDao, toolCheckoutDao, apiService)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getAllTools_returnsFlowOfTools() = runTest {
        // Given
        val tools = listOf(
            Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available"),
            Tool(2, "T002", "SN002", "Tool 2", "CL415", "A2", "Available"),
            Tool(3, "T003", "SN003", "Tool 3", "RJ85", "A3", "Checked Out")
        )

        // When
        toolDao.insertTools(tools)
        val result = repository.getAllTools().first()

        // Then
        assertEquals(3, result.size)
        assertEquals("T001", result[0].toolNumber)
        assertEquals("T002", result[1].toolNumber)
        assertEquals("T003", result[2].toolNumber)
    }

    @Test
    fun getToolsByStatus_filtersCorrectly() = runTest {
        // Given
        val tools = listOf(
            Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available"),
            Tool(2, "T002", "SN002", "Tool 2", "General", "A2", "Available"),
            Tool(3, "T003", "SN003", "Tool 3", "General", "A3", "Checked Out")
        )

        // When
        toolDao.insertTools(tools)
        val availableTools = repository.getToolsByStatus("Available").first()
        val checkedOutTools = repository.getToolsByStatus("Checked Out").first()

        // Then
        assertEquals(2, availableTools.size)
        assertEquals(1, checkedOutTools.size)
        assertTrue(availableTools.all { it.status == "Available" })
        assertTrue(checkedOutTools.all { it.status == "Checked Out" })
    }

    @Test
    fun getToolsByCategory_filtersCorrectly() = runTest {
        // Given
        val tools = listOf(
            Tool(1, "T001", "SN001", "Tool 1", "CL415", "A1", "Available"),
            Tool(2, "T002", "SN002", "Tool 2", "CL415", "A2", "Available"),
            Tool(3, "T003", "SN003", "Tool 3", "RJ85", "A3", "Available")
        )

        // When
        toolDao.insertTools(tools)
        val cl415Tools = repository.getToolsByCategory("CL415").first()
        val rj85Tools = repository.getToolsByCategory("RJ85").first()

        // Then
        assertEquals(2, cl415Tools.size)
        assertEquals(1, rj85Tools.size)
        assertTrue(cl415Tools.all { it.category == "CL415" })
        assertTrue(rj85Tools.all { it.category == "RJ85" })
    }

    @Test
    fun searchTools_findsMatchingTools() = runTest {
        // Given
        val tools = listOf(
            Tool(1, "T001", "SN001", "Wrench Set", "General", "A1", "Available"),
            Tool(2, "T002", "SN002", "Screwdriver", "General", "A2", "Available"),
            Tool(3, "T003", "SN003", "Wrench Individual", "General", "A3", "Available")
        )

        // When
        toolDao.insertTools(tools)
        val searchResults = repository.searchTools("Wrench").first()

        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.all { it.description.contains("Wrench") })
    }

    @Test
    fun getToolById_returnsCorrectTool() = runTest {
        // Given
        val tool = Tool(1, "T001", "SN001", "Test Tool", "General", "A1", "Available")

        // When
        toolDao.insertTool(tool)
        val result = repository.getToolById(1)

        // Then
        assertNotNull(result)
        assertEquals("T001", result?.toolNumber)
        assertEquals("Test Tool", result?.description)
    }

    @Test
    fun getToolByNumber_returnsCorrectTool() = runTest {
        // Given
        val tool = Tool(1, "T001", "SN001", "Test Tool", "General", "A1", "Available")

        // When
        toolDao.insertTool(tool)
        val result = repository.getToolByNumber("T001")

        // Then
        assertNotNull(result)
        assertEquals(1, result?.id)
        assertEquals("Test Tool", result?.description)
    }

    @Test
    fun getAllActiveCheckouts_returnsActiveCheckouts() = runTest {
        // Given
        val user = User(1, "EMP001", "John Doe", "Maintenance")
        val tool = Tool(1, "T001", "SN001", "Test Tool", "General", "A1", "Available")
        val checkouts = listOf(
            ToolCheckout(1, 1, 1, "2024-01-01", "2024-01-07", checkedOutBy = "John Doe", isActive = true),
            ToolCheckout(2, 1, 1, "2023-12-01", "2023-12-07", actualReturnDate = "2023-12-06",
                        checkedOutBy = "John Doe", isActive = false)
        )

        // When
        database.userDao().insertUser(user)
        toolDao.insertTool(tool)
        toolCheckoutDao.insertCheckouts(checkouts)
        val activeCheckouts = repository.getAllActiveCheckouts().first()

        // Then
        assertEquals(1, activeCheckouts.size)
        assertTrue(activeCheckouts.all { it.isActive })
    }

    @Test
    fun getCheckoutsForUser_returnsUserCheckouts() = runTest {
        // Given
        val user1 = User(1, "EMP001", "John Doe", "Maintenance")
        val user2 = User(2, "EMP002", "Jane Smith", "Materials")
        val tool1 = Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available")
        val tool2 = Tool(2, "T002", "SN002", "Tool 2", "General", "A2", "Available")

        val checkouts = listOf(
            ToolCheckout(1, 1, 1, "2024-01-01", "2024-01-07", checkedOutBy = "John Doe", isActive = true),
            ToolCheckout(2, 2, 2, "2024-01-02", "2024-01-08", checkedOutBy = "Jane Smith", isActive = true)
        )

        // When
        database.userDao().insertUser(user1)
        database.userDao().insertUser(user2)
        toolDao.insertTool(tool1)
        toolDao.insertTool(tool2)
        toolCheckoutDao.insertCheckouts(checkouts)

        val user1Checkouts = repository.getCheckoutsForUser(1).first()
        val user2Checkouts = repository.getCheckoutsForUser(2).first()

        // Then
        assertEquals(1, user1Checkouts.size)
        assertEquals(1, user2Checkouts.size)
        assertEquals(1, user1Checkouts[0].userId)
        assertEquals(2, user2Checkouts[0].userId)
    }

    @Test
    fun hasLocalData_returnsTrueWhenDataExists() = runTest {
        // Given
        val tool = Tool(1, "T001", "SN001", "Test Tool", "General", "A1", "Available")

        // When
        toolDao.insertTool(tool)
        val hasData = repository.hasLocalData()

        // Then
        assertTrue(hasData)
    }

    @Test
    fun hasLocalData_returnsFalseWhenNoData() = runTest {
        // When
        val hasData = repository.hasLocalData()

        // Then
        assertFalse(hasData)
    }
}
