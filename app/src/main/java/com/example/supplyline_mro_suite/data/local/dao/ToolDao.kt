package com.example.supplyline_mro_suite.data.local.dao

import androidx.room.*
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolCheckout
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {
    
    @Query("SELECT * FROM tools WHERE id = :id")
    suspend fun getToolById(id: Int): Tool?
    
    @Query("SELECT * FROM tools WHERE tool_number = :toolNumber")
    suspend fun getToolByNumber(toolNumber: String): Tool?
    
    @Query("SELECT * FROM tools WHERE serial_number = :serialNumber")
    suspend fun getToolBySerialNumber(serialNumber: String): Tool?
    
    @Query("SELECT * FROM tools ORDER BY tool_number ASC")
    fun getAllTools(): Flow<List<Tool>>
    
    @Query("SELECT * FROM tools WHERE status = :status ORDER BY tool_number ASC")
    fun getToolsByStatus(status: String): Flow<List<Tool>>
    
    @Query("SELECT * FROM tools WHERE category = :category ORDER BY tool_number ASC")
    fun getToolsByCategory(category: String): Flow<List<Tool>>
    
    @Query("SELECT * FROM tools WHERE location LIKE '%' || :location || '%' ORDER BY tool_number ASC")
    fun getToolsByLocation(location: String): Flow<List<Tool>>
    
    @Query("""
        SELECT * FROM tools 
        WHERE tool_number LIKE '%' || :query || '%' 
        OR serial_number LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY tool_number ASC
    """)
    fun searchTools(query: String): Flow<List<Tool>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: Tool)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTools(tools: List<Tool>)
    
    @Update
    suspend fun updateTool(tool: Tool)
    
    @Delete
    suspend fun deleteTool(tool: Tool)
    
    @Query("DELETE FROM tools")
    suspend fun deleteAllTools()
    
    @Query("SELECT COUNT(*) FROM tools")
    suspend fun getToolCount(): Int
    
    @Query("SELECT COUNT(*) FROM tools WHERE status = :status")
    suspend fun getToolCountByStatus(status: String): Int
    
    @Query("SELECT COUNT(*) FROM tools WHERE category = :category")
    suspend fun getToolCountByCategory(category: String): Int
    
    // Calibration queries
    @Query("SELECT * FROM tools WHERE requires_calibration = 1 AND calibration_due_date <= date('now', '+30 days') ORDER BY calibration_due_date ASC")
    fun getToolsDueSoonForCalibration(): Flow<List<Tool>>
    
    @Query("SELECT * FROM tools WHERE requires_calibration = 1 AND calibration_due_date < date('now') ORDER BY calibration_due_date ASC")
    fun getOverdueCalibrationTools(): Flow<List<Tool>>
}
