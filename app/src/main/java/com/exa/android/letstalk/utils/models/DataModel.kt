package com.exa.android.letstalk.utils.models

import com.google.firebase.Timestamp
import java.util.UUID

data class User(
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val profilePicture: String? = ""
)


data class Message(
    val messageId: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "sent", // Status could be "sent", "delivered", or "read"
    val replyTo: Message? = null,
    val members: List<String?> = emptyList()
)

data class Chat(
    var id: String = "",
    val name: String = "",
    val profilePicture: String? = "",
    val group : Boolean = false,
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastMessageCnt : Long = 0,
    var unreadMessages : Long = 0
)

data class Status(
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val typingTo: String = ""
)