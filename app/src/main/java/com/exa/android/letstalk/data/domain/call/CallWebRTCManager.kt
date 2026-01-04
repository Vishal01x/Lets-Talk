package com.exa.android.letstalk.data.domain.call

import android.content.Context
import android.util.Log
import com.exa.android.letstalk.data.domain.call.models.IceCandidate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.webrtc.*

/**
 * Manages WebRTC PeerConnection for voice and video calls
 * Handles media streams, ICE candidates, and SDP offer/answer exchange
 */
class CallWebRTCManager(
    private val context: Context
) {
    private val TAG = "WEBRTC_CALL"

    // EglBase for renderer initialization
    private val eglBase: EglBase by lazy { EglBase.create() }
    val eglBaseContext: EglBase.Context
        get() = eglBase.eglBaseContext

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null

    // Callback for remote video stream
    private var onRemoteStreamCallback: ((MediaStream) -> Unit)? = null

    private val _iceCandidateFlow = MutableSharedFlow<IceCandidate>()
    val iceCandidateFlow: SharedFlow<IceCandidate> = _iceCandidateFlow

    private val _connectionStateFlow = MutableSharedFlow<PeerConnection.PeerConnectionState>()
    val connectionStateFlow: SharedFlow<PeerConnection.PeerConnectionState> = _connectionStateFlow

    private var renegotiationCallback: (() -> Unit)? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Default)



    // ICE servers configuration (Google's public STUN servers)
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
    )

    init {
        initializePeerConnectionFactory()
    }

    /**
     * Initialize PeerConnectionFactory - must be called before creating connections
     */
    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()

        PeerConnectionFactory.initialize(options)

        // Use shared EglBase for encoder and decoder
        val encoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext,
            true,
            true
        )

        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setOptions(PeerConnectionFactory.Options().apply {
                disableNetworkMonitor = false
            })
            .createPeerConnectionFactory()

        Log.d(TAG, "PeerConnectionFactory initialized")
    }

    /**
     * Create a new peer connection
     */
    fun createPeerConnection(
        onRemoteStream: (MediaStream) -> Unit
    ): Boolean {
        return try {
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            }

            val observer = object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: org.webrtc.IceCandidate?) {
                    candidate?.let {
                        Log.d(TAG, "New ICE candidate: ${it.sdp}")
                        coroutineScope.launch {
                            _iceCandidateFlow.emit(
                                IceCandidate(
                                    sdpMid = it.sdpMid ?: "",
                                    sdpMLineIndex = it.sdpMLineIndex,
                                    sdp = it.sdp
                                )
                            )
                        }
                    }
                }

                override fun onAddStream(stream: MediaStream?) {
                    Log.d(TAG, "📺 Remote stream received")
                    stream?.let { 
                        onRemoteStream(it)
                        onRemoteStreamCallback?.invoke(it)
                    }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    Log.d(TAG, "Connection state: $newState")
                    newState?.let {
                        coroutineScope.launch {
                            _connectionStateFlow.emit(it)
                        }
                    }
                }

                override fun onDataChannel(p0: DataChannel?) {}
                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    Log.d(TAG, "ICE connection state: $p0")
                }
                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                    Log.d(TAG, "ICE gathering state: $p0")
                }
                override fun onRemoveStream(p0: MediaStream?) {}
                override fun onRenegotiationNeeded() {
                    Log.d(TAG, "Renegotiation needed")
                    renegotiationCallback?.invoke()
                }
                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
                override fun onIceCandidatesRemoved(p0: Array<out org.webrtc.IceCandidate>?) {}
                override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
            }

            peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, observer)
            peerConnection != null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create peer connection", e)
            false
        }
    }

    /**
     * Initialize local media tracks (audio and optionally video)
     */
    fun initializeLocalMedia(enableVideo: Boolean, surfaceViewRenderer: SurfaceViewRenderer? = null) {
        // Create audio track
        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
        }

        val audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("local_audio", audioSource)

        // Create video track if needed
        if (enableVideo && surfaceViewRenderer != null) {
            videoCapturer = createCameraCapturer()
            val videoSource = peerConnectionFactory?.createVideoSource(videoCapturer?.isScreencast ?: false)
            videoCapturer?.initialize(
                SurfaceTextureHelper.create("CameraThread", eglBase.eglBaseContext),
                context,
                videoSource?.capturerObserver
            )
            videoCapturer?.startCapture(1280, 720, 30)

            localVideoTrack = peerConnectionFactory?.createVideoTrack("local_video", videoSource)
            localVideoTrack?.addSink(surfaceViewRenderer)
        }

        // Add tracks to peer connection
        // Add audio track
        localAudioTrack?.let { audioTrack ->
            peerConnection?.addTrack(
                audioTrack,
                listOf("audio_stream")
            )
        }

        // Add video track (if enabled)
        if (enableVideo) {
            localVideoTrack?.let { videoTrack ->
                peerConnection?.addTrack(
                    videoTrack,
                    listOf("video_stream")
                )
            }
        }

        Log.d(TAG, "Local tracks added using Unified Plan")

        Log.d(TAG, "Local media initialized (video: $enableVideo)")
    }

    fun setOnRenegotiationNeeded(callback: () -> Unit) {
        renegotiationCallback = callback
    }

    /**
     * Create camera capturer (front camera by default)
     */
    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames

        // Try front camera first
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) {
                    return capturer
                }
            }
        }

        // Fall back to back camera
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) {
                    return capturer
                }
            }
        }

        return null
    }

    /**
     * Create SDP offer
     */
    suspend fun createOffer(): String? {
        val constraints = MediaConstraints().apply {
//            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
//            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

            optional.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            optional.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        }

        return try {
            val sdp = peerConnection?.createOffer(constraints)
            peerConnection?.setLocalDescription(sdp)
            sdp?.description
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create offer", e)
            null
        }
    }

    /**
     * Create SDP answer
     */
    suspend fun createAnswer(): String? {
        val constraints = MediaConstraints()

        return try {
            val sdp = peerConnection?.createAnswer(constraints)
            peerConnection?.setLocalDescription(sdp)
            sdp?.description
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create answer", e)
            null
        }
    }

    /**
     * Set remote SDP (offer or answer)
     */
    fun setRemoteDescription(sdp: String, type: SessionDescription.Type) {
        try {
            val sessionDescription = SessionDescription(type, sdp)
            peerConnection?.setRemoteDescription(sessionDescription)
            Log.d(TAG, "Remote description set: $type")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set remote description", e)
        }
    }

    /**
     * Add ICE candidate received from remote peer
     */
    fun addIceCandidate(iceCandidate: IceCandidate) {
        try {
            val candidate = org.webrtc.IceCandidate(
                iceCandidate.sdpMid,
                iceCandidate.sdpMLineIndex,
                iceCandidate.sdp
            )
            peerConnection?.addIceCandidate(candidate)
            Log.d(TAG, "ICE candidate added")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add ICE candidate", e)
        }
    }

    /**
     * Toggle microphone mute
     */
    fun toggleMicrophone(mute: Boolean) {
        localAudioTrack?.setEnabled(!mute)
    }

    /**
     * Toggle video
     */
    fun toggleVideo(enable: Boolean) {
        localVideoTrack?.setEnabled(enable)
    }

    /**
     * Switch camera (front/back)
     */
    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }

    /**
     * Set callback for remote stream
     */
    fun setRemoteStreamCallback(callback: (MediaStream) -> Unit) {
        onRemoteStreamCallback = callback
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
        peerConnection?.close()
        peerConnection?.dispose()
        peerConnection = null

        Log.d(TAG, "WebRTC resources cleaned up")
    }
}

// Extension function for synchronous SDP creation
private suspend fun PeerConnection.createOffer(constraints: MediaConstraints): SessionDescription? {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                continuation.resumeWith(Result.success(sdp))
            }

            override fun onCreateFailure(error: String?) {
                continuation.resumeWith(Result.failure(Exception(error ?: "Unknown error")))
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }
}

private suspend fun PeerConnection.createAnswer(constraints: MediaConstraints): SessionDescription? {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                continuation.resumeWith(Result.success(sdp))
            }

            override fun onCreateFailure(error: String?) {
                continuation.resumeWith(Result.failure(Exception(error ?: "Unknown error")))
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }
}

private fun PeerConnection.setLocalDescription(sdp: SessionDescription?) {
    setLocalDescription(object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }, sdp)
}

private fun PeerConnection.setRemoteDescription(sdp: SessionDescription) {
    setRemoteDescription(object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }, sdp)
}