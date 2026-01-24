package com.exa.android.letstalk.presentation.call

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.letstalk.core.utils.showToast
import com.exa.android.letstalk.domain.CallState
import com.exa.android.letstalk.domain.CallType
import com.exa.android.letstalk.presentation.call.screens.ActiveCallScreen
import com.exa.android.letstalk.presentation.call.screens.IncomingCallScreen
import com.exa.android.letstalk.presentation.call.screens.OutgoingCallScreen
import org.webrtc.SurfaceViewRenderer

/**
 * Main call screen that manages different call states
 * Routes to incoming, outgoing, or active call screens based on state
 */
@Composable
fun CallScreen(
    currentUserId: String,
    onCallEnded: () -> Unit,
    callViewModel: CallViewModel = hiltViewModel() // Default for preview, can be overridden
) {
    val callState by callViewModel.callState.collectAsState()
    val isMuted by callViewModel.isMuted.collectAsState()
    val isSpeakerOn by callViewModel.isSpeakerOn.collectAsState()
    val isVideoEnabled by callViewModel.isVideoEnabled.collectAsState()
    val isFrontCamera by callViewModel.isFrontCamera.collectAsState()
    val callDuration by callViewModel.callDuration.collectAsState()

    val context = LocalContext.current
    
    // Create SurfaceViewRenderers for video
    val localRenderer = remember {
        SurfaceViewRenderer(context).apply {
            init(callViewModel.eglBaseContext, null)
            setZOrderMediaOverlay(true)
            setMirror(true)
        }
    }
    
    val remoteRenderer = remember {
        SurfaceViewRenderer(context).apply {
            init(callViewModel.eglBaseContext, null)
        }
    }
    
    // Cleanup renderers
    DisposableEffect(Unit) {
        onDispose {
            localRenderer.release()
            remoteRenderer.release()
        }
    }

    // Permission launcher for camera and microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (!cameraGranted || !audioGranted) {
            showToast(context, "Camera and microphone permissions are required for calls")
            onCallEnded()
        }
    }

    // Request permissions on launch if needed
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    when (val state = callState) {
        is CallState.IncomingCall -> {
            IncomingCallScreen(
                callerName = state.callerName,
                callerImage = state.callerImage,
                isVideoCall = state.callType == CallType.VIDEO,
                onAnswer = {
                    callViewModel.answerCall(localRenderer, remoteRenderer)
                },
                onReject = {
                    callViewModel.rejectCall()
                    onCallEnded()
                }
            )
        }

        is CallState.OutgoingCall -> {
            OutgoingCallScreen(
                receiverName = state.receiverName,
                receiverImage = state.receiverImage,
                isVideoCall = state.callType == CallType.VIDEO,
                onCancel = {
                    callViewModel.endCall(currentUserId)
                    onCallEnded()
                }
            )
        }

        is CallState.ActiveCall -> {
            ActiveCallScreen(
                otherUserName = state.otherUserName,
                otherUserImage = state.otherUserImage,
                isVideoCall = state.callType == CallType.VIDEO,
                isMuted = isMuted,
                isSpeakerOn = isSpeakerOn,
                isVideoEnabled = isVideoEnabled,
                isFrontCamera = isFrontCamera,
                callDuration = callDuration,
                remoteRenderer = remoteRenderer,
                localRenderer = localRenderer,
                onMuteToggle = { callViewModel.toggleMute() },
                onSpeakerToggle = { callViewModel.toggleSpeaker() },
                onVideoToggle = { callViewModel.toggleVideo() },
                onCameraSwitch = { callViewModel.switchCamera() },
                onEndCall = {
                    callViewModel.endCall(currentUserId)
                    onCallEnded()
                }
            )
        }

        is CallState.CallEnded -> {
            LaunchedEffect(Unit) {
                showToast(context, state.reason)
                onCallEnded()
            }
        }

        is CallState.Error -> {
            LaunchedEffect(Unit) {
                showToast(context, state.message)
                onCallEnded()
            }
        }


        CallState.Idle -> {
            // Show loading while waiting for state update
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

/**
 * Helper composable to initiate a call
 * This should be called from chat screen or contacts screen
 */
@Composable
private fun InitiateCall(
    currentUserId: String,
    receiverId: String,
    receiverName: String,
    receiverImage: String?,
    callType: CallType,
    localRenderer: SurfaceViewRenderer?,
    remoteRenderer: SurfaceViewRenderer?,
    callViewModel: CallViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        callViewModel.initiateCall(
            callerId = currentUserId,
            receiverId = receiverId,
            receiverName = receiverName,
            receiverImage = receiverImage,
            callType = callType,
            localRenderer = localRenderer,
            remoteRenderer = remoteRenderer
        )
    }
}