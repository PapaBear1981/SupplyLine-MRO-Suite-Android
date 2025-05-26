package com.example.supplyline_mro_suite.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.supplyline_mro_suite.data.remote.NetworkConnectivityMonitor
import com.example.supplyline_mro_suite.data.remote.NetworkResult
import com.example.supplyline_mro_suite.data.repository.ToolRepository
import com.example.supplyline_mro_suite.data.repository.ChemicalRepository
import com.example.supplyline_mro_suite.data.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Background worker for syncing data with the server
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val toolRepository: ToolRepository,
    private val chemicalRepository: ChemicalRepository,
    private val userRepository: UserRepository,
    private val networkMonitor: NetworkConnectivityMonitor
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "sync_work"
        const val SYNC_TYPE_KEY = "sync_type"

        // Sync types
        const val SYNC_ALL = "all"
        const val SYNC_TOOLS = "tools"
        const val SYNC_CHEMICALS = "chemicals"
        const val SYNC_USERS = "users"
    }

    override suspend fun doWork(): Result {
        // Check if user is logged in
        if (!userRepository.isLoggedIn()) {
            return Result.failure()
        }

        // Check network connectivity
        if (!networkMonitor.isCurrentlyConnected()) {
            return Result.retry()
        }

        val syncType = inputData.getString(SYNC_TYPE_KEY) ?: SYNC_ALL

        return try {
            when (syncType) {
                SYNC_ALL -> syncAll()
                SYNC_TOOLS -> syncTools()
                SYNC_CHEMICALS -> syncChemicals()
                SYNC_USERS -> syncUsers()
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncAll(): Result {
        var hasFailures = false

        // Sync tools
        toolRepository.syncTools().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        // Sync tool checkouts
        toolRepository.syncCheckouts().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        // Sync chemicals
        chemicalRepository.syncChemicals().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        // Sync chemical issuances
        chemicalRepository.syncIssuances().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        // Sync users
        userRepository.syncUsers().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }

    private suspend fun syncTools(): Result {
        var hasFailures = false

        toolRepository.syncTools().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        toolRepository.syncCheckouts().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }

    private suspend fun syncChemicals(): Result {
        var hasFailures = false

        chemicalRepository.syncChemicals().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        chemicalRepository.syncIssuances().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }

    private suspend fun syncUsers(): Result {
        var hasFailures = false

        userRepository.syncUsers().collect { result ->
            if (result.isFailure) {
                hasFailures = true
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }
}
