package com.example.supplyline_mro_suite.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.supplyline_mro_suite.data.repository.ToolRepository
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolWithCheckout
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ToolListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val toolRepository = mockk<ToolRepository>()
    private lateinit var viewModel: ToolListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock repository methods
        coEvery { toolRepository.getAllTools() } returns flowOf(getSampleTools())
        coEvery { toolRepository.getToolsByStatus(any()) } returns flowOf(getSampleTools().filter { it.status == "Available" })
        coEvery { toolRepository.searchTools(any()) } returns flowOf(getSampleTools().filter { it.toolNumber.contains("HT001") })
        coEvery { toolRepository.getActiveCheckoutForTool(any()) } returns null
        coEvery { toolRepository.getUserById(any()) } returns null
        coEvery { toolRepository.syncTools() } returns Unit
        
        viewModel = ToolListViewModel(toolRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() {
        val initialState = viewModel.uiState.value
        assertTrue(initialState.isLoading)
        assertTrue(initialState.tools.isEmpty())
        assertEquals(ToolFilter.ALL, initialState.selectedFilter)
        assertEquals(ToolSort.NAME_ASC, initialState.selectedSort)
    }

    @Test
    fun `updateSearchQuery should update search query in state`() = runTest {
        val searchQuery = "HT001"
        
        viewModel.updateSearchQuery(searchQuery)
        advanceUntilIdle()
        
        assertEquals(searchQuery, viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `updateFilter should update filter in state`() = runTest {
        val filter = ToolFilter.AVAILABLE
        
        viewModel.updateFilter(filter)
        advanceUntilIdle()
        
        assertEquals(filter, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `updateSort should update sort in state`() = runTest {
        val sort = ToolSort.NAME_DESC
        
        viewModel.updateSort(sort)
        advanceUntilIdle()
        
        assertEquals(sort, viewModel.uiState.value.selectedSort)
    }

    @Test
    fun `refreshTools should set isRefreshing to true then false`() = runTest {
        viewModel.refreshTools()
        
        // Should be refreshing initially
        assertTrue(viewModel.uiState.value.isRefreshing)
        
        advanceUntilIdle()
        
        // Should not be refreshing after completion
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Simulate error state
        viewModel.updateSearchQuery("test")
        advanceUntilIdle()
        
        viewModel.clearError()
        
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `getToolCategories should return expected categories`() {
        val categories = viewModel.getToolCategories()
        val expectedCategories = listOf("CL415", "RJ85", "Q400", "Engine", "CNC", "Sheetmetal", "General")
        
        assertEquals(expectedCategories, categories)
    }

    @Test
    fun `getToolStatuses should return expected statuses`() {
        val statuses = viewModel.getToolStatuses()
        val expectedStatuses = listOf("Available", "Checked Out", "Maintenance", "Retired")
        
        assertEquals(expectedStatuses, statuses)
    }

    private fun getSampleTools(): List<Tool> {
        return listOf(
            Tool(
                id = 1,
                toolNumber = "HT001",
                serialNumber = "SN123456",
                description = "Torque Wrench 1/2\"",
                category = "General",
                location = "Tool Crib A",
                status = "Available"
            ),
            Tool(
                id = 2,
                toolNumber = "HT002",
                serialNumber = "SN123457",
                description = "Drill Set Complete",
                category = "General",
                location = "Tool Crib B",
                status = "Checked Out"
            ),
            Tool(
                id = 3,
                toolNumber = "HT003",
                serialNumber = "SN123458",
                description = "Hydraulic Jack",
                category = "CL415",
                location = "Hangar 1",
                status = "Available"
            )
        )
    }
}
