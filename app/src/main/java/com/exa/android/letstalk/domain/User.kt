package com.exa.android.letstalk.domain

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


data class UserStatus(
    val userId: String = "",
    val content: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "", // "TEXT", "IMAGE", "VIDEO", "LINK"
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val statusId: String = UUID.randomUUID().toString()
)
