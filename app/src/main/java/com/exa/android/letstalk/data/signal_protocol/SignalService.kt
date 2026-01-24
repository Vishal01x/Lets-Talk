package com.exa.android.letstalk.data.signal_protocol

import android.util.Base64
import android.util.Log
import com.exa.android.letstalk.domain.DeviceKeyBundle
import com.exa.android.letstalk.data.repository.SignalProtocolStoreImpl
import org.whispersystems.libsignal.DuplicateMessageException
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.LegacyMessageException
import org.whispersystems.libsignal.NoSessionException
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.UntrustedIdentityException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.CiphertextMessage
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import java.security.InvalidKeyException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core Signal Protocol service for encryption and decryption.
 * Handles session establishment, message encryption/decryption.
 * 
 * Library: org.whispersystems:signal-protocol-android:2.8.1
 */
@Singleton
class SignalService @Inject constructor(
    private val store: SignalProtocolStoreImpl,
    private val keyManager: SignalKeyManager
) {
    private val TAG = "SignalService"

    /**
     * Build a Signal Protocol session with a remote device using X3DH.
     * @param userId Remote user ID
     * @param deviceBundle Public key bundle from remote device
     * @throws InvalidKeyException if keys in bundle are invalid
     */
    suspend fun buildSessionForRemote(userId: String, deviceBundle: DeviceKeyBundle) {
        try {
            val address = SignalProtocolAddress(userId, deviceBundle.deviceId)
            
            // Check if session already exists to prevent duplicate processing
            if (store.containsSession(address)) {
                Log.d(TAG, "Session already exists with $userId:${deviceBundle.deviceId}, skipping")
                return
            }

            // Parse public keys from base64
            val identityKey = IdentityKey(
                Base64.decode(deviceBundle.identityKey, Base64.NO_WRAP), 0
            )

            val signedPreKeyPublic = Curve.decodePoint(
                Base64.decode(deviceBundle.signedPreKeyPublic, Base64.NO_WRAP),
                0
            )

            val signedPreKeySignature = Base64.decode(
                deviceBundle.signedPreKeySignature,
                Base64.NO_WRAP
            )

            // Optional one-time prekey
            val preKeyId = deviceBundle.preKeyId
            val preKeyPublic = deviceBundle.preKeyPublic?.let {
                Curve.decodePoint(Base64.decode(it, Base64.NO_WRAP), 0)
            }

            // Build PreKeyBundle
            val preKeyBundle = PreKeyBundle(
                deviceBundle.registrationId,
                deviceBundle.deviceId,
                preKeyId ?: 0, // Use 0 if no one-time prekey
                preKeyPublic,
                deviceBundle.signedPreKeyId,
                signedPreKeyPublic,
                signedPreKeySignature,
                identityKey
            )

            // Create session
            val sessionBuilder = SessionBuilder(store, address)
            sessionBuilder.process(preKeyBundle)

            Log.d(TAG, "✓ Session established with $userId:${deviceBundle.deviceId}")
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "Invalid keys in bundle for $userId", e)
            throw e
        } catch (e: UntrustedIdentityException) {
            Log.e(TAG, "Untrusted identity for $userId", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build session with $userId", e)
            throw e
        }
    }

    /**
     * Check if a session exists with a remote device.
     */
    fun hasSession(userId: String, deviceId: Int): Boolean {
        val address = SignalProtocolAddress(userId, deviceId)
        return store.containsSession(address)
    }

    /**
     * Encrypt a message for a specific device.
     * @param userId Remote user ID
     * @param deviceId Remote device ID
     * @param plaintext Message bytes to encrypt
     * @return Encrypted ciphertext message
     * @throws NoSessionException if no session exists (call buildSessionForRemote first)
     */
    suspend fun encryptMessage(
        userId: String,
        deviceId: Int,
        plaintext: ByteArray
    ): CiphertextMessage {
        try {
            val address = SignalProtocolAddress(userId, deviceId)
            
            // Verify session exists
            if (!store.containsSession(address)) {
                throw NoSessionException("No session exists with $userId:$deviceId. Build session first.") as Throwable
            }
            
            val sessionCipher = SessionCipher(store, address)
            
            val ciphertext = sessionCipher.encrypt(plaintext)
            
            val type = when (ciphertext.type) {
                CiphertextMessage.PREKEY_TYPE -> "PreKeySignalMessage"
                CiphertextMessage.WHISPER_TYPE -> "SignalMessage"
                else -> "Unknown"
            }
            
            Log.d(TAG, "✓ Encrypted message for $userId:$deviceId (type: $type)")
            return ciphertext
        } catch (e: NoSessionException) {
            Log.e(TAG, "No session for $userId:$deviceId - build session first", e)
            throw e
        } catch (e: UntrustedIdentityException) {
            Log.e(TAG, "Untrusted identity for $userId:$deviceId", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed for $userId:$deviceId", e)
            throw e
        }
    }

    /**
     * Decrypt a message from a specific device.
     * @param senderId Sender user ID
     * @param deviceId Sender device ID
     * @param ciphertextBytes Encrypted message bytes
     * @param ciphertextType 1 = PreKeySignalMessage, 2 = SignalMessage
     * @return Decrypted plaintext bytes
     * @throws InvalidMessageException if message cannot be decrypted
     */
    suspend fun decryptMessage(
        senderId: String,
        deviceId: Int,
        ciphertextBytes: ByteArray,
        ciphertextType: Int
    ): ByteArray {
        try {
            val address = SignalProtocolAddress(senderId, deviceId)
            val sessionCipher = SessionCipher(store, address)

            val plaintext = when (ciphertextType) {
                CiphertextMessage.PREKEY_TYPE -> {
                    // First message or session reset
                    val preKeyMessage = PreKeySignalMessage(ciphertextBytes)
                    sessionCipher.decrypt(preKeyMessage)
                }
                CiphertextMessage.WHISPER_TYPE -> {
                    // Subsequent messages
                    val signalMessage = SignalMessage(ciphertextBytes)
                    sessionCipher.decrypt(signalMessage)
                }
                else -> {
                    throw InvalidMessageException("Unknown ciphertext type: $ciphertextType") as Throwable
                }
            }

            Log.d(TAG, "✓ Decrypted message from $senderId:$deviceId")
            return plaintext
        } catch (e: InvalidMessageException) {
            Log.e(TAG, "❌ Invalid message from $senderId:$deviceId", e)
            throw e
        } catch (e: DuplicateMessageException) {
            Log.w(TAG, "⚠️ Duplicate message from $senderId:$deviceId", e)
            throw e
        } catch (e: LegacyMessageException) {
            Log.e(TAG, "❌ Legacy message from $senderId:$deviceId", e)
            throw e
        } catch (e: InvalidKeyIdException) {
            Log.e(TAG, "❌ Invalid key ID from $senderId:$deviceId", e)
            throw e
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "❌ Invalid key from $senderId:$deviceId", e)
            throw e
        } catch (e: UntrustedIdentityException) {
            Log.e(TAG, "❌ Untrusted identity from $senderId:$deviceId", e)
            throw e
        } catch (e: NoSessionException) {
            Log.e(TAG, "❌ No session exists with $senderId:$deviceId", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Decryption failed for $senderId:$deviceId", e)
            throw e
        }
    }

    /**
     * Convenience method to encrypt text messages.
     */
    suspend fun encryptText(userId: String, deviceId: Int, message: String): CiphertextMessage {
        return encryptMessage(userId, deviceId, message.toByteArray(Charsets.UTF_8))
    }

    /**
     * Convenience method to decrypt text messages.
     */
    suspend fun decryptText(
        senderId: String,
        deviceId: Int,
        ciphertextBytes: ByteArray,
        ciphertextType: Int
    ): String {
        val plaintext = decryptMessage(senderId, deviceId, ciphertextBytes, ciphertextType)
        return String(plaintext, Charsets.UTF_8)
    }

    /**
     * Delete session with a specific device.
     */
    fun deleteSession(userId: String, deviceId: Int) {
        val address = SignalProtocolAddress(userId, deviceId)
        store.deleteSession(address)
        Log.d(TAG, "Deleted session with $userId:$deviceId")
    }

    /**
     * Delete all sessions with a user (all their devices).
     */
    fun deleteAllSessionsWithUser(userId: String) {
        store.deleteAllSessions(userId)
        Log.d(TAG, "Deleted all sessions with $userId")
    }
    
    /**
     * Archive a session (marks it as needing re-establishment on security events).
     */
    fun archiveSession(userId: String, deviceId: Int) {
        val address = SignalProtocolAddress(userId, deviceId)
        store.archiveSession(address)
        Log.d(TAG, "Archived session with $userId:$deviceId")
    }
}
