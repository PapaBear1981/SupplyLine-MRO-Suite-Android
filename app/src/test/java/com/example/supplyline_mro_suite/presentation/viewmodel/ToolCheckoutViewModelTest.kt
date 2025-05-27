package com.example.supplyline_mro_suite.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.supplyline_mro_suite.data.repository.ToolRepository
import com.example.supplyline_mro_suite.data.repository.UserRepository
import com.example.supplyline_mro_suite.data.model.*
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ToolCheckoutViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val toolRepository = mockk<ToolRepository>()
    private val userRepository = mockk<UserRepository>()
    private lateinit var viewModel: ToolCheckoutViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock repository methods
        coEvery { toolRepository.getToolById(any()) } returns getSampleTool()
        coEvery { toolRepository.getActiveCheckoutForTool(any()) } returns null
        coEvery { toolRepository.getUserById(any()) } returns null
        coEvery { toolRepository.getToolByNumber(any()) } returns getSampleTool()
        coEvery { toolRepository.getToolBySerialNumber(any()) } returns getSampleTool()
        coEvery { userRepository.getCurrentUser() } returns flowOf(getSampleUser())
        coEvery { toolRepository.checkoutTool(any()) } returns flowOf(Result.success(getSampleCheckout()))
        coEvery { toolRepository.returnTool(any()) } returns flowOf(Result.success(getSampleCheckout()))
        
        viewModel = ToolCheckoutViewModel(toolRepository, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        val initialState = viewModel.uiState.value
        assertFalse(initialState.isLoading)
        assertNull(initialState.tool)
        assertNull(initialState.activeCheckout)
        assertNull(initialState.error)
        assertEquals("", initialState.expectedReturnDate)
    }

    @Test
    fun `loadToolForCheckout should load tool successfully`() = runTest {
        val toolId = 1
        
        viewModel.loadToolForCheckout(toolId)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.tool)
        assertEquals("HT001", state.tool?.toolNumber)
        assertNull(state.error)
        // Should set default return date to 7 days from now
        assertEquals(LocalDate.now().plusDays(7).toString(), state.expectedReturnDate)
    }

    @Test
    fun `loadToolByQRCode should find tool by tool number`() = runTest {
        val qrCode = "HT001"
        
        viewModel.loadToolByQRCode(qrCode)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.tool)
        assertEquals("HT001", state.tool?.toolNumber)
        assertNull(state.error)
    }

    @Test
    fun `updateExpectedReturnDate should update state`() {
        val date = "2024-12-31"
        
        viewModel.updateExpectedReturnDate(date)
        
        assertEquals(date, viewModel.uiState.value.expectedReturnDate)
    }

    @Test
    fun `updateCheckoutNotes should update state`() {
        val notes = "Test notes"
        
        viewModel.updateCheckoutNotes(notes)
        
        assertEquals(notes, viewModel.uiState.value.checkoutNotes)
    }

    @Test
    fun `updateReturnCondition should update state`() {
        val condition = "Good"
        
        viewModel.updateReturnCondition(condition)
        
        assertEquals(condition, viewModel.uiState.value.returnCondition)
    }

    @Test
    fun `updateReturnNotes should update state`() {
        val notes = "Return notes"
        
        viewModel.updateReturnNotes(notes)
        
        assertEquals(notes, viewModel.uiState.value.returnNotes)
    }

    @Test
    fun `getAvailableConditions should return expected conditions`() {
        val conditions = viewModel.getAvailableConditions()
        val expectedConditions = listOf("Good", "Fair", "Poor", "Damaged", "Needs Maintenance")
        
        assertEquals(expectedConditions, conditions)
    }

    @Test
    fun `isValidReturnDate should validate dates correctly`() {
        val today = LocalDate.now().toString()
        val tomorrow = LocalDate.now().plusDays(1).toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        
        assertTrue(viewModel.isValidReturnDate(today))
        assertTrue(viewModel.isValidReturnDate(tomorrow))
        assertFalse(viewModel.isValidReturnDate(yesterday))
        assertFalse(viewModel.isValidReturnDate("invalid-date"))
    }

    @Test
    fun `clearError should clear error state`() {
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resetState should reset to initial state`() {
        // Modify state first
        viewModel.updateCheckoutNotes("test")
        viewModel.updateExpectedReturnDate("2024-12-31")
        
        // Reset state
        viewModel.resetState()
        
        val state = viewModel.uiState.value
        assertEquals("", state.checkoutNotes)
        assertEquals("", state.expectedReturnDate)
        assertNull(state.tool)
        assertNull(state.activeCheckout)
    }

    private fun getSampleTool(): Tool {
        return Tool(
            id = 1,
            toolNumber = "HT001",
            serialNumber = "SN123456",
            description = "Torque Wrench 1/2\"",
            category = "General",
            location = "Tool Crib A",
            status = "Available"
        )
    }

    private fun getSampleUser(): User {
        return User(
            id = 1,
            employeeNumber = "EMP001",
            name = "John Doe",
            department = "Maintenance"
        )
    }

    private fun getSampleCheckout(): ToolCheckout {
        return ToolCheckout(
            id = 1,
            toolId = 1,
            userId = 1,
            checkoutDate = LocalDate.now().toString(),
            expectedReturnDate = LocalDate.now().plusDays(7).toString(),
            checkedOutBy = "EMP001"
        )
    }
}
