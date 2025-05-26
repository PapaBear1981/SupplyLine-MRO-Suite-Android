package com.example.supplyline_mro_suite.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supplyline_mro_suite.data.local.SupplyLineDatabase
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolCheckout
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
class ToolCheckoutDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: SupplyLineDatabase
    private lateinit var toolCheckoutDao: ToolCheckoutDao
    private lateinit var toolDao: ToolDao
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SupplyLineDatabase::class.java
        ).allowMainThreadQueries().build()
        
        toolCheckoutDao = database.toolCheckoutDao()
        toolDao = database.toolDao()
        userDao = database.userDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertCheckout_and_getCheckoutById() = runTest {
        // Given
        val user = User(1, "EMP001", "John Doe", "Maintenance")
        val tool = Tool(1, "T001", "SN001", "Test Tool", "General", "A1", "Available")
        val checkout = ToolCheckout(
            id = 1,
            toolId = 1,
            userId = 1,
            checkoutDate = "2024-01-01",
            expectedReturnDate = "2024-01-07",
            checkedOutBy = "John Doe",
            isActive = true
        )

        // When
        userDao.insertUser(user)
        toolDao.insertTool(tool)
        toolCheckoutDao.insertCheckout(checkout)
        val retrievedCheckout = toolCheckoutDao.getCheckoutById(1)

        // Then
        assertNotNull(retrievedCheckout)
        assertEquals(checkout.toolId, retrievedCheckout?.toolId)
        assertEquals(checkout.userId, retrievedCheckout?.userId)
        assertEquals(checkout.checkoutDate, retrievedCheckout?.checkoutDate)
    }

    @Test
    fun getActiveCheckoutForTool() = runTest {
        // Given
        val user = User(1, "EMP001", "John Doe", "Maintenance")
        val tool = Tool(1, "T001", "SN001", "Test Tool", "General", "A1", "Available")
        val activeCheckout = ToolCheckout(
            id = 1,
            toolId = 1,
            userId = 1,
            checkoutDate = "2024-01-01",
            expectedReturnDate = "2024-01-07",
            checkedOutBy = "John Doe",
            isActive = true
        )
        val inactiveCheckout = ToolCheckout(
            id = 2,
            toolId = 1,
            userId = 1,
            checkoutDate = "2023-12-01",
            expectedReturnDate = "2023-12-07",
            actualReturnDate = "2023-12-06",
            checkedOutBy = "John Doe",
            isActive = false
        )

        // When
        userDao.insertUser(user)
        toolDao.insertTool(tool)
        toolCheckoutDao.insertCheckout(activeCheckout)
        toolCheckoutDao.insertCheckout(inactiveCheckout)
        val retrievedCheckout = toolCheckoutDao.getActiveCheckoutForTool(1)

        // Then
        assertNotNull(retrievedCheckout)
        assertEquals(activeCheckout.id, retrievedCheckout?.id)
        assertTrue(retrievedCheckout?.isActive == true)
    }

    @Test
    fun getActiveCheckoutsForUser() = runTest {
        // Given
        val user = User(1, "EMP001", "John Doe", "Maintenance")
        val tool1 = Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available")
        val tool2 = Tool(2, "T002", "SN002", "Tool 2", "General", "A2", "Available")
        
        val checkouts = listOf(
            ToolCheckout(1, 1, 1, "2024-01-01", "2024-01-07", checkedOutBy = "John Doe", isActive = true),
            ToolCheckout(2, 2, 1, "2024-01-02", "2024-01-08", checkedOutBy = "John Doe", isActive = true),
            ToolCheckout(3, 1, 1, "2023-12-01", "2023-12-07", actualReturnDate = "2023-12-06", 
                        checkedOutBy = "John Doe", isActive = false)
        )

        // When
        userDao.insertUser(user)
        toolDao.insertTool(tool1)
        toolDao.insertTool(tool2)
        toolCheckoutDao.insertCheckouts(checkouts)
        val activeCheckouts = toolCheckoutDao.getActiveCheckoutsForUser(1).first()

        // Then
        assertEquals(2, activeCheckouts.size)
        assertTrue(activeCheckouts.all { it.isActive })
        assertTrue(activeCheckouts.all { it.userId == 1 })
    }

    @Test
    fun getAllActiveCheckouts() = runTest {
        // Given
        val user1 = User(1, "EMP001", "John Doe", "Maintenance")
        val user2 = User(2, "EMP002", "Jane Smith", "Materials")
        val tool1 = Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available")
        val tool2 = Tool(2, "T002", "SN002", "Tool 2", "General", "A2", "Available")
        
        val checkouts = listOf(
            ToolCheckout(1, 1, 1, "2024-01-01", "2024-01-07", checkedOutBy = "John Doe", isActive = true),
            ToolCheckout(2, 2, 2, "2024-01-02", "2024-01-08", checkedOutBy = "Jane Smith", isActive = true),
            ToolCheckout(3, 1, 1, "2023-12-01", "2023-12-07", actualReturnDate = "2023-12-06", 
                        checkedOutBy = "John Doe", isActive = false)
        )

        // When
        userDao.insertUser(user1)
        userDao.insertUser(user2)
        toolDao.insertTool(tool1)
        toolDao.insertTool(tool2)
        toolCheckoutDao.insertCheckouts(checkouts)
        val activeCheckouts = toolCheckoutDao.getAllActiveCheckouts().first()

        // Then
        assertEquals(2, activeCheckouts.size)
        assertTrue(activeCheckouts.all { it.isActive })
    }

    @Test
    fun checkoutTool_transaction() = runTest {
        // Given
        val user = User(1, "EMP001", "John Doe", "Maintenance")
        val tool = Tool(1, "T001", "SN001", "Test Tool", "General", "A1", "Available")
        val checkout = ToolCheckout(
            id = 1,
            toolId = 1,
            userId = 1,
            checkoutDate = "2024-01-01",
            expectedReturnDate = "2024-01-07",
            checkedOutBy = "John Doe",
            isActive = true
        )

        // When
        userDao.insertUser(user)
        toolDao.insertTool(tool)
        toolCheckoutDao.checkoutTool(checkout, 1)
        
        val retrievedCheckout = toolCheckoutDao.getCheckoutById(1)
        val retrievedTool = toolDao.getToolById(1)

        // Then
        assertNotNull(retrievedCheckout)
        assertNotNull(retrievedTool)
        assertEquals("Checked Out", retrievedTool?.status)
        assertTrue(retrievedCheckout?.isActive == true)
    }

    @Test
    fun returnTool_transaction() = runTest {
        // Given
        val user = User(1, "EMP001", "John Doe", "Maintenance")
        val tool = Tool(1, "T001", "SN001", "Test Tool", "General", "A1", "Checked Out")
        val checkout = ToolCheckout(
            id = 1,
            toolId = 1,
            userId = 1,
            checkoutDate = "2024-01-01",
            expectedReturnDate = "2024-01-07",
            checkedOutBy = "John Doe",
            isActive = true
        )

        // When
        userDao.insertUser(user)
        toolDao.insertTool(tool)
        toolCheckoutDao.insertCheckout(checkout)
        toolCheckoutDao.returnTool(1, 1, "Good", "Returned in good condition")
        
        val retrievedCheckout = toolCheckoutDao.getCheckoutById(1)
        val retrievedTool = toolDao.getToolById(1)

        // Then
        assertNotNull(retrievedCheckout)
        assertNotNull(retrievedTool)
        assertEquals("Available", retrievedTool?.status)
        assertFalse(retrievedCheckout?.isActive == true)
        assertEquals("Good", retrievedCheckout?.returnCondition)
        assertNotNull(retrievedCheckout?.actualReturnDate)
    }

    @Test
    fun getActiveCheckoutCount() = runTest {
        // Given
        val user = User(1, "EMP001", "John Doe", "Maintenance")
        val tool1 = Tool(1, "T001", "SN001", "Tool 1", "General", "A1", "Available")
        val tool2 = Tool(2, "T002", "SN002", "Tool 2", "General", "A2", "Available")
        
        val checkouts = listOf(
            ToolCheckout(1, 1, 1, "2024-01-01", "2024-01-07", checkedOutBy = "John Doe", isActive = true),
            ToolCheckout(2, 2, 1, "2024-01-02", "2024-01-08", checkedOutBy = "John Doe", isActive = true),
            ToolCheckout(3, 1, 1, "2023-12-01", "2023-12-07", actualReturnDate = "2023-12-06", 
                        checkedOutBy = "John Doe", isActive = false)
        )

        // When
        userDao.insertUser(user)
        toolDao.insertTool(tool1)
        toolDao.insertTool(tool2)
        toolCheckoutDao.insertCheckouts(checkouts)
        val activeCount = toolCheckoutDao.getActiveCheckoutCount()

        // Then
        assertEquals(2, activeCount)
    }
}
