package com.exa.android.letstalk.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exa.android.letstalk.utils.models.Message
import com.google.firebase.Timestamp
import java.util.UUID

// ScheduledMessageEntity.kt
@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey
    val messageId: String = UUID.randomUUID().toString(),
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    var scheduledTime: Long = 0,
    val status: String = "scheduled",
    val replyTo: Message? = null,
    val members: List<String?> = emptyList(),
    val recipientName: String? = null,  // Added for UI
    val profileImageUri: String? = null  // Added for UI
)

fun Message.toEntity(recipientName: String, profileImageUri: String): ScheduledMessageEntity {
    return ScheduledMessageEntity(
        messageId = messageId,
        chatId = chatId,
        senderId = senderId,
        receiverId = receiverId,
        message = message,
        replyTo = replyTo,
        members = members,
        recipientName = recipientName,
        profileImageUri = profileImageUri
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