package com.example.supplyline_mro_suite.data.remote

import com.example.supplyline_mro_suite.data.model.*
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `login with valid credentials returns success`() = runTest {
        // Given
        val loginResponse = """
            {
                "success": true,
                "message": "Login successful",
                "user": {
                    "id": 1,
                    "employee_number": "EMP001",
                    "first_name": "John",
                    "last_name": "Doe",
                    "email": "john.doe@example.com",
                    "department": "Maintenance",
                    "role": "Technician",
                    "is_active": true
                },
                "token": "mock_jwt_token"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(loginResponse)
                .addHeader("Content-Type", "application/json")
        )

        val loginRequest = LoginRequest("EMP001", "password123")

        // When
        val response = apiService.login(loginRequest)

        // Then
        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertTrue(body.success)
        assertEquals("Login successful", body.message)
        assertEquals("EMP001", body.user?.employeeNumber)
        assertEquals("mock_jwt_token", body.token)
    }

    @Test
    fun `login with invalid credentials returns error`() = runTest {
        // Given
        val errorResponse = """
            {
                "success": false,
                "message": "Invalid credentials",
                "user": null,
                "token": null
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json")
        )

        val loginRequest = LoginRequest("EMP001", "wrongpassword")

        // When
        val response = apiService.login(loginRequest)

        // Then
        assertEquals(401, response.code())
    }

    @Test
    fun `getTools returns list of tools`() = runTest {
        // Given
        val toolsResponse = """
            [
                {
                    "id": 1,
                    "tool_number": "T001",
                    "name": "Wrench Set",
                    "description": "Standard wrench set",
                    "category": "Hand Tools",
                    "location": "A1-B2",
                    "status": "Available",
                    "condition": "Good",
                    "purchase_date": "2023-01-15",
                    "last_calibration": "2023-06-01",
                    "next_calibration": "2024-06-01",
                    "is_active": true
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(toolsResponse)
                .addHeader("Content-Type", "application/json")
        )

        // When
        val response = apiService.getTools()

        // Then
        assertTrue(response.isSuccessful)
        val tools = response.body()!!
        assertEquals(1, tools.size)
        assertEquals("T001", tools[0].toolNumber)
        assertEquals("Wrench Set", tools[0].name)
    }

    @Test
    fun `createTool returns created tool`() = runTest {
        // Given
        val newTool = Tool(
            id = 0,
            toolNumber = "T002",
            name = "Screwdriver Set",
            description = "Phillips and flathead screwdrivers",
            category = "Hand Tools",
            location = "A1-B3",
            status = "Available",
            condition = "New"
        )

        val createdToolResponse = """
            {
                "id": 2,
                "tool_number": "T002",
                "name": "Screwdriver Set",
                "description": "Phillips and flathead screwdrivers",
                "category": "Hand Tools",
                "location": "A1-B3",
                "status": "Available",
                "condition": "New",
                "purchase_date": "2023-12-01",
                "last_calibration": null,
                "next_calibration": null,
                "is_active": true
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(createdToolResponse)
                .addHeader("Content-Type", "application/json")
        )

        // When
        val response = apiService.createTool(newTool)

        // Then
        assertTrue(response.isSuccessful)
        val createdTool = response.body()!!
        assertEquals(2, createdTool.id)
        assertEquals("T002", createdTool.toolNumber)
        assertEquals("Screwdriver Set", createdTool.name)
    }

    @Test
    fun `getDashboardStats returns statistics`() = runTest {
        // Given
        val statsResponse = """
            {
                "totalUsers": 25,
                "activeUsers": 23,
                "totalTools": 150,
                "availableTools": 120,
                "checkedOutTools": 25,
                "overdueTools": 5,
                "totalChemicals": 75,
                "expiringChemicals": 3,
                "lowStockChemicals": 8,
                "recentActivity": [
                    {
                        "id": 1,
                        "type": "checkout",
                        "description": "Tool T001 checked out by John Doe",
                        "timestamp": "2023-12-01T10:30:00Z",
                        "user": "John Doe"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(statsResponse)
                .addHeader("Content-Type", "application/json")
        )

        // When
        val response = apiService.getDashboardStats()

        // Then
        assertTrue(response.isSuccessful)
        val stats = response.body()!!
        assertEquals(25, stats.totalUsers)
        assertEquals(150, stats.totalTools)
        assertEquals(1, stats.recentActivity.size)
        assertEquals("checkout", stats.recentActivity[0].type)
    }
}
