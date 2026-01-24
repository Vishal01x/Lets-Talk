package com.exa.android.letstalk.data.signal_protocol

import android.util.Base64
import android.util.Log
import com.exa.android.letstalk.domain.DeviceKeyBundle
import com.exa.android.letstalk.data.repository.SignalProtocolStoreImpl
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.util.KeyHelper
import org.whispersystems.libsignal.util.KeyHelper.generateIdentityKeyPair
import org.whispersystems.libsignal.util.KeyHelper.generateRegistrationId
import org.whispersystems.libsignal.util.KeyHelper.generateSignedPreKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Signal Protocol cryptographic keys.
 * Handles key generation, rotation, and bundle creation.
 * 
 * Library: org.whispersystems:signal-protocol-android:2.8.1
 */
@Singleton
class SignalKeyManager @Inject constructor(
    private val store: SignalProtocolStoreImpl
) {

    private val TAG = "SignalKeyManager"

    companion object {
        const val DEFAULT_PREKEY_COUNT = 100
        const val PREKEY_REFRESH_THRESHOLD = 50
        const val SIGNED_PREKEY_ROTATION_DAYS = 30
    }

    /**
     * Ensure identity keys exist and are valid. Regenerates if corrupted.
     */
    suspend fun ensureIdentityKeys(): IdentityKeyPair {
        return try {
            store.getIdentityKeyPair()
        } catch (e: Exception) {
            Log.w(TAG, "IdentityKeyPair missing or corrupted. Regenerating.", e)

            // Clear corrupted data BEFORE regenerating
            store.clearIdentityKeys()

            val identityKeyPair = generateIdentityKeyPair()
            val registrationId = generateRegistrationId(false)

            store.saveIdentityKeyPair(identityKeyPair, registrationId)
            Log.d(TAG, "✓ New identity keys generated")
            identityKeyPair
        }
    }

    /**
     * Generate or rotate a signed prekey.
     */
    suspend fun generateSignedPreKey(): SignedPreKeyRecord {
        val identityKeyPair = ensureIdentityKeys()

        val signedPreKeyId = (System.currentTimeMillis() / 1000).toInt()
        val signedPreKey = generateSignedPreKey(identityKeyPair, signedPreKeyId)

        store.storeSignedPreKey(signedPreKeyId, signedPreKey)

        Log.d(TAG, "✓ Generated signed prekey with ID: $signedPreKeyId")
        return signedPreKey
    }

    /**
     * Generate batch of one-time prekeys.
     */
    suspend fun generateOneTimePreKeys(
        count: Int = DEFAULT_PREKEY_COUNT,
        startId: Int = 0
    ): List<PreKeyRecord> {

        val preKeys = KeyHelper.generatePreKeys(startId, count)

        preKeys.forEach { preKey ->
            store.storePreKey(preKey.getId(), preKey)
        }

        Log.d(TAG, "✓ Generated $count one-time prekeys starting at ID: $startId")
        return preKeys
    }

    /**
     * Derive device ID from registration ID.
     */
    fun getDeviceId(): Int {
        return store.getLocalRegistrationId() % 10000
    }

    /**
     * Create a device key bundle for server upload.
     * Includes at least one one-time pre-key for initial session establishment.
     */
    suspend fun createDeviceKeyBundle(): DeviceKeyBundle {

        val identityKeyPair = ensureIdentityKeys()
        val registrationId = store.getLocalRegistrationId()
        val deviceId = getDeviceId()

        val signedPreKey = try {
            store.loadSignedPreKeys().firstOrNull()
                ?: generateSignedPreKey()
        } catch (e: Exception) {
            generateSignedPreKey()
        }

        // Get the first available one-time pre-key for the bundle
        val preKeys = try {
            val allPreKeys = mutableListOf<PreKeyRecord>()
            var preKeyId = 0
            while (allPreKeys.isEmpty() && preKeyId < 1000) {
                try {
                    val preKey = store.loadPreKey(preKeyId)
                    allPreKeys.add(preKey)
                    break
                } catch (e: Exception) {
                    preKeyId++
                }
            }
            allPreKeys
        } catch (e: Exception) {
            emptyList()
        }

        val firstPreKey = preKeys.firstOrNull()

        return DeviceKeyBundle(
            deviceId = deviceId,
            registrationId = registrationId,

            identityKey = Base64.encodeToString(
                identityKeyPair.publicKey.serialize(),
                Base64.NO_WRAP
            ),

            signedPreKeyId = signedPreKey.id,

            signedPreKeyPublic = Base64.encodeToString(
                signedPreKey.keyPair.publicKey.serialize(),
                Base64.NO_WRAP
            ),

            signedPreKeySignature = Base64.encodeToString(
                signedPreKey.signature,
                Base64.NO_WRAP
            ),

            preKeyId = firstPreKey?.id,
            preKeyPublic = firstPreKey?.let {
                Base64.encodeToString(
                    it.keyPair.publicKey.serialize(),
                    Base64.NO_WRAP
                )
            }
        )
    }

    /**
     * Refresh one-time prekeys if below threshold.
     */
    suspend fun refreshPreKeysIfNeeded(): Boolean {

        val currentCount = store.getPreKeyCount()

        return if (currentCount < PREKEY_REFRESH_THRESHOLD) {

            val toGenerate = DEFAULT_PREKEY_COUNT - currentCount
            val startId = (System.currentTimeMillis() % 100000).toInt()

            generateOneTimePreKeys(toGenerate, startId)

            Log.d(TAG, "✓ Prekeys refreshed: generated $toGenerate")
            true
        } else {
            Log.d(TAG, "Prekey count sufficient: $currentCount")
            false
        }
    }

    /**
     * Initialize a new device with all required keys.
     */
    suspend fun initializeDevice() {
        ensureIdentityKeys()
        generateSignedPreKey()
        generateOneTimePreKeys(DEFAULT_PREKEY_COUNT)

        Log.d(TAG, "✓ Device initialized with full Signal key set")
    }
    
    /**
     * Get identity key fingerprint for verification.
     * @return Hex-encoded SHA-256 hash of identity public key
     */
    fun getIdentityKeyFingerprint(): String {
        try {
            val identityKey = store.getIdentityKeyPair().publicKey
            val publicKeyBytes = identityKey.serialize()
            
            // Create SHA-256 hash
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(publicKeyBytes)
            
            // Convert to hex string
            return hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate fingerprint", e)
            return ""
        }
    }
}
