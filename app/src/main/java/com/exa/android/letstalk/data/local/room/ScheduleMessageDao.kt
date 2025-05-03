package com.exa.android.letstalk.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMessage(message: ScheduledMessageEntity)

    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledTime DESC")
    fun getAllMessages(): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE scheduledTime = :time")
    suspend fun getMessagesAtTime(time: Long): List<ScheduledMessageEntity>

    @Query("SELECT COUNT(*) FROM scheduled_messages WHERE scheduledTime = :time")
    suspend fun getMessageCountAtTime(time: Long): Int

    @Query("DELETE FROM scheduled_messages WHERE messageId = :messageId")
    suspend fun deleteScheduledMessage(messageId: String)

    @Query("DELETE FROM scheduled_messages WHERE scheduledTime = :time")
    suspend fun deleteAllMessagesAtTime(time: Long)
}
