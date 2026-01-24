package com.exa.android.letstalk.domain

import com.google.firebase.Timestamp

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