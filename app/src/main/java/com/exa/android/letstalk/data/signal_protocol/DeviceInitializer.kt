package com.exa.android.letstalk.data.signal_protocol

import android.util.Log
import com.exa.android.letstalk.data.repository.FirestoreKeyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper service for initializing Signal Protocol keys on device.
 * Should be called once after user authentication.
 */
@Singleton
class DeviceInitializer @Inject constructor(
    private val signalKeyManager: SignalKeyManager,
    private val firestoreKeyRepo: FirestoreKeyRepository
) {
    private val TAG = "DeviceInitializer"
    private var isInitialized = false

    /**
     * Initialize device with Signal Protocol keys and upload to Firestore.
     * This should be called once when user logs in.
     * 
     * @param userId Current user ID
     * @return true if initialization successful
     */
    suspend fun initializeDeviceIfNeeded(userId: String): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) {
            Log.d(TAG, "Device already initialized")
            return@withContext true
        }

        try {
            Log.d(TAG, "Starting device initialization for user: $userId")
            
            // Step 1: Ensure identity keys exist
            val identityKeyPair = signalKeyManager.ensureIdentityKeys()
            Log.d(TAG, "✓ Identity keys ready")
            
            // Step 2: Generate signed prekey
            signalKeyManager.generateSignedPreKey()
            Log.d(TAG, "✓ Signed prekey generated")
            
            // Step 3: Generate one-time prekeys
            signalKeyManager.generateOneTimePreKeys()
            Log.d(TAG, "✓ One-time prekeys generated")
            
            // Step 4: Create device key bundle
            val deviceBundle = signalKeyManager.createDeviceKeyBundle()
            Log.d(TAG, "✓ Device key bundle created")
            
            // Step 5: Upload to Firestore
            firestoreKeyRepo.publishDeviceKeys(userId, deviceBundle)
            Log.d(TAG, "✓ Keys uploaded to Firestore")
            
            isInitialized = true
            Log.d(TAG, "🎉 Device initialization complete! DeviceId: ${deviceBundle.deviceId}")
            
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Device initialization failed", e)
            return@withContext false
        }
    }

    /**
     * Refresh prekeys if running low.
     * Should be called periodically (e.g., weekly).
     */
    suspend fun refreshPreKeysIfNeeded(userId: String) = withContext(Dispatchers.IO) {
        try {
            val wasRefreshed = signalKeyManager.refreshPreKeysIfNeeded()
            
            if (wasRefreshed) {
                // Upload updated bundle
                val deviceBundle = signalKeyManager.createDeviceKeyBundle()
                firestoreKeyRepo.publishDeviceKeys(userId, deviceBundle)
                Log.d(TAG, "Prekeys refreshed and uploaded for user: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh prekeys", e)
        }
    }

    /**
     * Reset initialization state (e.g., after logout).
     */
    fun reset() {
        isInitialized = false
        Log.d(TAG, "Device initializer reset")
    }
}
