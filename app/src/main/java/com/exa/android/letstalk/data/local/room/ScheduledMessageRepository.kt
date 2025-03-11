package com.exa.android.letstalk.data.local.room

import javax.inject.Inject

class ScheduledMessageRepository @Inject constructor(
    private val db: ScheduleMessageDatabase
) {
    val dao = db.dao
    suspend fun saveScheduledMessage(message: ScheduledMessageEntity) {
        dao.insertScheduledMessage(message)
    }

    suspend fun getMessagesAtTime(time: Long): List<ScheduledMessageEntity> {
        return dao.getMessagesAtTime(time)
    }

    suspend fun getMessageCountAtTime(time: Long): Int {
        return dao.getMessageCountAtTime(time)
    }

    suspend fun removeScheduledMessage(messageId: String) {
        dao.deleteScheduledMessage(messageId)
    }

    suspend fun removeAllMessagesAtTime(time: Long) {
        dao.deleteAllMessagesAtTime(time)
    }
}
