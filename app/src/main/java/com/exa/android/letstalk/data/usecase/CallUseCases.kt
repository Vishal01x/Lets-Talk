package com.exa.android.letstalk.data.usecase

import android.util.Log
import com.exa.android.letstalk.domain.CallAnswer
import com.exa.android.letstalk.data.repository.CallSignalingRepository
import com.exa.android.letstalk.data.webrtc.CallWebRTCManager
import com.exa.android.letstalk.domain.CallOffer
import com.exa.android.letstalk.domain.CallStatus
import com.exa.android.letstalk.domain.CallType
import com.google.firebase.Timestamp
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

/**
 * Use case for initiating a call
 */
class InitiateCallUseCase @Inject constructor(
    private val webRTCManager: CallWebRTCManager,
    private val signalingRepository: CallSignalingRepository
) {
    suspend operator fun invoke(
        callerId: String,
        receiverId: String,
        callType: CallType,
        localRenderer: SurfaceViewRenderer?
    ): Result<String> {
        return try {
            Log.d("WEBRTC_CALL", "🚀 [CALLER] Starting call initiation")

            // Generate call ID
            val callId = "call_${System.currentTimeMillis()}"
            Log.d("WEBRTC_CALL", "🆔 [CALLER] Call ID generated: $callId")

            // Create peer connection
            val peerCreated = webRTCManager.createPeerConnection { remoteStream ->
                // Remote stream will be handled in the active call screen
            }

            if (!peerCreated) {
                Log.e("WEBRTC_CALL", "❌ [CALLER] Failed to create peer connection")
                return Result.failure(Exception("Failed to create peer connection"))
            }
            Log.d("WEBRTC_CALL", "✅ [CALLER] Peer connection created")

            // Initialize local media
            webRTCManager.initializeLocalMedia(
                enableVideo = callType == CallType.VIDEO,
                surfaceViewRenderer = localRenderer
            )
            Log.d("WEBRTC_CALL", "🎤 [CALLER] Local media initialized (video: ${callType == CallType.VIDEO})")

            // Create SDP offer
            Log.d("WEBRTC_CALL", "📝 [CALLER] Creating SDP offer...")
            val sdpOffer = webRTCManager.createOffer()

            if (sdpOffer == null) {
                Log.e("WEBRTC_CALL", "❌ [CALLER] SDP offer is null")
                return Result.failure(Exception("Failed to create SDP offer"))
            }

            Log.d("WEBRTC_CALL", "✅ [CALLER] SDP offer created (${sdpOffer.length} chars)")

            val callOffer = CallOffer(
                callId = callId,
                callerId = callerId,
                receiverId = receiverId,
                sdpOffer = sdpOffer,
                callType = callType,
                timestamp = Timestamp.now(),
                status = CallStatus.RINGING
            )

            Log.d("WEBRTC_CALL", "💾 [CALLER] Saving to Firestore: $callId -> receiverId: $receiverId")

            // Save to Firestore
            val saveResult = signalingRepository.createCallOffer(callOffer)

            saveResult.onSuccess {
                Log.d("WEBRTC_CALL", "✅ [CALLER] Call saved to Firestore successfully")
            }.onFailure { error ->
                Log.e("WEBRTC_CALL", "❌ [CALLER] Firestore save failed: ${error.message}")
                return Result.failure(error)
            }

            Log.d("WEBRTC_CALL", "🎉 [CALLER] Call initiation completed for $callId")
            Result.success(callId)
        } catch (e: Exception) {
            Log.e("WEBRTC_CALL", "💥 [CALLER] Exception during initiation", e)
            Result.failure(e)
        }
    }
}

/**
 * Use case for answering an incoming call
 */
class AnswerCallUseCase @Inject constructor(
    private val webRTCManager: CallWebRTCManager,
    private val signalingRepository: CallSignalingRepository
) {
    suspend operator fun invoke(
        callId: String,
        localRenderer: SurfaceViewRenderer?
    ): Result<Unit> {
        return try {
            // Get call details
            val callDetailsResult = signalingRepository.getCallDetails(callId)
            val callOffer = callDetailsResult.getOrNull()
                ?: return Result.failure(Exception("Call not found"))

            // Create peer connection
            val peerCreated = webRTCManager.createPeerConnection { remoteStream ->
                // Remote stream will be handled in active call screen
            }

            if (!peerCreated) {
                return Result.failure(Exception("Failed to create peer connection"))
            }

            // Initialize local media
            webRTCManager.initializeLocalMedia(
                enableVideo = callOffer.callType == CallType.VIDEO,
                surfaceViewRenderer = localRenderer
            )

            // Set remote description (the offer)
            webRTCManager.setRemoteDescription(
                callOffer.sdpOffer,
                SessionDescription.Type.OFFER
            )

            // Create SDP answer
            val sdpAnswer = webRTCManager.createAnswer()
                ?: return Result.failure(Exception("Failed to create SDP answer"))

            // Send answer to Firestore
            val answer = CallAnswer(
                callId = callId,
                sdpAnswer = sdpAnswer,
                timestamp = Timestamp.now(),
                accepted = true
            )

            signalingRepository.sendCallAnswer(callId, answer)

            // Update call status
            //signalingRepository.updateCallStatus(callId, CallStatus.CONNECTING)

            signalingRepository.updateCallStatus(callId, CallStatus.CONNECTED)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for ending a call
 */
class EndCallUseCase @Inject constructor(
    private val webRTCManager: CallWebRTCManager,
    private val signalingRepository: CallSignalingRepository
) {
    suspend operator fun invoke(callId: String, userId: String): Result<Unit> {
        return try {
            // Update Firestore
            signalingRepository.endCall(callId, userId)

            // Cleanup WebRTC resources
            webRTCManager.cleanup()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for rejecting a call
 */
class RejectCallUseCase @Inject constructor(
    private val signalingRepository: CallSignalingRepository
) {
    suspend operator fun invoke(callId: String): Result<Unit> {
        return signalingRepository.rejectCall(callId)
    }
}

/**
 * Use case for observing incoming calls
 */
class ObserveIncomingCallsUseCase @Inject constructor(
    private val signalingRepository: CallSignalingRepository
) {
    operator fun invoke(userId: String) = signalingRepository.observeIncomingCalls(userId)
}