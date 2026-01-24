package com.exa.android.letstalk.data.local.room.crypto

import androidx.room.*

/**
 * DAO for Signal Protocol cryptographic storage operations.
 * Handles sessions, prekeys, and signed prekeys.
 */
@Dao
interface CryptoDao {
    
    // ==================== Session Operations ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionRecordEntity)
    
    @Query("SELECT * FROM signal_sessions WHERE address = :address")
    suspend fun getSession(address: String): SessionRecordEntity?
    
    @Query("SELECT * FROM signal_sessions")
    suspend fun getAllSessions(): List<SessionRecordEntity>
    
    @Query("DELETE FROM signal_sessions WHERE address = :address")
    suspend fun deleteSession(address: String)
    
    @Query("DELETE FROM signal_sessions")
    suspend fun deleteAllSessions()
    
    @Query("SELECT EXISTS(SELECT 1 FROM signal_sessions WHERE address = :address LIMIT 1)")
    suspend fun containsSession(address: String): Boolean
    
    // ==================== PreKey Operations ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreKey(preKey: PreKeyEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreKeys(preKeys: List<PreKeyEntity>)
    
    @Query("SELECT * FROM signal_prekeys WHERE preKeyId = :preKeyId")
    suspend fun getPreKey(preKeyId: Int): PreKeyEntity?
    
    @Query("SELECT * FROM signal_prekeys")
    suspend fun getAllPreKeys(): List<PreKeyEntity>
    
    @Query("DELETE FROM signal_prekeys WHERE preKeyId = :preKeyId")
    suspend fun deletePreKey(preKeyId: Int)
    
    @Query("DELETE FROM signal_prekeys")
    suspend fun deleteAllPreKeys()
    
    @Query("SELECT COUNT(*) FROM signal_prekeys")
    suspend fun getPreKeyCount(): Int
    
    // ==================== SignedPreKey Operations ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignedPreKey(signedPreKey: SignedPreKeyEntity)
    
    @Query("SELECT * FROM signal_signed_prekeys WHERE signedPreKeyId = :signedPreKeyId")
    suspend fun getSignedPreKey(signedPreKeyId: Int): SignedPreKeyEntity?
    
    @Query("SELECT * FROM signal_signed_prekeys ORDER BY timestamp DESC")
    suspend fun getAllSignedPreKeys(): List<SignedPreKeyEntity>
    
    @Query("DELETE FROM signal_signed_prekeys WHERE signedPreKeyId = :signedPreKeyId")
    suspend fun deleteSignedPreKey(signedPreKeyId: Int)
    
    @Query("DELETE FROM signal_signed_prekeys")
    suspend fun deleteAllSignedPreKeys()
}
