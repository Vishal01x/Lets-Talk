package com.exa.android.letstalk.data.local.room.crypto

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing Signal Protocol session records.
 * Each session represents an encrypted communication channel with a specific device.
 *
 * @property address Unique identifier in format "userId:deviceId"
 * @property record Serialized SessionRecord bytes from libsignal
 * @property timestamp Last update timestamp for session management
 */
@Entity(tableName = "signal_sessions")
data class SessionRecordEntity(
    @PrimaryKey
    val address: String, // Format: "userId:deviceId"
    val record: ByteArray,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionRecordEntity

        if (address != other.address) return false
        if (!record.contentEquals(other.record)) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + record.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
