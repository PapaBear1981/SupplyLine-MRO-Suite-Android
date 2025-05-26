package com.example.supplyline_mro_suite.data.local.dao

import androidx.room.*
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.ToolCheckout
import com.example.supplyline_mro_suite.data.model.ToolWithCheckoutInfo
import com.example.supplyline_mro_suite.data.model.ToolUsageStats
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

    // Advanced queries with joins
    @Query("""
        SELECT t.*, tc.checkout_date, tc.expected_return_date, u.name as checked_out_to_name
        FROM tools t
        LEFT JOIN tool_checkouts tc ON t.id = tc.tool_id AND tc.is_active = 1
        LEFT JOIN users u ON tc.user_id = u.id
        WHERE t.id = :toolId
    """)
    suspend fun getToolWithCheckoutInfo(toolId: Int): ToolWithCheckoutInfo?

    @Query("""
        SELECT t.*, COUNT(tc.id) as checkout_count
        FROM tools t
        LEFT JOIN tool_checkouts tc ON t.id = tc.tool_id
        GROUP BY t.id
        ORDER BY checkout_count DESC
        LIMIT :limit
    """)
    fun getMostUsedTools(limit: Int = 10): Flow<List<ToolUsageStats>>

    @Query("""
        SELECT t.*
        FROM tools t
        INNER JOIN tool_checkouts tc ON t.id = tc.tool_id
        WHERE tc.is_active = 1 AND tc.expected_return_date < date('now')
        ORDER BY tc.expected_return_date ASC
    """)
    fun getOverdueTools(): Flow<List<Tool>>

    // Tool status management
    @Query("UPDATE tools SET status = :status WHERE id = :toolId")
    suspend fun updateToolStatus(toolId: Int, status: String)
}
