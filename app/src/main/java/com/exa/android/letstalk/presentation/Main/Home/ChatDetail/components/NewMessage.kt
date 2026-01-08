package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exa.android.letstalk.R
import com.exa.android.letstalk.data.local.room.ScheduledMessageViewModel
import com.exa.android.letstalk.data.domain.main.ViewModel.UserViewModel
import com.exa.android.letstalk.utils.helperFun.AudioWaveForm
import com.exa.android.letstalk.utils.helperFun.getUserIdFromChatId
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.ScheduleType
import com.exa.android.letstalk.utils.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun NewMessageSection(
    isGroup: Boolean,
    replyTo: Message?,
    members: List<User?>,
    curUser: String,
    typingTo: String,
    userViewModel: UserViewModel,
    scheduledMessageViewModel: ScheduledMessageViewModel,
    focusRequester: FocusRequester,
    onTextMessageSend: (String, Message?) -> Unit,
    onRecordingSend: () -> Unit,
    onAddClick: () -> Unit,
    onClockClick: () -> Unit,
    onSendOrDiscard: () -> Unit,
    onDone: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf("00:00") }
    var elapsedSeconds by remember { mutableStateOf(0) } // Tracks total elapsed seconds
    var timerJob by remember { mutableStateOf<Job?>(null) }
    val isMessageScheduled by scheduledMessageViewModel.scheduleMessageType.collectAsState()

    fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(1000L)
                elapsedSeconds++
                recordingTime =
                    String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun resetTimer() {
        isRecording = false
        isPaused = false
        recordingTime = "00:00"
        elapsedSeconds = 0
        timerJob?.cancel()
    }

    // Timer logic
    LaunchedEffect(isRecording, isPaused) {
        if (isRecording) {
            if (!isPaused) {
                startTimer()
            } else {
                pauseTimer()
            }
        } else {
            resetTimer()
        }
    }

    if (!isRecording) {
        // Text Input UI
        SendTFMessage(
            curUser = curUser,
            replyTo = replyTo,
            members = members,
            focusRequester = focusRequester,
            isMessageScheduled = isMessageScheduled != ScheduleType.NONE,
            onSendClick = { message, replyTo ->
                onSendOrDiscard()
                onTextMessageSend(message, replyTo)
            },
            onClockClick = onClockClick,
            onAddClick = onAddClick,
            onMicClick = { isRecording = true },
            onTyping = { message ->
                if (message.isEmpty()) {
                    if (!isGroup)
                        userViewModel.setTypingStatus(curUser, "")
                    else userViewModel.setTypingStatus(typingTo, "") // here typing to is groupChatId
                } else if (message.isNotEmpty()) {
                    if (!isGroup)
                        userViewModel.setTypingStatus(curUser, getUserIdFromChatId(typingTo, curUser))
                    else
                        userViewModel.setTypingStatus(
                            typingTo,
                            curUser
                        ) // for group in chatID set curUser typing
                    userViewModel.setTypingStatus(curUser, typingTo)
                }
            },
            onDiscardReply = { onSendOrDiscard() },
            onDone = { onDone() }
        )
    } else {
        // Audio Recording UI
        SendAudioMessage(
            isPaused = isPaused,
            recordingTime = recordingTime,
            onDeleteRecording = {
                resetTimer()
            },
            onPauseResumeRecording = {
                isPaused = !isPaused
            },
            onSendRecording = {
                onRecordingSend()
                resetTimer()
            }
        )
    }
}


@Composable
fun SendTFMessage(
    curUser: String,
    replyTo: Message?,
    members: List<User?>,
    focusRequester: FocusRequester,
    isMessageScheduled: Boolean,
    onSendClick: (String, Message?) -> Unit,
    onClockClick: () -> Unit,
    onAddClick: () -> Unit,
    onMicClick: () -> Unit,
    onTyping: (msg: String) -> Unit,
    onDiscardReply: () -> Unit,
    onDone: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        shape = RectangleShape,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Column {
            if (replyTo != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ReplyUi(curUser, replyTo, members, true) {
                        onDiscardReply()
                        onDone()
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Button
                IconButton(onClick = onAddClick) {
                    androidx.compose.material.Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onClockClick) {
                    androidx.compose.material.Icon(
                        painter = if(isMessageScheduled)painterResource(R.drawable.alarm_dark)else painterResource(R.drawable.alarm_light),
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Light grey background
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (message.isEmpty()) {
                        Text(
                            text = "Type a Message",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    BasicTextField(
                        value = message,
                        onValueChange = { message = it; onTyping(message) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val text = message.trim()
                                if (text.isNotEmpty())
                                    onSendClick(text, replyTo)
                                message = ""
                                onTyping(message)
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Microphone or Send Button
                IconButton(
                    onClick = {
                        if (message.isNotEmpty()) {
                            onSendClick(message, replyTo)
                            message = ""
                            onTyping(message)
                        } else {
                            onMicClick()
                        }
                    }
                ) {
                    androidx.compose.material.Icon(
                        painter = painterResource(if (message.isEmpty()) R.drawable.microphone else R.drawable.send),
                        contentDescription = "Send or Mic",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SendAudioMessage(
//    isRecording: Boolean = true,
    isPaused: Boolean,
    recordingTime: String,
    onDeleteRecording: () -> Unit,
    onPauseResumeRecording: () -> Unit,
    onSendRecording: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Button
            IconButton(onClick = onDeleteRecording) {
                androidx.compose.material.Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Recording",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Timer
            Text(
                text = recordingTime,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Sound Wave Animation
            if (!isPaused) {
                AudioWaveForm(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    isPaused = isPaused
                )
            } else {
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(Color.Gray)
                )
            }
            // Pause/Resume Button
            IconButton(onClick = onPauseResumeRecording) {
                androidx.compose.material.Icon(
                    painter = painterResource(if (isPaused) R.drawable.play else R.drawable.pause),
                    contentDescription = "Pause/Resume Recording",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Send Button
            IconButton(onClick = onSendRecording) {
                androidx.compose.material.Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Recording",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ReplyUi(
    curUser: String,
    replyTo: Message,
    members: List<User?>,
    showCross: Boolean = false,
    onDiscard: (() -> Unit)? = null
) {

    val user = members.find { it?.userId == replyTo.senderId }

    // making onDiscard nullable because we pass it form newMessage only not from messageBubble
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (showCross) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceEvenly) {
            (if(user?.userId != curUser)user?.name else "You")?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = replyTo.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showCross) {
            Box(modifier = Modifier
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
                .clickable {
                    if (onDiscard != null) {
                        onDiscard()
                    }
                }
                .padding(2.dp)
                .align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Discard Reply",
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

}
