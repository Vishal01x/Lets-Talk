package com.exa.android.letstalk.utils.models

import android.telecom.InCallService.VideoCall
import com.google.firebase.Timestamp

data class Call(
    val callId : Long? = System.currentTimeMillis(),
    val callerId : String = "",
    val receiverId : String = "",
    val timestamp: Long? = System.currentTimeMillis(),
    val isVideoCall : CallType = CallType.VIDEO
)

enum class CallType{
    VOICE, VIDEO
}