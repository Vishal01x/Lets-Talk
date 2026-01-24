package com.exa.android.letstalk.domain

import com.google.firebase.Timestamp
import java.util.UUID

data class Message(
    val messageId: String = UUID.randomUUID().toString(),
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val media: Media? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "sent", // Status could be "sent", "delivered", or "read"
    val replyTo: Message? = null,
    val members: List<String?> = emptyList(),
    // Signal E2EE fields
    val senderDeviceId: Int = 0,
    val receiverDeviceId: Int = 0,
    val ciphertextType: Int = 0 // 1 = PreKeySignalMessage, 2 = SignalMessage, 0 = not encrypted
)

data class Media(
    var mediaType: MediaType = MediaType.IMAGE,
    var mediaUrl: String = "",
)

data class Status(
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val typingTo: String = ""
)

enum class MediaType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, LOCATION, CONTACT
}

data class PriorityMessage(
    val message: Message = Message(),
    //val chat: Chat = Chat(),
    val media: Media? = null
)