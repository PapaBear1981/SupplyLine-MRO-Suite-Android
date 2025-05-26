package com.example.supplyline_mro_suite.data.local.dao

import androidx.room.*
import com.example.supplyline_mro_suite.data.model.Chemical
import com.example.supplyline_mro_suite.data.model.ChemicalIssuance
import com.example.supplyline_mro_suite.data.model.ChemicalWithUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChemicalDao {

    @Query("SELECT * FROM chemicals WHERE id = :id")
    suspend fun getChemicalById(id: Int): Chemical?

    @Query("SELECT * FROM chemicals WHERE part_number = :partNumber AND lot_number = :lotNumber")
    suspend fun getChemicalByPartAndLot(partNumber: String, lotNumber: String): Chemical?

    @Query("SELECT * FROM chemicals WHERE is_archived = 0 ORDER BY part_number ASC")
    fun getAllActiveChemicals(): Flow<List<Chemical>>

    @Query("SELECT * FROM chemicals WHERE is_archived = 1 ORDER BY part_number ASC")
    fun getAllArchivedChemicals(): Flow<List<Chemical>>

    @Query("SELECT * FROM chemicals WHERE status = :status AND is_archived = 0 ORDER BY part_number ASC")
    fun getChemicalsByStatus(status: String): Flow<List<Chemical>>

    @Query("SELECT * FROM chemicals WHERE category = :category AND is_archived = 0 ORDER BY part_number ASC")
    fun getChemicalsByCategory(category: String): Flow<List<Chemical>>

    @Query("SELECT * FROM chemicals WHERE location LIKE '%' || :location || '%' AND is_archived = 0 ORDER BY part_number ASC")
    fun getChemicalsByLocation(location: String): Flow<List<Chemical>>

    @Query("""
        SELECT * FROM chemicals
        WHERE (part_number LIKE '%' || :query || '%'
        OR lot_number LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        OR manufacturer LIKE '%' || :query || '%')
        AND is_archived = 0
        ORDER BY part_number ASC
    """)
    fun searchChemicals(query: String): Flow<List<Chemical>>

    @Query("SELECT * FROM chemicals WHERE expiration_date <= date('now', '+30 days') AND is_archived = 0 ORDER BY expiration_date ASC")
    fun getChemicalsExpiringSoon(): Flow<List<Chemical>>

    @Query("SELECT * FROM chemicals WHERE expiration_date < date('now') AND is_archived = 0 ORDER BY expiration_date ASC")
    fun getExpiredChemicals(): Flow<List<Chemical>>

    @Query("SELECT * FROM chemicals WHERE quantity <= minimum_stock_level AND is_archived = 0 ORDER BY part_number ASC")
    fun getLowStockChemicals(): Flow<List<Chemical>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemical(chemical: Chemical)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemicals(chemicals: List<Chemical>)

    @Update
    suspend fun updateChemical(chemical: Chemical)

    @Delete
    suspend fun deleteChemical(chemical: Chemical)

    @Query("DELETE FROM chemicals")
    suspend fun deleteAllChemicals()

    @Query("SELECT COUNT(*) FROM chemicals WHERE is_archived = 0")
    suspend fun getActiveChemicalCount(): Int

    @Query("SELECT COUNT(*) FROM chemicals WHERE status = :status AND is_archived = 0")
    suspend fun getChemicalCountByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM chemicals WHERE category = :category AND is_archived = 0")
    suspend fun getChemicalCountByCategory(category: String): Int

    // Advanced queries with aggregations
    @Query("""
        SELECT c.*, COALESCE(SUM(ci.quantity_issued), 0) as total_issued,
               (c.quantity - COALESCE(SUM(ci.quantity_issued), 0)) as remaining_quantity
        FROM chemicals c
        LEFT JOIN chemical_issuances ci ON c.id = ci.chemical_id
        WHERE c.id = :chemicalId
        GROUP BY c.id
    """)
    suspend fun getChemicalWithUsage(chemicalId: Int): ChemicalWithUsage?

    @Query("""
        SELECT c.*, COALESCE(SUM(ci.quantity_issued), 0) as total_issued,
               (c.quantity - COALESCE(SUM(ci.quantity_issued), 0)) as remaining_quantity
        FROM chemicals c
        LEFT JOIN chemical_issuances ci ON c.id = ci.chemical_id
        WHERE c.is_archived = 0
        GROUP BY c.id
        HAVING remaining_quantity <= c.minimum_stock_level
        ORDER BY remaining_quantity ASC
    """)
    fun getCriticalStockChemicals(): Flow<List<ChemicalWithUsage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssuance(issuance: ChemicalIssuance)

    @Transaction
    suspend fun issueChemical(chemicalId: Int, quantityIssued: Double, userId: Int, location: String = "Unknown"): Boolean {
        if (quantityIssued <= 0) return false

        val chemical = getChemicalById(chemicalId) ?: return false
        if (chemical.quantity < quantityIssued) return false

        // 1) Insert issuance record
        insertIssuance(
            ChemicalIssuance(
                id = 0,
                chemicalId = chemicalId,
                userId = userId,
                quantityIssued = quantityIssued,
                issueDate = java.time.LocalDateTime.now().toString(),
                location = location,
                issuedBy = "System" // TODO: Get from current user context
            )
        )

        // 2) Update chemical quantity
        updateChemical(
            chemical.copy(
                quantity = chemical.quantity - quantityIssued,
                updatedAt = java.time.LocalDateTime.now().toString()
            )
        )
        return true
    }
}
