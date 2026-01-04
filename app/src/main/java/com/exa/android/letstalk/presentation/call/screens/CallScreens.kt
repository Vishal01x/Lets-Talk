package com.exa.android.letstalk.presentation.call.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.exa.android.letstalk.presentation.Main.components.CircularUserImage

/**
 * Incoming call screen with ringtone
 * Shows caller information and answer/reject buttons
 */
@Composable
fun IncomingCallScreen(
    callerName: String,
    callerImage: String?,
    isVideoCall: Boolean,
    onAnswer: () -> Unit,
    onReject: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF0D47A1)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 80.dp)
        ) {
            // Top section - Call type
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVideoCall) "Incoming Video Call" else "Incoming Voice Call",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "LetsTalk",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            
            // Middle section - Caller info with pulsing animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (callerImage != null) {
                        AsyncImage(
                            model = callerImage,
                            contentDescription = "Caller",
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        CircularUserImage(
                            imageUrl = null,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = callerName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Calling...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
            
            // Bottom section - Action buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Reject button
                FloatingActionButton(
                    onClick = onReject,
                    containerColor = Color.Red,
                    modifier = Modifier.size(70.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Reject",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Answer button
                FloatingActionButton(
                    onClick = onAnswer,
                    containerColor = Color(0xFF4CAF50),
                    modifier = Modifier.size(70.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Answer",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Outgoing call screen with ringing animation
 */
@Composable
fun OutgoingCallScreen(
    receiverName: String,
    receiverImage: String?,
    isVideoCall: Boolean,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1565C0),
                        Color(0xFF0D47A1)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 80.dp)
        ) {
            // Top section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVideoCall) "Video Calling..." else "Voice Calling...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Middle section - Receiver info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (receiverImage != null) {
                        AsyncImage(
                            model = receiverImage,
                            contentDescription = "Receiver",
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        CircularUserImage(
                            imageUrl = null,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = receiverName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Ringing...",
                    color = Color.White.copy(alpha = alpha),
                    fontSize = 16.sp
                )
            }
            
            // Bottom section - Cancel button
            FloatingActionButton(
                onClick = onCancel,
                containerColor = Color.Red,
                modifier = Modifier.size(70.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Cancel",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
