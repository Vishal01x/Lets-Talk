package com.exa.android.letstalk.utils.helperFun

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.exa.android.letstalk.utils.models.Media
import com.exa.android.letstalk.utils.models.Message
import java.io.File
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AudioWaveForm(
    modifier: Modifier = Modifier,
    isPaused: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val amplitudeAnimation by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val waveAmplitudes = remember {
        List(10) { Random.nextFloat() } // Mock random wave amplitudes
    }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(40.dp)) {
        val waveWidth = size.width / waveAmplitudes.size
        val centerY = size.height / 2

        waveAmplitudes.forEachIndexed { index, amplitude ->
            val barHeight = if (isPaused) 0f else centerY * amplitude * amplitudeAnimation
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(
                    x = index * waveWidth + waveWidth / 4,
                    y = centerY - barHeight / 2
                ),
                size = Size(waveWidth / 2, barHeight),
                cornerRadius = CornerRadius(x = 4.dp.toPx(), y = 4.dp.toPx())
            )
        }
    }
}



fun formatTimestamp(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    val todayStart = calendar.apply {
        timeInMillis = currentTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterdayStart = todayStart - 24 * 60 * 60 * 1000 // Start of yesterday
   // val dayBeforeYesterdayStart = yesterdayStart - 24 * 60 * 60 * 1000 // Start of day before yesterday

    return when {
        timestamp >= todayStart -> {
            // Today's timestamp: show clock time
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(Date(timestamp))
        }
        timestamp >= yesterdayStart -> "Yesterday" // Yesterday's timestamp
        else -> {
            // Older: show date in a specific format
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}

fun generateChatId(user1: String? = null, user2: String? = null): String {
    if(user1 != null && user2 != null) {
        return if (user1 > user2) "$user1-$user2" else "$user2-$user1"
    }
    return UUID.randomUUID().toString()
}
fun getOtherUserName(chatName: String, chatId: String, currentUser: String): String {
    val users = chatName.split("-").map { it.trim() }
    val chats = chatId.split("-").map { it.trim() }
    val trimmedCurrentUser = currentUser.trim()
    Log.d("checkingName", "Users: $users, Chats: $chats, Current User: $trimmedCurrentUser")
    return if (chats.size == 2 && users.size == 2 && chats.contains(trimmedCurrentUser)) {
        if (chats[0] == trimmedCurrentUser) users[1] else users[0]
    } else {
        chatName // Return same if no valid other user
    }
}

fun generateChatName(chatId: String, curUserId: String, curUserName: String, otherUserName: String): String {
    // Determine the position of the current user in the chat based on the chat ID
    val chatUsers = chatId.split("-").map { it.trim() }

    return if (chatUsers.size == 2) {
        if (chatUsers[0] == curUserId) {
            "$curUserName-$otherUserName"  // Current user on the left
        } else {
            "$otherUserName-$curUserName"  // Current user on the right
        }
    } else {
        // Fallback if chat ID doesn't match expected structure
        chatId
    }
}

fun getOtherProfilePic(profilePic: String, chatId: String, currentUser: String): String {
    val users = profilePic.split("-").map { it.trim() }
    val chats = chatId.split("-").map { it.trim() }
    val trimmedCurrentUser = currentUser.trim()
    Log.d("checkingName", "Users: $users, Chats: $chats, Current User: $trimmedCurrentUser")
    return if (chats.size == 2 && users.size == 2 && chats.contains(trimmedCurrentUser)) {
        if (chats[0] == trimmedCurrentUser) users[1] else users[0]
    } else {
        profilePic // Return same if no valid other user
    }
}

fun generateProfilePic(chatId: String, curUserId: String, curUserProfile: String, otherUserProfile: String): String {
    // Determine the position of the current user in the chat based on the chat ID
    val chatUsers = chatId.split("-").map { it.trim() }

    return if (chatUsers.size == 2) {
        if (chatUsers[0] == curUserId) {
            "$curUserProfile-$otherUserProfile"  // Current user on the left
        } else {
            "$otherUserProfile-$curUserProfile"  // Current user on the right
        }
    } else {
        // Fallback if chat ID doesn't match expected structure
        chatId
    }
}

fun generateMessage(
    currentUser: String,
    chatId: String,
    text: String,
    media : Media? = null,
    replyTo : Message? = null,
    members: List<String> = emptyList()
): Message =  Message(
    chatId = chatId,
    senderId = currentUser,
    message = cleanMessage(text),
    media = media,
    replyTo = replyTo,
    members = members.ifEmpty {
        listOf(
            currentUser,
            getUserIdFromChatId(chatId, currentUser)
        )
    }
)

fun cleanMessage(message: String): String {
    return message.replace("@urgent", "", ignoreCase = true).trim()
}

fun getUserIdFromChatId(chatId: String, currentUser: String): String {
    return chatId.split('-').filter { it != currentUser }.joinToString("")
}
