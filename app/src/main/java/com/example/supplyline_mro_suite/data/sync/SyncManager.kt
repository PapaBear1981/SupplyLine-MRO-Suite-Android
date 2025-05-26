package com.example.supplyline_mro_suite.data.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages background synchronization with the server
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule periodic sync for all data
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15, // 15 minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5, // 5 minutes flex
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(
                workDataOf(SyncWorker.SYNC_TYPE_KEY to SyncWorker.SYNC_ALL)
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Trigger immediate sync for all data
     */
    fun syncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(SyncWorker.SYNC_TYPE_KEY to SyncWorker.SYNC_ALL)
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_now",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Trigger immediate sync for specific data type
     */
    fun syncNow(syncType: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(SyncWorker.SYNC_TYPE_KEY to syncType)
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_now_$syncType",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Cancel all sync work
     */
    fun cancelSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        workManager.cancelUniqueWork("sync_now")
    }

    /**
     * Get sync work status
     */
    fun getSyncStatus(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow(SyncWorker.WORK_NAME)
    }

    /**
     * Check if sync is currently running
     */
    fun isSyncRunning(): Flow<Boolean> {
        return workManager.getWorkInfosForUniqueWorkFlow(SyncWorker.WORK_NAME)
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING }
            }
    }

    /**
     * Sync tools only
     */
    fun syncTools() {
        syncNow(SyncWorker.SYNC_TOOLS)
    }

    /**
     * Sync chemicals only
     */
    fun syncChemicals() {
        syncNow(SyncWorker.SYNC_CHEMICALS)
    }

    /**
     * Sync users only
     */
    fun syncUsers() {
        syncNow(SyncWorker.SYNC_USERS)
    }
}
