package com.exa.android.letstalk.data.local.room

import com.exa.android.letstalk.data.usecase.ScheduledMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduledMessageRepositoryImpl @Inject constructor(
    private val db: ScheduleMessageDatabase
): ScheduledMessageRepository {
    val dao = db.dao

    override fun getAllMessages(): Flow<List<ScheduledMessageEntity>> = dao.getAllMessages()

    override suspend fun deleteMessage(message: ScheduledMessageEntity) {
        dao.deleteScheduledMessage(message.messageId)
    }

    override suspend fun saveScheduledMessage(message: ScheduledMessageEntity) {
        dao.insertScheduledMessage(message)
    }

    suspend fun saveScheduledMessageImpl(message: ScheduledMessageEntity) {

    }

    override suspend fun getMessagesAtTime(time: Long): List<ScheduledMessageEntity> {
        return dao.getMessagesAtTime(time)
    }

    override suspend fun getMessageCountAtTime(time: Long): Int {
        return dao.getMessageCountAtTime(time)
    }

    override suspend fun removeScheduledMessage(messageId: String) {
        dao.deleteScheduledMessage(messageId)
    }

    override suspend fun removeAllMessagesAtTime(time: Long) {
        dao.deleteAllMessagesAtTime(time)
    }
}
