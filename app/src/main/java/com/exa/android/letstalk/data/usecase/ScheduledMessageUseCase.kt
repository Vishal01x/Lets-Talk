package com.exa.android.letstalk.data.usecase

import com.exa.android.letstalk.data.local.room.ScheduledMessageEntity
import kotlinx.coroutines.flow.Flow

interface ScheduledMessageRepository {
    fun getAllMessages(): Flow<List<ScheduledMessageEntity>>
    suspend fun deleteMessage(message: ScheduledMessageEntity)
    suspend fun saveScheduledMessage(message: ScheduledMessageEntity)
    suspend fun getMessagesAtTime(time: Long): List<ScheduledMessageEntity>
    suspend fun getMessageCountAtTime(time: Long): Int
    suspend fun removeScheduledMessage(messageId: String)
    suspend fun removeAllMessagesAtTime(time: Long)
}
