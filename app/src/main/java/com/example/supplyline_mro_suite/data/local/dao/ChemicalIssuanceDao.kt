package com.example.supplyline_mro_suite.data.local.dao

import androidx.room.*
import com.example.supplyline_mro_suite.data.model.ChemicalIssuance
import kotlinx.coroutines.flow.Flow

@Dao
interface ChemicalIssuanceDao {
    
    @Query("SELECT * FROM chemical_issuances WHERE id = :id")
    suspend fun getIssuanceById(id: Int): ChemicalIssuance?
    
    @Query("SELECT * FROM chemical_issuances WHERE chemical_id = :chemicalId ORDER BY issue_date DESC")
    fun getIssuancesForChemical(chemicalId: Int): Flow<List<ChemicalIssuance>>
    
    @Query("SELECT * FROM chemical_issuances WHERE user_id = :userId ORDER BY issue_date DESC")
    fun getIssuancesForUser(userId: Int): Flow<List<ChemicalIssuance>>
    
    @Query("SELECT * FROM chemical_issuances ORDER BY issue_date DESC")
    fun getAllIssuances(): Flow<List<ChemicalIssuance>>
    
    @Query("SELECT * FROM chemical_issuances WHERE location LIKE '%' || :location || '%' ORDER BY issue_date DESC")
    fun getIssuancesByLocation(location: String): Flow<List<ChemicalIssuance>>
    
    @Query("SELECT * FROM chemical_issuances WHERE issue_date >= :startDate AND issue_date <= :endDate ORDER BY issue_date DESC")
    fun getIssuancesByDateRange(startDate: String, endDate: String): Flow<List<ChemicalIssuance>>
    
    @Query("SELECT SUM(quantity_issued) FROM chemical_issuances WHERE chemical_id = :chemicalId")
    suspend fun getTotalIssuedForChemical(chemicalId: Int): Double?
    
    @Query("""
        SELECT SUM(quantity_issued) FROM chemical_issuances 
        WHERE chemical_id = :chemicalId 
        AND issue_date >= :startDate 
        AND issue_date <= :endDate
    """)
    suspend fun getTotalIssuedForChemicalInPeriod(chemicalId: Int, startDate: String, endDate: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssuance(issuance: ChemicalIssuance)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssuances(issuances: List<ChemicalIssuance>)
    
    @Update
    suspend fun updateIssuance(issuance: ChemicalIssuance)
    
    @Delete
    suspend fun deleteIssuance(issuance: ChemicalIssuance)
    
    @Query("DELETE FROM chemical_issuances")
    suspend fun deleteAllIssuances()
    
    @Query("SELECT COUNT(*) FROM chemical_issuances")
    suspend fun getIssuanceCount(): Int
    
    @Query("SELECT COUNT(*) FROM chemical_issuances WHERE issue_date >= :startDate AND issue_date <= :endDate")
    suspend fun getIssuanceCountInPeriod(startDate: String, endDate: String): Int
}
