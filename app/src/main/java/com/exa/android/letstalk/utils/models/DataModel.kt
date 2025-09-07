package com.exa.android.letstalk.utils.models

import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID

data class User(
    var userId: String = "",
    val name: String = "",
    val phone: String = "",
    var profilePicture: String? = "",
    val about: String = "",
    val birthDate: String = "",
    val socialMedia: String = ""
)

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
    val members: List<String?> = emptyList()
)

data class UserStatus(
    val userId: String = "",
    val content: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "", // "TEXT", "IMAGE", "VIDEO", "LINK"
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val statusId: String = UUID.randomUUID().toString()
)

data class Media(
    var mediaType: MediaType = MediaType.IMAGE,
    var mediaUrl: String = "",
)

data class Chat(
    var id: String = "",
    var name: String = "",
    var profilePicture: String? = "",
    val group: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastMessageCnt: Long = 0,
    var unreadMessages: Long = 0
)

data class PriorityMessage(
    val message: Message = Message(),
    //val chat: Chat = Chat(),
    val media: Media? = null
)


data class Status(
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val typingTo: String = ""
)

enum class MediaType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, LOCATION, CONTACT
}