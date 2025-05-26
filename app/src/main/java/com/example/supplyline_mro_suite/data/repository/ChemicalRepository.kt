package com.example.supplyline_mro_suite.data.repository

import com.example.supplyline_mro_suite.data.local.dao.ChemicalDao
import com.example.supplyline_mro_suite.data.local.dao.ChemicalIssuanceDao
import com.example.supplyline_mro_suite.data.model.Chemical
import com.example.supplyline_mro_suite.data.model.ChemicalIssuance
import com.example.supplyline_mro_suite.data.model.IssueRequest
import com.example.supplyline_mro_suite.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChemicalRepository @Inject constructor(
    private val chemicalDao: ChemicalDao,
    private val chemicalIssuanceDao: ChemicalIssuanceDao,
    private val apiService: ApiService
) {
    
    // Chemicals
    fun getAllActiveChemicals(): Flow<List<Chemical>> = chemicalDao.getAllActiveChemicals()
    
    fun getChemicalsByStatus(status: String): Flow<List<Chemical>> = chemicalDao.getChemicalsByStatus(status)
    
    fun getChemicalsByCategory(category: String): Flow<List<Chemical>> = chemicalDao.getChemicalsByCategory(category)
    
    fun searchChemicals(query: String): Flow<List<Chemical>> = chemicalDao.searchChemicals(query)
    
    fun getChemicalsExpiringSoon(): Flow<List<Chemical>> = chemicalDao.getChemicalsExpiringSoon()
    
    fun getExpiredChemicals(): Flow<List<Chemical>> = chemicalDao.getExpiredChemicals()
    
    fun getLowStockChemicals(): Flow<List<Chemical>> = chemicalDao.getLowStockChemicals()
    
    suspend fun getChemicalById(id: Int): Chemical? = chemicalDao.getChemicalById(id)
    
    suspend fun syncChemicals(): Flow<Result<List<Chemical>>> = flow {
        try {
            val response = apiService.getChemicals()
            if (response.isSuccessful) {
                val chemicals = response.body() ?: emptyList()
                chemicalDao.deleteAllChemicals()
                chemicalDao.insertChemicals(chemicals)
                emit(Result.success(chemicals))
            } else {
                emit(Result.failure(Exception("Failed to sync chemicals: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun createChemical(chemical: Chemical): Flow<Result<Chemical>> = flow {
        try {
            val response = apiService.createChemical(chemical)
            if (response.isSuccessful) {
                val createdChemical = response.body()!!
                chemicalDao.insertChemical(createdChemical)
                emit(Result.success(createdChemical))
            } else {
                val localChemical = chemical.copy(id = 0)
                chemicalDao.insertChemical(localChemical)
                emit(Result.success(localChemical))
            }
        } catch (e: Exception) {
            val localChemical = chemical.copy(id = 0)
            chemicalDao.insertChemical(localChemical)
            emit(Result.success(localChemical))
        }
    }
    
    suspend fun updateChemical(chemical: Chemical): Flow<Result<Chemical>> = flow {
        try {
            val response = apiService.updateChemical(chemical.id, chemical)
            if (response.isSuccessful) {
                val updatedChemical = response.body()!!
                chemicalDao.updateChemical(updatedChemical)
                emit(Result.success(updatedChemical))
            } else {
                chemicalDao.updateChemical(chemical)
                emit(Result.success(chemical))
            }
        } catch (e: Exception) {
            chemicalDao.updateChemical(chemical)
            emit(Result.success(chemical))
        }
    }
    
    // Chemical Issuances
    fun getAllIssuances(): Flow<List<ChemicalIssuance>> = chemicalIssuanceDao.getAllIssuances()
    
    fun getIssuancesForChemical(chemicalId: Int): Flow<List<ChemicalIssuance>> = 
        chemicalIssuanceDao.getIssuancesForChemical(chemicalId)
    
    fun getIssuancesForUser(userId: Int): Flow<List<ChemicalIssuance>> = 
        chemicalIssuanceDao.getIssuancesForUser(userId)
    
    suspend fun issueChemical(request: IssueRequest): Flow<Result<ChemicalIssuance>> = flow {
        try {
            val response = apiService.createIssuance(request)
            if (response.isSuccessful) {
                val issuance = response.body()!!
                chemicalIssuanceDao.insertIssuance(issuance)
                
                // Update chemical quantity
                val chemical = chemicalDao.getChemicalById(request.chemicalId)
                chemical?.let {
                    val updatedChemical = it.copy(
                        quantity = it.quantity - request.quantity
                    )
                    chemicalDao.updateChemical(updatedChemical)
                }
                
                emit(Result.success(issuance))
            } else {
                emit(Result.failure(Exception("Failed to issue chemical: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun syncIssuances(): Flow<Result<List<ChemicalIssuance>>> = flow {
        try {
            val response = apiService.getIssuances()
            if (response.isSuccessful) {
                val issuances = response.body() ?: emptyList()
                chemicalIssuanceDao.deleteAllIssuances()
                chemicalIssuanceDao.insertIssuances(issuances)
                emit(Result.success(issuances))
            } else {
                emit(Result.failure(Exception("Failed to sync issuances: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    // Statistics
    suspend fun getChemicalStats(): ChemicalStats {
        val totalChemicals = chemicalDao.getActiveChemicalCount()
        val goodChemicals = chemicalDao.getChemicalCountByStatus("Good")
        val expiringChemicals = chemicalDao.getChemicalCountByStatus("Expiring")
        val expiredChemicals = chemicalDao.getChemicalCountByStatus("Expired")
        val lowStockChemicals = chemicalDao.getChemicalCountByStatus("Low Stock")
        val totalIssuances = chemicalIssuanceDao.getIssuanceCount()
        
        return ChemicalStats(
            totalChemicals = totalChemicals,
            goodChemicals = goodChemicals,
            expiringChemicals = expiringChemicals,
            expiredChemicals = expiredChemicals,
            lowStockChemicals = lowStockChemicals,
            totalIssuances = totalIssuances
        )
    }
    
    suspend fun hasLocalData(): Boolean {
        return chemicalDao.getActiveChemicalCount() > 0
    }
}

data class ChemicalStats(
    val totalChemicals: Int,
    val goodChemicals: Int,
    val expiringChemicals: Int,
    val expiredChemicals: Int,
    val lowStockChemicals: Int,
    val totalIssuances: Int
)
