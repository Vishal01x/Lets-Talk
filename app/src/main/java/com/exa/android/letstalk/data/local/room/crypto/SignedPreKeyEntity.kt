package com.exa.android.letstalk.data.local.room.crypto

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing signed prekeys.
 * These are rotated periodically (recommended: monthly) and signed by the identity key.
 *
 * @property signedPreKeyId Unique signed prekey identifier
 * @property record Serialized SignedPreKeyRecord bytes from libsignal
 * @property timestamp Creation timestamp for rotation tracking
 */
@Entity(tableName = "signal_signed_prekeys")
data class SignedPreKeyEntity(
    @PrimaryKey
    val signedPreKeyId: Int,
    val record: ByteArray,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignedPreKeyEntity

        if (signedPreKeyId != other.signedPreKeyId) return false
        if (!record.contentEquals(other.record)) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = signedPreKeyId
        result = 31 * result + record.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
