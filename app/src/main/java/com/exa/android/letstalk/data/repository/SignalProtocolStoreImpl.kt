package com.exa.android.letstalk.data.repository

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.exa.android.letstalk.core.di.SignalEncryptedPrefs
import com.exa.android.letstalk.data.local.room.ScheduleMessageDatabase
import com.exa.android.letstalk.data.local.room.crypto.PreKeyEntity
import com.exa.android.letstalk.data.local.room.crypto.SessionRecordEntity
import com.exa.android.letstalk.data.local.room.crypto.SignedPreKeyEntity
import kotlinx.coroutines.runBlocking
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.groups.SenderKeyName
import org.whispersystems.libsignal.groups.state.SenderKeyRecord
import org.whispersystems.libsignal.state.IdentityKeyStore
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SessionRecord
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.SignedPreKeyRecord

import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Implementation of Signal Protocol's storage interface.
 * Persists identity keys, prekeys, and session records using:
 * - EncryptedSharedPreferences for identity key pairs
 * - Room database for sessions and prekeys
 *
 * Library: org.whispersystems:signal-protocol-android:2.8.1
 *
 * @property encryptedPrefs Secure storage for private keys
 * @property database Room database for crypto entities
 */
@Singleton
class SignalProtocolStoreImpl @Inject constructor(
    @SignalEncryptedPrefs private val encryptedPrefs: SharedPreferences,
    private val database: ScheduleMessageDatabase
) : SignalProtocolStore {

    private val cryptoDao = database.cryptoDao()
    private val TAG = "SignalProtocolStore"

    companion object {
        private const val IDENTITY_KEY_PAIR_KEY = "signal_identity_key_pair"
        private const val REGISTRATION_ID_KEY = "signal_registration_id"
    }

    // ==================== Identity Key Management ====================

    override fun getIdentityKeyPair(): IdentityKeyPair {
        val serialized = encryptedPrefs.getString(IDENTITY_KEY_PAIR_KEY, null)
            ?: throw IllegalStateException("Identity key pair not initialized")

        val bytes = Base64.decode(serialized, Base64.DEFAULT)
        return IdentityKeyPair(bytes)
    }

    override fun getLocalRegistrationId(): Int {
        return encryptedPrefs.getInt(REGISTRATION_ID_KEY, 0)
    }

    fun saveIdentityKeyPair(identityKeyPair: IdentityKeyPair, registrationId: Int) {
        val serialized = Base64.encodeToString(identityKeyPair.serialize(), Base64.DEFAULT)
        encryptedPrefs.edit {
            putString(IDENTITY_KEY_PAIR_KEY, serialized)
                .putInt(REGISTRATION_ID_KEY, registrationId)
        }
        Log.d(TAG, "Identity key pair saved with registration ID: $registrationId")
    }

    // ==================== Trusted Identity Management ====================

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
        // For simplicity, we trust all identity keys on first encounter
        // Production apps should implement proper trust verification UI
        val key = "trusted_identity_${address.name}_${address.deviceId}"
        val serialized = Base64.encodeToString(identityKey.serialize(), Base64.DEFAULT)
        encryptedPrefs.edit { putString(key, serialized) }
        return true
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress,
        identityKey: IdentityKey,
        direction: IdentityKeyStore.Direction
    ): Boolean {
        val key = "trusted_identity_${address.name}_${address.deviceId}"
        val saved = encryptedPrefs.getString(key, null) ?: return true // Trust on first use

        val savedKey = IdentityKey(Base64.decode(saved, Base64.DEFAULT) as ECPublicKey?)
        return savedKey == identityKey
    }

    override fun getIdentity(address: SignalProtocolAddress): IdentityKey? {
        val key = "trusted_identity_${address.name}_${address.deviceId}"
        val saved = encryptedPrefs.getString(key, null) ?: return null
        return IdentityKey(Base64.decode(saved, Base64.DEFAULT) as ECPublicKey?)
    }

    // ==================== Session Management ====================

    override fun loadSession(address: SignalProtocolAddress): SessionRecord = runBlocking {
        val addressKey = "${address.name}:${address.deviceId}"
        val entity = cryptoDao.getSession(addressKey)

        return@runBlocking if (entity != null) {
            SessionRecord(entity.record)
        } else {
            SessionRecord()
        }
    }

    fun loadExistingSessions(addresses: MutableList<SignalProtocolAddress>): MutableList<SessionRecord> = runBlocking {
        val records = mutableListOf<SessionRecord>()
        addresses.forEach { address ->
            val addressKey = "${address.name}:${address.deviceId}"
            val entity = cryptoDao.getSession(addressKey)
            if (entity != null) {
                try {
                    records.add(SessionRecord(entity.record))
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to deserialize session for $addressKey", e)
                }
            }
        }
        return@runBlocking records
    }

    override fun getSubDeviceSessions(name: String): List<Int> = runBlocking {
        val sessions = cryptoDao.getAllSessions()
        return@runBlocking sessions
            .filter { it.address.startsWith("$name:") }
            .mapNotNull {
                val parts = it.address.split(":")
                if (parts.size == 2) parts[1].toIntOrNull() else null
            }
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
        runBlocking {
            val addressKey = "${address.name}:${address.deviceId}"
            val entity = SessionRecordEntity(
                address = addressKey,
                record = record.serialize(),
                timestamp = System.currentTimeMillis()
            )
            cryptoDao.insertSession(entity)
            Log.d(TAG, "Session stored for: $addressKey")
        }
    }

    override fun containsSession(address: SignalProtocolAddress): Boolean = runBlocking {
        val addressKey = "${address.name}:${address.deviceId}"
        val hasSession = cryptoDao.containsSession(addressKey)
        
        // Also verify the session is not empty
        if (hasSession) {
            val entity = cryptoDao.getSession(addressKey)
            if (entity != null) {
                try {
                    val session = SessionRecord(entity.record)
                    return@runBlocking !session.isFresh
                } catch (e: Exception) {
                    Log.w(TAG, "Session exists but is corrupted for $addressKey", e)
                    return@runBlocking false
                }
            }
        }
        return@runBlocking false
    }

    override fun deleteSession(address: SignalProtocolAddress) {
        runBlocking {
            val addressKey = "${address.name}:${address.deviceId}"
            cryptoDao.deleteSession(addressKey)
            Log.d(TAG, "Session deleted for: $addressKey")
        }
    }

    override fun deleteAllSessions(name: String) {
        runBlocking {
            val sessions = cryptoDao.getAllSessions()
            sessions.filter { it.address.startsWith("$name:") }
                .forEach { cryptoDao.deleteSession(it.address) }
            Log.d(TAG, "All sessions deleted for user: $name")
        }
    }
    
    /**
     * Archive a session (marks for re-establishment).
     */
    fun archiveSession(address: SignalProtocolAddress) {
        runBlocking {
            val addressKey = "${address.name}:${address.deviceId}"
            val entity = cryptoDao.getSession(addressKey)
            if (entity != null) {
                val session = SessionRecord(entity.record)
                session.archiveCurrentState()
                cryptoDao.insertSession(
                    SessionRecordEntity(
                        address = addressKey,
                        record = session.serialize(),
                        timestamp = System.currentTimeMillis()
                    )
                )
                Log.d(TAG, "Session archived for: $addressKey")
            }
        }
    }

    // ==================== PreKey Management ====================

    override fun loadPreKey(preKeyId: Int): PreKeyRecord = runBlocking {
        val entity = cryptoDao.getPreKey(preKeyId)
            ?: throw InvalidKeyIdException("No prekey found for ID: $preKeyId") as Throwable
        return@runBlocking PreKeyRecord(entity.record)
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
        runBlocking {
            val entity = PreKeyEntity(
                preKeyId = preKeyId,
                record = record.serialize()
            )
            cryptoDao.insertPreKey(entity)
        }
    }

    override fun containsPreKey(preKeyId: Int): Boolean = runBlocking {
        return@runBlocking cryptoDao.getPreKey(preKeyId) != null
    }

    override fun removePreKey(preKeyId: Int) {
        runBlocking {
            cryptoDao.deletePreKey(preKeyId)
            Log.d(TAG, "PreKey removed: $preKeyId")
        }
    }

    // ==================== SignedPreKey Management ====================

    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord = runBlocking {
        val entity = cryptoDao.getSignedPreKey(signedPreKeyId)
            ?: throw InvalidKeyIdException("No signed prekey found for ID: $signedPreKeyId")
        return@runBlocking SignedPreKeyRecord(entity.record)
    }

    override fun loadSignedPreKeys(): List<SignedPreKeyRecord> = runBlocking {
        return@runBlocking cryptoDao.getAllSignedPreKeys()
            .map { SignedPreKeyRecord(it.record) }
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
        runBlocking {
            val entity = SignedPreKeyEntity(
                signedPreKeyId = signedPreKeyId,
                record = record.serialize(),
                timestamp = System.currentTimeMillis()
            )
            cryptoDao.insertSignedPreKey(entity)
        }
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean = runBlocking {
        return@runBlocking cryptoDao.getSignedPreKey(signedPreKeyId) != null
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        runBlocking {
            cryptoDao.deleteSignedPreKey(signedPreKeyId)
            Log.d(TAG, "SignedPreKey removed: $signedPreKeyId")
        }
    }

    // ==================== Utility Methods ====================

    fun getPreKeyCount(): Int = runBlocking {
        return@runBlocking cryptoDao.getPreKeyCount()
    }

    fun clearAllData() = runBlocking {
        cryptoDao.deleteAllSessions()
        cryptoDao.deleteAllPreKeys()
        cryptoDao.deleteAllSignedPreKeys()
        encryptedPrefs.edit().clear().apply()
        Log.d(TAG, "All Signal Protocol data cleared")
    }

     fun clearIdentityKeys() {
         encryptedPrefs.edit()
            .remove(IDENTITY_KEY_PAIR_KEY)
            .remove(REGISTRATION_ID_KEY)
            .apply()
     }
     
    // ==================== Sender Key Management (for Group Chats) ====================
    // These methods are required by SignalProtocolStore but will be fully implemented
    // when we add Sender Keys support for group chats
    
    fun storeSenderKey(senderKeyName: SenderKeyName,
                                record: SenderKeyRecord
    ) {
        // TODO: Implement when adding group chat support with Sender Keys
        Log.w(TAG, "storeSenderKey called but not yet implemented - group E2EE pending")
    }
    
    fun loadSenderKey(senderKeyName: SenderKeyName):
        SenderKeyRecord? {
        // TODO: Implement when adding group chat support with Sender Keys
        return null
    }
}