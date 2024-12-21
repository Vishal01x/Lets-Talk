package com.exa.android.khacheri.screens.Main.Home.ChatDetail

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
import com.exa.android.khacheri.utils.helperFun.formatTimestamp
import com.exa.android.khacheri.utils.helperFun.getVibrator
import com.exa.android.khacheri.utils.models.Message
import com.exa.android.khacheri.utils.models.User
import com.exa.android.letstalk.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageList(
    messages: List<Message>,
    curUserId: String,
    members: List<User?>,
    unreadMessages: Int,
    selectedMessages: Set<String>,
    updateMessages : (Set<String>) -> Unit,
    onReply: (message: Message) -> Unit
) {
    // State for selected messages and showing options
    Log.d("checkingSelected", selectedMessages.toString())
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    val renderedIndex = remember { mutableStateMapOf<String, Int>() }

    // LazyColumn state and coroutine scope for scrolling
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // LazyColumn for displaying the message bubbles
    LazyColumn(
        state = listState
    ) {
        itemsIndexed(messages) { index, message ->
            renderedIndex[message.messageId] = index
            if(message.members.contains(curUserId)) {
                MessageBubble(
                    message = message,
                    curUserId = curUserId,
                    members = members,
                    isSelected = selectedMessages.contains(message.messageId),
                    selectedMessagesSize = selectedMessages.size,
                    isHighlighted = highlightedIndex == index,
                    onTapOrLongPress = {
                        onMessageLongPress(
                            message.messageId,
                            selectedMessages,
                            onSelect = { updatedSelection ->
                                updateMessages(updatedSelection)
                            })
                    },
                    onReply = { message ->
                        onReply(message)
                    },
                    onReplyClick = { id ->
                        coroutineScope.launch {// since using launched effect i was not able to re-scroll to the same message again and again
                            // and if we try to maintain any variable so we need to update it that causes re-composition that leads it to scroll till mid and before reaching
                            // re-scroll to end
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
        }
    }

    // LaunchedEffect to handle scrolling to unread messages once
    LaunchedEffect(messages.size, unreadMessages) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                val targetIndex = if (unreadMessages > 0) messages.size - unreadMessages else messages.size - 1
                listState.animateScrollToItem(targetIndex)
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    curUserId: String,
    members: List<User?>,
    isSelected: Boolean, // it is used to extract that particular messages is selected or not
    selectedMessagesSize : Int, // it is passed to use a key for pointerInput so it changes whenever
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
                    onTap = { if (selectedMessagesSize>0) onTapOrLongPress() },
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
                .pointerInput(selectedMessagesSize<=0) { // active whenever message is unselected
                    detectHorizontalDragGestures(// used for applying rightSwipe gesture for replyMessage functionality
                        onDragEnd = {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, animationSpec = tween(durationMillis = 600))
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
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
                    )
                }
                .offset { IntOffset(offsetX.value.toInt(), 0) } // applying animation
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = if (curUserId == message.senderId) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = (0.7 * LocalConfiguration.current.screenWidthDp).dp) // occupy 70% of screen only
                    .background(
                        color = if (isHighlighted) Color.LightGray else if (curUserId == message.senderId) Color(0xFF007AFF) else Color(0xFFf6f6f6),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = selectedMessagesSize<=0) { message.replyTo?.let { onReplyClick(it.messageId) } }) {
                    message.replyTo?.let {
                        ReplyUi(replyTo = it, members = members) // show replied message inside box
                    }
                }

                if(message.message == "deleted"){
                    Text(
                        text =  if(message.senderId == curUserId)"You deleted this message" else "This message was deleted",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 16.sp),
                        color = Color.White.copy(alpha = 0.8F),
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic
                    )
                }else{
                    Text(
                        text =  message.message,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 16.sp),
                        color = if (curUserId == message.senderId) Color.White else Color.Black,
                        fontWeight = FontWeight.Medium
                    )
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
                    if (curUserId == message.senderId) {
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
    messageId : String,
    selectedMessages: Set<String>,
    onSelect: (Set<String>) -> Unit
) {
    Log.d("checkingSelected", "${selectedMessages.toString()} -  $messageId")
    onSelect(if (selectedMessages.contains(messageId)) selectedMessages - messageId else selectedMessages + messageId)
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