package com.exa.android.letstalk.presentation.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.core.utils.Response
import com.exa.android.letstalk.data.repository.CallSignalingRepository
import com.exa.android.letstalk.data.repository.UserRepository
import com.exa.android.letstalk.data.usecase.AnswerCallUseCase
import com.exa.android.letstalk.data.usecase.EndCallUseCase
import com.exa.android.letstalk.data.usecase.InitiateCallUseCase
import com.exa.android.letstalk.data.usecase.ObserveIncomingCallsUseCase
import com.exa.android.letstalk.data.usecase.RejectCallUseCase
import com.exa.android.letstalk.data.webrtc.CallRingtoneManager
import com.exa.android.letstalk.data.webrtc.CallWebRTCManager
import com.exa.android.letstalk.domain.CallState
import com.exa.android.letstalk.domain.CallStatus
import com.exa.android.letstalk.domain.CallType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.webrtc.MediaStream
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

/**
 * ViewModel for managing call state and coordinating call operations
 */
@HiltViewModel
class CallViewModel @Inject constructor(
    private val initiateCallUseCase: InitiateCallUseCase,
    private val answerCallUseCase: AnswerCallUseCase,
    private val endCallUseCase: EndCallUseCase,
    private val rejectCallUseCase: RejectCallUseCase,
    private val observeIncomingCallsUseCase: ObserveIncomingCallsUseCase,
    private val webRTCManager: CallWebRTCManager,
    private val signalingRepository: CallSignalingRepository,
    private val ringtoneManager: CallRingtoneManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val TAG = "WEBRTC_CALL"

    // Expose EglBase context for SurfaceViewRenderer initialization
    val eglBaseContext: org.webrtc.EglBase.Context
        get() = webRTCManager.eglBaseContext

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _remoteStream = MutableStateFlow<MediaStream?>(null)
    val remoteStream: StateFlow<MediaStream?> = _remoteStream.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _callDuration = MutableStateFlow(0)
    val callDuration: StateFlow<Int> = _callDuration.asStateFlow()

    private var callId: String? = null
    private var currentUserId : String? = null
    private var incomingCallListenerJob: Job? = null
    private var iceCandidateJob: Job? = null
    private var callAnswerJob: Job? = null
    private var durationTimerJob: Job? = null

    /**
     * Start observing incoming calls for the current user
     */
    fun startObservingIncomingCalls(userId: String) {
        //incomingCallListenerJob?.cancel()
        Log.d(TAG, "👂 [RECEIVER] Starting to observe incoming calls for: $userId")

        // Store current user ID for later use (ICE candidates, etc.)
        currentUserId = userId

        incomingCallListenerJob = viewModelScope.launch(Dispatchers.IO) {
            observeIncomingCallsUseCase(userId).collect { callOffer ->
                Log.d(TAG, "🆕 [RECEIVER] New call detected: ${callOffer.callId}")

                callId = callOffer.callId

                // Start ringtone
                ringtoneManager.startRingtone()

                // Fetch caller info from UserRepository
                Log.d(TAG, "🔍 [RECEIVER] Attempting to fetch caller with ID: ${callOffer.callerId}")

                val callerUserResponse = try {
                    // Wait for Success or Error response, skip Loading
                    userRepository.getUserProfile(callOffer.callerId)
                        .firstOrNull { it is Response.Success || it is Response.Error }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ [RECEIVER] Exception fetching user: ${e.message}", e)
                    null
                }

                Log.d(TAG, "📦 [RECEIVER] Response type: ${callerUserResponse?.javaClass?.simpleName}")

                val callerUser = when (callerUserResponse) {
                    is Response.Success -> {
                        Log.d(TAG, "✅ [RECEIVER] Success! User: ${callerUserResponse.data?.name}")
                        callerUserResponse.data
                    }
                    is Response.Error -> {
                        Log.e(TAG, "❌ [RECEIVER] Error: ${callerUserResponse.message}")
                        null
                    }
                    else -> {
                        Log.w(TAG, "⚠️ [RECEIVER] Unknown response type or null")
                        null
                    }
                }

                Log.d(TAG, "👤 [RECEIVER] Final caller: name=${callerUser?.name}, image=${callerUser?.profilePicture}")

                // Update UI state with actual caller info
                _callState.value = CallState.IncomingCall(
                    callId = callOffer.callId,
                    callerId = callOffer.callerId,
                    callerName = callerUser?.name ?: "Unknown User",
                    callerImage = callerUser?.profilePicture,
                    callType = callOffer.callType
                )
            }
        }
    }

    /**
     * Stop observing incoming calls
     */
    fun stopObservingIncomingCalls() {
        incomingCallListenerJob?.cancel()
        incomingCallListenerJob = null
    }

    /**
     * Initiate an outgoing call
     */
    fun initiateCall(
        callerId: String,
        receiverId: String,
        receiverName: String,
        receiverImage: String?,
        callType: CallType,
        localRenderer: SurfaceViewRenderer?,
        remoteRenderer: SurfaceViewRenderer?
    ) {
        Log.d(TAG, "📞 [CALLER] Initiating call to: $receiverId")

        currentUserId = callerId
        
        // Set remote stream callback
        remoteRenderer?.let { renderer ->
            webRTCManager.setRemoteStreamCallback { stream ->
                stream.videoTracks.firstOrNull()?.addSink(renderer)
            }
        }

        // Set OutgoingCall state IMMEDIATELY (synchronously) so UI updates right away
        _callState.value = CallState.OutgoingCall(
            callId = "", // Will be updated after call is created
            receiverId = receiverId,
            receiverName = receiverName,
            receiverImage = receiverImage,
            callType = callType
        )
        Log.d(TAG, "✅ [CALLER] State set to OutgoingCall (synchronous)")

        viewModelScope.launch(Dispatchers.IO) {
            // Check if receiver is already in a call
//            val receiverBusy = signalingRepository.isUserInCall(receiverId)
//            if (receiverBusy) {
//                Log.w(TAG, "⚠️ Receiver is already in another call")
//                _callState.value = CallState.Error("User is already in another call")
//                return@launch
//            }


            val result = initiateCallUseCase(
                callerId = callerId,
                receiverId = receiverId,
                callType = callType,
                localRenderer = localRenderer
            )

            result.onSuccess { generatedCallId ->
                this@CallViewModel.callId = generatedCallId

                // Update state with actual callId
                _callState.value = CallState.OutgoingCall(
                    callId = generatedCallId,
                    receiverId = receiverId,
                    receiverName = receiverName,
                    receiverImage = receiverImage,
                    callType = callType
                )

                // Start observing ICE candidates from remote peer
                observeRemoteIceCandidates(generatedCallId, receiverId)

                // Start observing call answer
                observeCallAnswer(generatedCallId, receiverId, receiverName, receiverImage, callType)

                Log.d(TAG, "Call initiated: $generatedCallId")
            }.onFailure { error ->
                Log.e(TAG, "Failed to initiate call", error)
                _callState.value = CallState.Error(error.message ?: "Failed to initiate call")
            }

            // Start sending ICE candidates
            collectAndSendIceCandidates(callerId)
        }
    }

    /**
     * Answer an incoming call
     */
    fun answerCall(localRenderer: SurfaceViewRenderer?, remoteRenderer: SurfaceViewRenderer?) {
        val currentState = _callState.value as? CallState.IncomingCall ?: return
        val currentCallId = callId ?: return
        
        // Set remote stream callback
        remoteRenderer?.let { renderer ->
            webRTCManager.setRemoteStreamCallback { stream ->
                stream.videoTracks.firstOrNull()?.addSink(renderer)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Stop ringtone (but keep listener active for future calls)
            ringtoneManager.stopRingtone()

            val result = answerCallUseCase(currentCallId, localRenderer)

            result.onSuccess {
                _callState.value = CallState.ActiveCall(
                    callId = currentCallId,
                    otherUserId = currentState.callerId,
                    otherUserName = currentState.callerName,
                    otherUserImage = currentState.callerImage,
                    callType = currentState.callType,
                    isVideoEnabled = currentState.callType == CallType.VIDEO
                )

                // Start observing ICE candidates from caller
                observeRemoteIceCandidates(currentCallId, currentState.callerId)

                // Start call duration timer
                startCallDurationTimer()

                // Start sending ICE candidates
                val userId = currentUserId
                if (userId != null) {
                    collectAndSendIceCandidates(userId)
                } else {
                    Log.e(TAG, "⚠️ Cannot send ICE candidates: currentUserId is null")
                }

                Log.d(TAG, "Call answered: $currentCallId")
            }.onFailure { error ->
                Log.e(TAG, "Failed to answer call", error)
                _callState.value = CallState.Error(error.message ?: "Failed to answer call")
            }
        }
    }

    /**
     * Reject an incoming call
     */
    fun rejectCall() {
        val currentCallId = callId ?: return

        viewModelScope.launch(Dispatchers.IO) {
            ringtoneManager.stopRingtone()

            rejectCallUseCase(currentCallId).onSuccess {
                _callState.value = CallState.Idle
                callId = null
                Log.d(TAG, "Call rejected: $currentCallId")
            }.onFailure { error ->
                Log.e(TAG, "Failed to reject call", error)
            }
        }
    }

    /**
     * End an active call
     */
    fun endCall(userId: String) {
        val currentCallId = callId ?: return

        viewModelScope.launch(Dispatchers.IO) {
            endCallUseCase(currentCallId, userId).onSuccess {
                _callState.value = CallState.CallEnded(
                    reason = "Call ended",
                    duration = _callDuration.value
                )

                stopCallDurationTimer()
                cleanup()

                Log.d(TAG, "Call ended: $currentCallId")
            }.onFailure { error ->
                Log.e(TAG, "Failed to end call", error)
            }
        }
    }

    /**
     * Toggle microphone mute
     */
    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        webRTCManager.toggleMicrophone(_isMuted.value)
    }

    /**
     * Toggle speaker
     */
    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
        // TODO: Implement speaker toggle via AudioManager
    }

    /**
     * Toggle video on/off
     */
    fun toggleVideo() {
        _isVideoEnabled.value = !_isVideoEnabled.value
        webRTCManager.toggleVideo(_isVideoEnabled.value)
    }

    /**
     * Switch camera (front/back)
     */
    fun switchCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
        webRTCManager.switchCamera()
    }

    /**
     * Observe call answer from receiver
     */
    private fun observeCallAnswer(
        callId: String,
        receiverId: String,
        receiverName: String,
        receiverImage: String?,
        callType: CallType
    ) {
        //callAnswerJob?.cancel()

        callAnswerJob = viewModelScope.launch(Dispatchers.IO) {
            signalingRepository.observeCallAnswer(callId).collect { answer ->
                answer?.let {
                    if (it.accepted) {
                        // Set remote SDP answer
                        webRTCManager.setRemoteDescription(
                            it.sdpAnswer,
                            org.webrtc.SessionDescription.Type.ANSWER
                        )

                        signalingRepository.updateCallStatus(callId, CallStatus.CONNECTED)

                        _callState.value = CallState.ActiveCall(
                            callId = callId,
                            otherUserId = receiverId,
                            otherUserName = receiverName,
                            otherUserImage = receiverImage,
                            callType = callType,
                            isVideoEnabled = callType == CallType.VIDEO
                        )

                        startCallDurationTimer()
                        Log.d(TAG, "Call answered by receiver")
                    }
                }
            }
        }
    }

    /**
     * Observe and handle remote ICE candidates
     */
    private fun observeRemoteIceCandidates(callId: String, remotePeerId: String) {

        iceCandidateJob?.cancel()

        iceCandidateJob = viewModelScope.launch(Dispatchers.IO) {
            signalingRepository.observeIceCandidates(callId, remotePeerId).collect { candidate ->
                webRTCManager.addIceCandidate(candidate)
                Log.d(TAG, "Remote ICE candidate added")
            }
        }
    }

    /**
     * Collect and send local ICE candidates to Firestore
     */
    private fun collectAndSendIceCandidates(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            webRTCManager.iceCandidateFlow.collect { candidate ->
                callId?.let { id ->
                    signalingRepository.addIceCandidate(id, userId, candidate)
                    Log.d(TAG, "Local ICE candidate sent to Firestore")
                }
            }
        }
    }

    /**
     * Start call duration timer
     */
    private fun startCallDurationTimer() {
        stopCallDurationTimer()

        durationTimerJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                _callDuration.value += 1
            }
        }
    }

    /**
     * Stop call duration timer
     */
    private fun stopCallDurationTimer() {
        durationTimerJob?.cancel()
        _callDuration.value = 0
    }

    /**
     * Cleanup resources
     */
    private fun cleanup() {
        ringtoneManager.stopRingtone()
        iceCandidateJob?.cancel()
        callAnswerJob?.cancel()
        // DON'T cancel incomingCallListenerJob - keep it active for future calls!
        callId = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
        stopObservingIncomingCalls()
    }
}