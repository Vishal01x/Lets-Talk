package com.exa.android.letstalk.data.domain.call.models

import com.google.firebase.Timestamp

/**
 * Represents a WebRTC call offer sent via Firestore signaling
 */
data class CallOffer(
    val callId: String = "",
    val callerId: String = "",
    val receiverId: String = "",
    val sdpOffer: String = "", // Session Description Protocol offer
    val callType: CallType = CallType.VOICE,
    val timestamp: Timestamp = Timestamp.now(),
    val status: CallStatus = CallStatus.RINGING
)

/**
 * Represents a WebRTC call answer sent via Firestore signaling
 */
data class CallAnswer(
    val callId: String = "",
    val sdpAnswer: String = "", // Session Description Protocol answer
    val timestamp: Timestamp = Timestamp.now(),
    val accepted: Boolean = false
)

/**
 * Represents an ICE (Interactive Connectivity Establishment) candidate
 * for WebRTC peer connection
 */
data class IceCandidate(
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val sdp: String = "" // The actual ICE candidate string
)

/**
 * Collection of ICE candidates for a call participant
 */
data class IceCandidateData(
    val callId: String = "",
    val userId: String = "", // ID of the user who generated this candidate
    val candidates: List<IceCandidate> = emptyList()
)

/**
 * Type of call - voice or video
 */
enum class CallType {
    VOICE,
    VIDEO
}

/**
 * Current status of the call
 */
enum class CallStatus {
    RINGING,      // Call initiated, waiting for answer
    CONNECTING,   // Answer received, establishing connection
    CONNECTED,    // Peer connection established
    ENDED,        // Call ended normally
    REJECTED,     // Call rejected by receiver
    MISSED,       // Call not answered within timeout
    FAILED,       // Call failed due to error
    BUSY          // Receiver already in another call
}

/**
 * UI state for call screens - sealed class for type safety
 */
sealed class CallState {
    object Idle : CallState()
    
    data class OutgoingCall(
        val callId: String,
        val receiverId: String,
        val receiverName: String,
        val receiverImage: String?,
        val callType: CallType
    ) : CallState()
    
    data class IncomingCall(
        val callId: String,
        val callerId: String,
        val callerName: String,
        val callerImage: String?,
        val callType: CallType
    ) : CallState()
    
    data class ActiveCall(
        val callId: String,
        val otherUserId: String,
        val otherUserName: String,
        val otherUserImage: String?,
        val callType: CallType,
        val isMuted: Boolean = false,
        val isSpeakerOn: Boolean = false,
        val isVideoEnabled: Boolean = true,
        val isFrontCamera: Boolean = true,
        val durationSeconds: Int = 0
    ) : CallState()
    
    data class CallEnded(
        val reason: String,
        val duration: Int = 0
    ) : CallState()
    
    data class Error(
        val message: String
    ) : CallState()
}

/**
 * Call event for Firestore listeners
 */
data class CallEvent(
    val callOffer: CallOffer? = null,
    val callAnswer: CallAnswer? = null,
    val iceCandidates: List<IceCandidate>? = null,
    val endedBy: String? = null
)
