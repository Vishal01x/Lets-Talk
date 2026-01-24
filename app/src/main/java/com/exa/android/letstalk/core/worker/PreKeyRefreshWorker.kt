package com.exa.android.letstalk.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.exa.android.letstalk.data.signal_protocol.DeviceInitializer
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker to refresh Signal Protocol prekeys.
 * Runs periodically to ensure device has enough prekeys available.
 */
@HiltWorker
class PreKeyRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deviceInitializer: DeviceInitializer,
    private val auth: FirebaseAuth
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "PreKeyRefreshWorker"
        const val WORK_NAME = "signal_prekey_refresh"
    }

    override suspend fun doWork(): Result {
        return try {
            val currentUserId = auth.currentUser?.uid
            
            if (currentUserId == null) {
                Log.w(TAG, "No user logged in, skipping prekey refresh")
                return Result.success()
            }

            Log.d(TAG, "Starting prekey refresh for user: $currentUserId")
            deviceInitializer.refreshPreKeysIfNeeded(currentUserId)
            Log.d(TAG, "Prekey refresh completed successfully")
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Prekey refresh failed", e)
            Result.retry()
        }
    }
}
