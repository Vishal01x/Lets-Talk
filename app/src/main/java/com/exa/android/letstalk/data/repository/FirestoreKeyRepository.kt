package com.exa.android.letstalk.data.repository

import android.util.Log
import com.exa.android.letstalk.domain.DeviceKeyBundle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for uploading and fetching public keys from Firestore.
 * Schema: users/{userId}/devices/{deviceId}
 */
@Singleton
class FirestoreKeyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val TAG = "FirestoreKeyRepo"

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val DEVICES_COLLECTION = "devices"
    }

    /**
     * Publish device public keys to Firestore.
     * @param userId User ID
     * @param bundle Device key bundle containing all public keys
     */
    suspend fun publishDeviceKeys(userId: String, bundle: DeviceKeyBundle) {
        try {
            val deviceRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(DEVICES_COLLECTION)
                .document(bundle.deviceId.toString())

            val data = mapOf(
                "deviceId" to bundle.deviceId,
                "registrationId" to bundle.registrationId,
                "identityKey" to bundle.identityKey,
                "signedPreKeyId" to bundle.signedPreKeyId,
                "signedPreKeyPublic" to bundle.signedPreKeyPublic,
                "signedPreKeySignature" to bundle.signedPreKeySignature,
                "preKeyId" to bundle.preKeyId,
                "preKeyPublic" to bundle.preKeyPublic,
                "timestamp" to System.currentTimeMillis()
            )

            deviceRef.set(data).await()
            Log.d(TAG, "Published keys for user $userId device ${bundle.deviceId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to publish keys for $userId", e)
            throw e
        }
    }

    /**
     * Fetch all device key bundles for a user.
     * @param userId User ID
     * @return List of device key bundles
     */
    suspend fun fetchDeviceKeys(userId: String): List<DeviceKeyBundle> {
        return try {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(DEVICES_COLLECTION)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    DeviceKeyBundle(
                        deviceId = doc.getLong("deviceId")?.toInt() ?: 0,
                        registrationId = doc.getLong("registrationId")?.toInt() ?: 0,
                        identityKey = doc.getString("identityKey") ?: "",
                        signedPreKeyId = doc.getLong("signedPreKeyId")?.toInt() ?: 0,
                        signedPreKeyPublic = doc.getString("signedPreKeyPublic") ?: "",
                        signedPreKeySignature = doc.getString("signedPreKeySignature") ?: "",
                        preKeyId = doc.getLong("preKeyId")?.toInt(),
                        preKeyPublic = doc.getString("preKeyPublic")
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse device bundle from ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch device keys for $userId", e)
            emptyList()
        }
    }

    /**
     * Fetch key bundle for a specific device.
     * @param userId User ID
     * @param deviceId Device ID
     * @return Device key bundle or null if not found
     */
    suspend fun fetchSingleDeviceKeys(userId: String, deviceId: Int): DeviceKeyBundle? {
        return try {
            val doc = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(DEVICES_COLLECTION)
                .document(deviceId.toString())
                .get()
                .await()

            if (doc.exists()) {
                DeviceKeyBundle(
                    deviceId = doc.getLong("deviceId")?.toInt() ?: deviceId,
                    registrationId = doc.getLong("registrationId")?.toInt() ?: 0,
                    identityKey = doc.getString("identityKey") ?: "",
                    signedPreKeyId = doc.getLong("signedPreKeyId")?.toInt() ?: 0,
                    signedPreKeyPublic = doc.getString("signedPreKeyPublic") ?: "",
                    signedPreKeySignature = doc.getString("signedPreKeySignature") ?: "",
                    preKeyId = doc.getLong("preKeyId")?.toInt(),
                    preKeyPublic = doc.getString("preKeyPublic")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch keys for $userId:$deviceId", e)
            null
        }
    }

    /**
     * Atomically consume a one-time prekey using Firestore transaction.
     * This ensures the same prekey isn't used by multiple senders.
     *
     * @param userId User ID
     * @param deviceId Device ID
     * @return Consumed one-time prekey data (preKeyId and preKeyPublic) or null
     */
    suspend fun consumeOneTimePreKey(userId: String, deviceId: Int): Pair<Int, String>? {
        return try {
            val deviceRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(DEVICES_COLLECTION)
                .document(deviceId.toString())

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(deviceRef)
                val preKeyId = snapshot.getLong("preKeyId")?.toInt()
                val preKeyPublic = snapshot.getString("preKeyPublic")

                if (preKeyId != null && preKeyPublic != null) {
                    // Remove the one-time prekey atomically
                    transaction.update(deviceRef, mapOf(
                        "preKeyId" to null,
                        "preKeyPublic" to null
                    ))
                    Pair(preKeyId, preKeyPublic)
                } else {
                    null
                }
            }.await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to consume prekey for $userId:$deviceId", e)
            null
        }
    }

    /**
     * Delete all device keys for a user (e.g., on logout).
     */
    suspend fun deleteDeviceKeys(userId: String, deviceId: Int) {
        try {
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(DEVICES_COLLECTION)
                .document(deviceId.toString())
                .delete()
                .await()

            Log.d(TAG, "Deleted keys for $userId:$deviceId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete keys for $userId:$deviceId", e)
        }
    }
}