package com.example.supplyline_mro_suite.data.remote

import com.example.supplyline_mro_suite.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    // Users
    @GET("users")
    suspend fun getUsers(): Response<List<User>>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<User>
    
    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>
    
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: User): Response<User>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>
    
    // Tools
    @GET("tools")
    suspend fun getTools(): Response<List<Tool>>
    
    @GET("tools/{id}")
    suspend fun getTool(@Path("id") id: Int): Response<Tool>
    
    @POST("tools")
    suspend fun createTool(@Body tool: Tool): Response<Tool>
    
    @PUT("tools/{id}")
    suspend fun updateTool(@Path("id") id: Int, @Body tool: Tool): Response<Tool>
    
    @DELETE("tools/{id}")
    suspend fun deleteTool(@Path("id") id: Int): Response<Unit>
    
    @GET("tools/search")
    suspend fun searchTools(@Query("q") query: String): Response<List<Tool>>
    
    @GET("tools/category/{category}")
    suspend fun getToolsByCategory(@Path("category") category: String): Response<List<Tool>>
    
    @GET("tools/status/{status}")
    suspend fun getToolsByStatus(@Path("status") status: String): Response<List<Tool>>
    
    // Tool Checkouts
    @GET("checkouts")
    suspend fun getCheckouts(): Response<List<ToolCheckout>>
    
    @GET("checkouts/active")
    suspend fun getActiveCheckouts(): Response<List<ToolCheckout>>
    
    @GET("checkouts/user/{userId}")
    suspend fun getCheckoutsForUser(@Path("userId") userId: Int): Response<List<ToolCheckout>>
    
    @GET("checkouts/tool/{toolId}")
    suspend fun getCheckoutsForTool(@Path("toolId") toolId: Int): Response<List<ToolCheckout>>
    
    @POST("checkouts")
    suspend fun createCheckout(@Body request: CheckoutRequest): Response<ToolCheckout>
    
    @PUT("checkouts/{id}/return")
    suspend fun returnTool(@Path("id") id: Int, @Body request: ReturnRequest): Response<ToolCheckout>
    
    @GET("checkouts/overdue")
    suspend fun getOverdueCheckouts(): Response<List<ToolCheckout>>
    
    // Chemicals
    @GET("chemicals")
    suspend fun getChemicals(): Response<List<Chemical>>
    
    @GET("chemicals/{id}")
    suspend fun getChemical(@Path("id") id: Int): Response<Chemical>
    
    @POST("chemicals")
    suspend fun createChemical(@Body chemical: Chemical): Response<Chemical>
    
    @PUT("chemicals/{id}")
    suspend fun updateChemical(@Path("id") id: Int, @Body chemical: Chemical): Response<Chemical>
    
    @DELETE("chemicals/{id}")
    suspend fun deleteChemical(@Path("id") id: Int): Response<Unit>
    
    @GET("chemicals/search")
    suspend fun searchChemicals(@Query("q") query: String): Response<List<Chemical>>
    
    @GET("chemicals/category/{category}")
    suspend fun getChemicalsByCategory(@Path("category") category: String): Response<List<Chemical>>
    
    @GET("chemicals/status/{status}")
    suspend fun getChemicalsByStatus(@Path("status") status: String): Response<List<Chemical>>
    
    @GET("chemicals/expiring")
    suspend fun getExpiringChemicals(): Response<List<Chemical>>
    
    @GET("chemicals/low-stock")
    suspend fun getLowStockChemicals(): Response<List<Chemical>>
    
    // Chemical Issuances
    @GET("issuances")
    suspend fun getIssuances(): Response<List<ChemicalIssuance>>
    
    @GET("issuances/chemical/{chemicalId}")
    suspend fun getIssuancesForChemical(@Path("chemicalId") chemicalId: Int): Response<List<ChemicalIssuance>>
    
    @GET("issuances/user/{userId}")
    suspend fun getIssuancesForUser(@Path("userId") userId: Int): Response<List<ChemicalIssuance>>
    
    @POST("issuances")
    suspend fun createIssuance(@Body request: IssueRequest): Response<ChemicalIssuance>
    
    // Dashboard/Analytics
    @GET("dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStats>
    
    @GET("analytics/tools")
    suspend fun getToolAnalytics(): Response<ToolAnalytics>
    
    @GET("analytics/chemicals")
    suspend fun getChemicalAnalytics(): Response<ChemicalAnalytics>
}

// Response models for analytics
data class DashboardStats(
    val totalUsers: Int,
    val activeUsers: Int,
    val totalTools: Int,
    val availableTools: Int,
    val checkedOutTools: Int,
    val overdueTools: Int,
    val totalChemicals: Int,
    val expiringChemicals: Int,
    val lowStockChemicals: Int,
    val recentActivity: List<ActivityItem>
)

data class ActivityItem(
    val id: Int,
    val type: String, // "checkout", "return", "issuance", etc.
    val description: String,
    val timestamp: String,
    val user: String
)

data class ToolAnalytics(
    val utilizationRate: Double,
    val categoryBreakdown: Map<String, Int>,
    val statusBreakdown: Map<String, Int>,
    val checkoutTrends: List<TrendData>
)

data class ChemicalAnalytics(
    val usageRate: Double,
    val categoryBreakdown: Map<String, Int>,
    val statusBreakdown: Map<String, Int>,
    val issuanceTrends: List<TrendData>
)

data class TrendData(
    val date: String,
    val value: Double
)
