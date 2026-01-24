package com.exa.android.letstalk.data.local.room.crypto

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing one-time prekeys.
 * These keys are consumed during X3DH key agreement and provide forward secrecy.
 *
 * @property preKeyId Unique prekey identifier
 * @property record Serialized PreKeyRecord bytes from libsignal
 */
@Entity(tableName = "signal_prekeys")
data class PreKeyEntity(
    @PrimaryKey
    val preKeyId: Int,
    val record: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PreKeyEntity

        if (preKeyId != other.preKeyId) return false
        if (!record.contentEquals(other.record)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = preKeyId
        result = 31 * result + record.contentHashCode()
        return result
    }
}
