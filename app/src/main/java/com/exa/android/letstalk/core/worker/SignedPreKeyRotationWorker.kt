package com.exa.android.letstalk.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.exa.android.letstalk.data.signal_protocol.SignalKeyManager
import com.exa.android.letstalk.data.repository.FirestoreKeyRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker to rotate Signal Protocol signed pre-keys.
 * Runs periodically (every 30 days) to maintain forward secrecy.
 * 
 * As per Signal Protocol specification, signed pre-keys should be rotated
 * regularly to minimize the impact of key compromise.
 */
@HiltWorker
class SignedPreKeyRotationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val signalKeyManager: SignalKeyManager,
    private val firestoreKeyRepo: FirestoreKeyRepository,
    private val auth: FirebaseAuth
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "SignedPreKeyRotationWorker"
        const val WORK_NAME = "signal_signed_prekey_rotation"
    }

    override suspend fun doWork(): Result {
        return try {
            val currentUserId = auth.currentUser?.uid
            
            if (currentUserId == null) {
                Log.w(TAG, "No user logged in, skipping signed pre-key rotation")
                return Result.success()
            }

            Log.d(TAG, "Starting signed pre-key rotation for user: $currentUserId")
            
            // Generate new signed pre-key
            signalKeyManager.generateSignedPreKey()
            Log.d(TAG, "✓ New signed pre-key generated")
            
            // Upload updated bundle to Firestore
            val deviceBundle = signalKeyManager.createDeviceKeyBundle()
            firestoreKeyRepo.publishDeviceKeys(currentUserId, deviceBundle)
            Log.d(TAG, "✓ Updated device bundle uploaded to Firestore")
            
            Log.d(TAG, "Signed pre-key rotation completed successfully")
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Signed pre-key rotation failed", e)
            Result.retry()
        }
    }
}
