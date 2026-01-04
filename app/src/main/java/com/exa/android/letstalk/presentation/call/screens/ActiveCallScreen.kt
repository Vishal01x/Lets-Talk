package com.exa.android.letstalk.presentation.call.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.exa.android.letstalk.presentation.Main.components.CircularUserImage
import org.webrtc.SurfaceViewRenderer

/**
 * Active call screen for voice and video calls
 * Shows remote video (if video call), local video (floating), and call controls
 */
@Composable
fun ActiveCallScreen(
    otherUserName: String,
    otherUserImage: String?,
    isVideoCall: Boolean,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isVideoEnabled: Boolean,
    isFrontCamera: Boolean,
    callDuration: Int,
    remoteRenderer: SurfaceViewRenderer?,
    localRenderer: SurfaceViewRenderer?,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onCameraSwitch: () -> Unit,
    onEndCall: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main video area (remote video for video calls, user avatar for voice calls)
        if (isVideoCall && remoteRenderer != null) {
            // Remote video
            AndroidView(
                factory = { remoteRenderer },
                modifier = Modifier.fillMaxSize()
            )
            
            // Local video (floating picture-in-picture)
            if (isVideoEnabled && localRenderer != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(120.dp, 160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray)
                ) {
                    AndroidView(
                        factory = { localRenderer },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // Voice call - show user avatar
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularUserImage(
                        imageUrl = otherUserImage,
                        modifier = Modifier.size(140.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = otherUserName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Top bar with call duration and user info
        AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = otherUserName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatDuration(callDuration),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
        
        // Bottom control panel
        AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mute button
                    CallControlButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (isMuted) "Unmute" else "Mute",
                        isActive = isMuted,
                        onClick = onMuteToggle
                    )
                    
                    // Speaker button (for voice calls)
                    if (!isVideoCall) {
                        CallControlButton(
                            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            label = if (isSpeakerOn) "Speaker On" else "Speaker Off",
                            isActive = isSpeakerOn,
                            onClick = onSpeakerToggle
                        )
                    }
                    
                    // Video toggle (for video calls)
                    if (isVideoCall) {
                        CallControlButton(
                            icon = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            label = if (isVideoEnabled) "Video On" else "Video Off",
                            isActive = !isVideoEnabled,
                            onClick = onVideoToggle
                        )
                    }
                    
                    // Camera switch (for video calls)
                    if (isVideoCall && isVideoEnabled) {
                        CallControlButton(
                            icon = Icons.Default.Cameraswitch,
                            label = "Switch",
                            isActive = false,
                            onClick = onCameraSwitch
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // End call button (red, prominent)
                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = Color.Red,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Reusable call control button component
 */
@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color.White.copy(alpha = 0.3f)
                    else Color.White.copy(alpha = 0.1f)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

/**
 * Format call duration in MM:SS format
 */
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
