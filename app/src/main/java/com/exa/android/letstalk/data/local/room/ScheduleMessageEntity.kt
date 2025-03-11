package com.exa.android.letstalk.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exa.android.letstalk.utils.models.Message
import com.google.firebase.Timestamp
import java.util.UUID

@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey
    val messageId: String = UUID.randomUUID().toString(),
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    var scheduledTime:  Long = 0,
    val status: String = "scheduled", // Status could be "sent", "delivered", or "read"
    val replyTo: Message? = null, // store this as a JSON string using type convertor
    val members: List<String?> = emptyList() // since
)


fun Message.toEntity(): ScheduledMessageEntity {
    return ScheduledMessageEntity(
        messageId = messageId,
        chatId = chatId,
        senderId = senderId,
        receiverId = receiverId,
        message = message,
        replyTo = replyTo,
        members = members
    )
}


fun ScheduledMessageEntity.toMessage(): Message {
    return Message(
        messageId = messageId,
        chatId = chatId,
        senderId = senderId,
        receiverId = receiverId,
        message = message,
        replyTo = replyTo,
        members = members
    )
}