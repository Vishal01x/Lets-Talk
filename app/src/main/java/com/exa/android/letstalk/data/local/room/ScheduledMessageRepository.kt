package com.exa.android.letstalk.data.local.room

import com.exa.android.letstalk.data.usecase.ScheduledMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduledMessageRepositoryImpl @Inject constructor(
    private val db: ScheduleMessageDatabase
): ScheduledMessageRepository {
    val dao = db.scheduleMessageDao()

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

    override suspend fun getMessagesScheduledOnOrBefore(time: Long): List<ScheduledMessageEntity> {
        return dao.getMessagesScheduledOnOrBefore(time)
    }

    override suspend fun deleteMessagesScheduledOnOrBefore(time: Long) {
        dao.deleteMessagesScheduledOnOrBefore(time)
    }

    override suspend fun getFutureScheduledMessages(time: Long): List<ScheduledMessageEntity> {
        return dao.getFutureScheduledMessages(time)
    }

    override suspend fun markMessagesAsSent(time: Long) {
        dao.markMessagesAsSent(time)
    }

    override fun getScheduledMessages(): Flow<List<ScheduledMessageEntity>> {
        return dao.getScheduledMessages()
    }

    override fun getSentMessages(): Flow<List<ScheduledMessageEntity>> {
        return dao.getSentMessages()
    }
}
