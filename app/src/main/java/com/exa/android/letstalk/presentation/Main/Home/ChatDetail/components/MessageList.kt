package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components

import android.os.Build
import android.os.VibrationEffect
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.letstalk.utils.helperFun.formatTimestamp
import com.exa.android.letstalk.utils.helperFun.getVibrator
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.R
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.checkAndOpenOrDownloadMedia
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.docs.DocumentMessageItem
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.downloadMedia
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.getFileNameFromUrl
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.image.ImageMessageContent
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.image.downloadImageToAppFolder
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.image.openImageIntent
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.openFileWithIntent
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.video.VideoMessageItem
import com.exa.android.letstalk.utils.models.MediaType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageList(
    messages: List<Message>,
    curUserId: String,
    members: List<User?>,
    unreadMessages: Int,
    selectedMessages: Set<Message>,
    updateMessages: (Set<Message>) -> Unit,
    onReply: (message: Message) -> Unit
) {
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    val renderedIndex = remember { mutableStateMapOf<String, Int>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var hasScrolledToUnread by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size, unreadMessages) {
        if (messages.isNotEmpty() && unreadMessages > 0 && !hasScrolledToUnread) {
            hasScrolledToUnread = true
            val targetIndex = messages.size - unreadMessages
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        reverseLayout = true
    ) {
        itemsIndexed(messages.reversed()) { index, message ->
            val reversedIndex = messages.lastIndex - index

            renderedIndex[message.messageId] = index

            if (message.members.contains(curUserId)) {
                MessageBubble(
                    message = message,
                    curUserId = curUserId,
                    members = members,
                    isSelected = selectedMessages.contains(message),
                    selectedMessagesSize = selectedMessages.size,
                    isHighlighted = highlightedIndex == index,
                    onTapOrLongPress = {
                        onMessageLongPress(
                            message,
                            selectedMessages,
                            onSelect = { updatedSelection ->
                                updateMessages(updatedSelection)
                            })
                    },
                    onReply = { message ->
                        onReply(message)
                    },
                    onReplyClick = { id ->
                        coroutineScope.launch {
                            scrollToMessage(id, renderedIndex, listState)
                            renderedIndex[id]?.let { index ->
                                highlightedIndex = index
                                delay(500)
                                highlightedIndex = null
                            }
                        }
                    }
                )
            }
            // Show unread separator right after the message before unread ones
            if (reversedIndex == messages.size - unreadMessages && unreadMessages > 0) {
                UnreadMessageSeparator()
            }
        }

    }
}


@Composable
fun UnreadMessageSeparator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE0E0E0),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Unread Messages",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    curUserId: String,
    members: List<User?>,
    isSelected: Boolean, // it is used to extract that particular messages is selected or not
    selectedMessagesSize: Int, // it is passed to use a key for pointerInput so it changes whenever
    // selection changes and causes the detectGesture to call
    isHighlighted: Boolean, // it used to change color of messages for 500ms to reply that reply message is rendered
    onTapOrLongPress: () -> Unit, //select and unselect messages
    onReply: (Message) -> Unit, // pass the message which is to be reply
    onReplyClick: (String) -> Unit // pass the click reply index to messageList to scroll and update the ui of replied message
) {
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // used in vibration
    val vibrator = getVibrator(context) // to vibrate on right Swipe

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color.Yellow else Color.Transparent)
            .pointerInput(selectedMessagesSize) { // selectedMessagesSize is used for Key as it change it enables to call
                detectTapGestures(
                    onTap = { if (selectedMessagesSize > 0) onTapOrLongPress() },
                    onLongPress = { onTapOrLongPress() }
                )
            }
    ) {
        if (offsetX.value > 50) { // show reply icon on right swipe
            SwipeHint(icon = R.drawable.ic_reply, alignment = Alignment.CenterStart)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.background(if (isSelected) Color.Yellow else Color.Transparent)
                .pointerInput(selectedMessagesSize <= 0) { // active whenever message is unselected
                    if (message.message != "deleted") {
                        detectHorizontalDragGestures(// used for applying rightSwipe gesture for replyMessage functionality
                            onDragEnd = {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        0f,
                                        animationSpec = tween(durationMillis = 600)
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                if (dragAmount > 0) {
                                    coroutineScope.launch {
                                        offsetX.snapTo(offsetX.value + dragAmount)
                                    }
                                    if (offsetX.value > 100) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vibrator?.vibrate(
                                                VibrationEffect.createOneShot(
                                                    50,
                                                    VibrationEffect.DEFAULT_AMPLITUDE
                                                )
                                            )
                                            onReply(message) // send reply message to messageList using Lambda
                                            coroutineScope.launch {
                                                offsetX.animateTo(
                                                    0f,
                                                    animationSpec = tween(durationMillis = 600)
                                                )
                                            }
                                        }
                                        change.consume()
                                    }
                                }
                            }
                        )
                    }
                }
                .offset { IntOffset(offsetX.value.toInt(), 0) } // applying animation
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = if (curUserId == message.senderId) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
        ) {
            val bubbleColor = if (curUserId == message.senderId) Color(
                0xFF007AFF
            ) else Color(0xFFf6f6f6)
            Column(
                modifier = Modifier
                    .widthIn(max = (0.7 * LocalConfiguration.current.screenWidthDp).dp) // occupy 70% of screen only
                    .background(
                        color = if (isHighlighted) bubbleColor.copy(alpha = 0.6f) else bubbleColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(modifier = Modifier
                    //.fillMaxWidth()
                    .clickable(enabled = selectedMessagesSize <= 0 && message.message != "deleted") {
                        message.replyTo?.let {
                            onReplyClick(
                                it.messageId
                            )
                        }
                    }) {
                    message.replyTo?.let {
                        if (message.message != "deleted")
                            ReplyUi(
                                curUser = curUserId,
                                replyTo = it,
                                members = members
                            ) // show replied message inside box
                    }
                }

                if (message.message == "deleted") {
                    Text(
                        text = if (message.senderId == curUserId) "You deleted this message" else "This message was deleted",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = if (message.senderId == curUserId) Color.White.copy(alpha = 0.8F) else Color.Black.copy(
                            alpha = 0.8F
                        ),
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    if (message.media == null) {
                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            color = if (curUserId == message.senderId) Color.White else Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        val fileName = getFileNameFromUrl(message.media.mediaUrl)
                        when (message.media.mediaType) {
                            MediaType.IMAGE -> {
                                ImageMessageContent(
                                    imageUrl = message.media.mediaUrl,
                                    onDownloadClick = {

                                        downloadMedia(
                                            context, message.media.mediaUrl,
                                            fileName = fileName
                                        )
                                        //openImageIntent(context,message.media.mediaUrl)
                                    },
                                    onImageClick = {
                                        openImageIntent(context, message.media.mediaUrl)
                                    }
                                )
                            }

                            MediaType.VIDEO -> {
                                VideoMessageItem(message.media.mediaUrl, fileName)
                            }

                            MediaType.AUDIO -> TODO()
                            MediaType.DOCUMENT -> {
                                DocumentMessageItem(
                                    fileUrl = message.media.mediaUrl,
                                    fileName = getFileNameFromUrl(message.media.mediaUrl)
                                )
                            }

                            MediaType.LOCATION -> TODO()
                            MediaType.CONTACT -> TODO()
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    val timestampInMillis = message.timestamp.seconds * 1000L
                    Text(
                        text = formatTimestamp(timestampInMillis), // generate timeStamp like hrs, yesterday
                        style = MaterialTheme.typography.labelSmall,
                        color = if (curUserId == message.senderId) Color.White else Color.Gray
                    )
                    if (curUserId == message.senderId && message.message != "deleted") {
                        Spacer(modifier = Modifier.width(2.dp))
                        Log.d("Detail Chat", message.status)

                        if (message.status == "delivered") {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = Color.White,  // Gray color for sent
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (message.status == "sent") {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_seen),
                                contentDescription = "Delivered",
                                tint = Color.White,  // Gray color for delivered
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_seen),
                                contentDescription = "Seen",
                                tint = Color.Yellow,  // Blue color for seen
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeHint(icon: Int, alignment: Alignment) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(1.dp, Color.White, CircleShape)
            .background(Color.Black)
            .padding(2.dp),
        contentAlignment = alignment
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Reply Icon",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun onMessageLongPress(
    message: Message,
    selectedMessages: Set<Message>,
    onSelect: (Set<Message>) -> Unit
) {
    Log.d("checkingSelected", "${selectedMessages.toString()} -  $message")
    onSelect(if (selectedMessages.contains(message)) selectedMessages - message else selectedMessages + message)
}

suspend fun scrollToMessage(
    messageId: String,
    renderedIndex: MutableMap<String, Int>,
    listState: LazyListState
) {
    renderedIndex[messageId]?.let { index ->
        listState.animateScrollToItem(index)
    }
}

