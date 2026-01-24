package com.exa.android.letstalk.core.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Centralized scheduler for Signal Protocol background workers.
 * Manages periodic tasks for key maintenance and rotation.
 */
object WorkerScheduler {
    
    private const val TAG = "WorkerScheduler"
    
    /**
     * Schedule all Signal Protocol workers.
     * Should be called after successful device initialization.
     * 
     * @param context Application context
     */
    fun scheduleSignalWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        // Constraints: requires network connection
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // 1. Schedule Pre-Key Refresh Worker (runs weekly)
        val preKeyRefreshWork = PeriodicWorkRequestBuilder<PreKeyRefreshWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            PreKeyRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already scheduled
            preKeyRefreshWork
        )
        
        Log.d(TAG, "✓ Scheduled PreKeyRefreshWorker (weekly)")
        
        // 2. Schedule Signed Pre-Key Rotation Worker (runs every 30 days)
        val signedPreKeyRotationWork = PeriodicWorkRequestBuilder<SignedPreKeyRotationWorker>(
            repeatInterval = 30,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SignedPreKeyRotationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            signedPreKeyRotationWork
        )
        
        Log.d(TAG, "✓ Scheduled SignedPreKeyRotationWorker (monthly)")
        
        Log.d(TAG, "🔧 All Signal Protocol workers scheduled successfully")
    }
    
    /**
     * Cancel all Signal Protocol workers.
     * Should be called on user logout.
     * 
     * @param context Application context
     */
    fun cancelSignalWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        workManager.cancelUniqueWork(PreKeyRefreshWorker.WORK_NAME)
        workManager.cancelUniqueWork(SignedPreKeyRotationWorker.WORK_NAME)
        
        Log.d(TAG, "✓ Cancelled all Signal Protocol workers")
    }
}
