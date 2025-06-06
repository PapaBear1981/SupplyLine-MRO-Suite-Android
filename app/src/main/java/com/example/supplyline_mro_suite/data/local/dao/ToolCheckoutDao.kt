package com.example.supplyline_mro_suite.data.local.dao

import androidx.room.*
import com.example.supplyline_mro_suite.data.model.ToolCheckout
import com.example.supplyline_mro_suite.data.model.Tool
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolCheckoutDao {

    @Query("SELECT * FROM tool_checkouts WHERE id = :id")
    suspend fun getCheckoutById(id: Int): ToolCheckout?

    @Query("SELECT * FROM tool_checkouts WHERE tool_id = :toolId AND is_active = 1")
    suspend fun getActiveCheckoutForTool(toolId: Int): ToolCheckout?

    @Query("SELECT * FROM tool_checkouts WHERE user_id = :userId AND is_active = 1 ORDER BY checkout_date DESC")
    fun getActiveCheckoutsForUser(userId: Int): Flow<List<ToolCheckout>>

    @Query("SELECT * FROM tool_checkouts WHERE is_active = 1 ORDER BY checkout_date DESC")
    fun getAllActiveCheckouts(): Flow<List<ToolCheckout>>

    @Query("SELECT * FROM tool_checkouts WHERE is_active = 0 ORDER BY actual_return_date DESC")
    fun getAllReturnedCheckouts(): Flow<List<ToolCheckout>>

    @Query("SELECT * FROM tool_checkouts WHERE tool_id = :toolId ORDER BY checkout_date DESC")
    fun getCheckoutHistoryForTool(toolId: Int): Flow<List<ToolCheckout>>

    @Query("SELECT * FROM tool_checkouts WHERE user_id = :userId ORDER BY checkout_date DESC")
    fun getCheckoutHistoryForUser(userId: Int): Flow<List<ToolCheckout>>

    @Query("""
        SELECT * FROM tool_checkouts
        WHERE is_active = 1
        AND expected_return_date < date('now')
        ORDER BY expected_return_date ASC
    """)
    fun getOverdueCheckouts(): Flow<List<ToolCheckout>>

    @Query("""
        SELECT * FROM tool_checkouts
        WHERE is_active = 1
        AND expected_return_date <= date('now', '+3 days')
        ORDER BY expected_return_date ASC
    """)
    fun getCheckoutsDueSoon(): Flow<List<ToolCheckout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckout(checkout: ToolCheckout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckouts(checkouts: List<ToolCheckout>)

    @Update
    suspend fun updateCheckout(checkout: ToolCheckout)

    @Delete
    suspend fun deleteCheckout(checkout: ToolCheckout)

    @Query("DELETE FROM tool_checkouts")
    suspend fun deleteAllCheckouts()

    @Query("SELECT COUNT(*) FROM tool_checkouts WHERE is_active = 1")
    suspend fun getActiveCheckoutCount(): Int

    @Query("SELECT COUNT(*) FROM tool_checkouts WHERE is_active = 1 AND expected_return_date < date('now')")
    suspend fun getOverdueCheckoutCount(): Int

    // Note: Transaction methods moved to repository layer to properly handle cross-DAO operations
}
