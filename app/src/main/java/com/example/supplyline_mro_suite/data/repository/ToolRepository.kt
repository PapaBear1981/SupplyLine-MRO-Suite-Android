package com.example.supplyline_mro_suite.data.repository

import com.example.supplyline_mro_suite.data.local.dao.ToolDao
import com.example.supplyline_mro_suite.data.local.dao.ToolCheckoutDao
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolCheckout
import com.example.supplyline_mro_suite.data.model.CheckoutRequest
import com.example.supplyline_mro_suite.data.model.ReturnRequest
import com.example.supplyline_mro_suite.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRepository @Inject constructor(
    private val toolDao: ToolDao,
    private val toolCheckoutDao: ToolCheckoutDao,
    private val apiService: ApiService
) {

    // Tools
    fun getAllTools(): Flow<List<Tool>> = toolDao.getAllTools()

    fun getToolsByStatus(status: String): Flow<List<Tool>> = toolDao.getToolsByStatus(status)

    fun getToolsByCategory(category: String): Flow<List<Tool>> = toolDao.getToolsByCategory(category)

    fun searchTools(query: String): Flow<List<Tool>> = toolDao.searchTools(query)

    suspend fun getToolById(id: Int): Tool? = toolDao.getToolById(id)

    suspend fun getToolByNumber(toolNumber: String): Tool? = toolDao.getToolByNumber(toolNumber)

    suspend fun getToolBySerialNumber(serialNumber: String): Tool? = toolDao.getToolBySerialNumber(serialNumber)

    suspend fun syncToolsWithResult(): Flow<Result<List<Tool>>> = flow {
        try {
            val response = apiService.getTools()
            if (response.isSuccessful) {
                val tools = response.body() ?: emptyList()
                toolDao.deleteAllTools()
                toolDao.insertTools(tools)
                emit(Result.success(tools))
            } else {
                emit(Result.failure(Exception("Failed to sync tools: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun createTool(tool: Tool): Flow<Result<Tool>> = flow {
        try {
            // Try to create on server first
            val response = apiService.createTool(tool)
            if (response.isSuccessful) {
                val createdTool = response.body()!!
                toolDao.insertTool(createdTool)
                emit(Result.success(createdTool))
            } else {
                // If offline, save locally with pending sync flag
                val localTool = tool.copy(id = 0) // Let Room generate ID
                toolDao.insertTool(localTool)
                emit(Result.success(localTool))
            }
        } catch (e: Exception) {
            // Offline mode - save locally
            val localTool = tool.copy(id = 0)
            toolDao.insertTool(localTool)
            emit(Result.success(localTool))
        }
    }

    suspend fun updateTool(tool: Tool): Flow<Result<Tool>> = flow {
        try {
            val response = apiService.updateTool(tool.id, tool)
            if (response.isSuccessful) {
                val updatedTool = response.body()!!
                toolDao.updateTool(updatedTool)
                emit(Result.success(updatedTool))
            } else {
                // Update locally if server fails
                toolDao.updateTool(tool)
                emit(Result.success(tool))
            }
        } catch (e: Exception) {
            // Offline mode
            toolDao.updateTool(tool)
            emit(Result.success(tool))
        }
    }

    // Tool Checkouts
    fun getAllActiveCheckouts(): Flow<List<ToolCheckout>> = toolCheckoutDao.getAllActiveCheckouts()

    fun getCheckoutsForUser(userId: Int): Flow<List<ToolCheckout>> =
        toolCheckoutDao.getActiveCheckoutsForUser(userId)

    fun getOverdueCheckouts(): Flow<List<ToolCheckout>> = toolCheckoutDao.getOverdueCheckouts()

    fun getCheckoutsDueSoon(): Flow<List<ToolCheckout>> = toolCheckoutDao.getCheckoutsDueSoon()

    suspend fun checkoutTool(request: CheckoutRequest): Flow<Result<ToolCheckout>> = flow {
        try {
            val response = apiService.createCheckout(request)
            if (response.isSuccessful) {
                val checkout = response.body()!!
                toolCheckoutDao.insertCheckout(checkout)

                // Update tool status
                val tool = toolDao.getToolById(request.toolId)
                tool?.let {
                    val updatedTool = it.copy(status = "Checked Out")
                    toolDao.updateTool(updatedTool)
                }

                emit(Result.success(checkout))
            } else {
                emit(Result.failure(Exception("Failed to checkout tool: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun returnTool(returnRequest: ReturnRequest): Flow<Result<ToolCheckout>> = flow {
        try {
            val response = apiService.returnTool(returnRequest.checkoutId, returnRequest)
            if (response.isSuccessful) {
                val returnedCheckout = response.body()!!
                toolCheckoutDao.updateCheckout(returnedCheckout)

                // Update tool status
                val checkout = toolCheckoutDao.getCheckoutById(returnRequest.checkoutId)
                checkout?.let {
                    val tool = toolDao.getToolById(it.toolId)
                    tool?.let { t ->
                        val updatedTool = t.copy(status = "Available")
                        toolDao.updateTool(updatedTool)
                    }
                }

                emit(Result.success(returnedCheckout))
            } else {
                emit(Result.failure(Exception("Failed to return tool: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun syncCheckouts(): Flow<Result<List<ToolCheckout>>> = flow {
        try {
            val response = apiService.getCheckouts()
            if (response.isSuccessful) {
                val checkouts = response.body() ?: emptyList()
                toolCheckoutDao.deleteAllCheckouts()
                toolCheckoutDao.insertCheckouts(checkouts)
                emit(Result.success(checkouts))
            } else {
                emit(Result.failure(Exception("Failed to sync checkouts: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Statistics
    suspend fun getToolStats(): ToolStats {
        val totalTools = toolDao.getToolCount()
        val availableTools = toolDao.getToolCountByStatus("Available")
        val checkedOutTools = toolDao.getToolCountByStatus("Checked Out")
        val maintenanceTools = toolDao.getToolCountByStatus("Maintenance")
        val overdueCheckouts = toolCheckoutDao.getOverdueCheckoutCount()

        return ToolStats(
            totalTools = totalTools,
            availableTools = availableTools,
            checkedOutTools = checkedOutTools,
            maintenanceTools = maintenanceTools,
            overdueCheckouts = overdueCheckouts
        )
    }

    // Calibration
    fun getToolsDueSoonForCalibration(): Flow<List<Tool>> = toolDao.getToolsDueSoonForCalibration()

    fun getOverdueCalibrationTools(): Flow<List<Tool>> = toolDao.getOverdueCalibrationTools()

    // Offline support
    suspend fun hasLocalData(): Boolean {
        return toolDao.getToolCount() > 0
    }

    suspend fun getPendingSyncItems(): List<Tool> {
        // In a real implementation, you would track items that need syncing
        return emptyList()
    }

    // Additional methods for ViewModels
    suspend fun getActiveCheckoutForTool(toolId: Int): ToolCheckout? {
        return toolCheckoutDao.getActiveCheckoutForTool(toolId)
    }

    suspend fun getCheckoutHistoryForTool(toolId: Int): List<ToolCheckout> {
        return toolCheckoutDao.getCheckoutHistoryForTool(toolId).first()
    }

    suspend fun getUserById(userId: Int): com.example.supplyline_mro_suite.data.model.User? {
        // This would typically come from UserRepository, but for now return null
        // In a real implementation, you'd inject UserRepository or have a shared DAO
        return null
    }

    // Simplified sync method for ViewModels
    suspend fun syncTools() {
        try {
            val response = apiService.getTools()
            if (response.isSuccessful) {
                val tools = response.body() ?: emptyList()
                toolDao.insertTools(tools)
            }
        } catch (e: Exception) {
            // Offline mode - no sync possible
        }
    }
}

data class ToolStats(
    val totalTools: Int,
    val availableTools: Int,
    val checkedOutTools: Int,
    val maintenanceTools: Int,
    val overdueCheckouts: Int
)
