package com.exa.android.letstalk.presentation.Main.priorityMessages

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.R
import com.exa.android.letstalk.presentation.Main.priorityMessages.PriorityViewModel
import com.exa.android.letstalk.presentation.Main.components.CircularUserImage
import com.exa.android.letstalk.presentation.Main.components.showLoader
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.core.utils.Response
import com.exa.android.letstalk.core.utils.helperFun.formatTimestamp
import com.exa.android.letstalk.domain.Chat
import com.exa.android.letstalk.domain.PriorityMessage
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLEncoder


@Composable
fun PriorityMessagingScreen(
    navController: NavController,
    priorityViewModel: PriorityViewModel = hiltViewModel()
) {

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var showReplyBox by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    val curUserId by priorityViewModel.curUserId.collectAsState()
    var curMessage by remember { mutableStateOf<PriorityMessage?>(null) }

    val responsePriorityMessage by priorityViewModel.messages.collectAsState()
    var priorityMessage by remember { mutableStateOf(emptyList<PriorityMessage>()) }

    LaunchedEffect(Unit) {
        priorityViewModel.getPriorityMessages()
    }

    when (val response = responsePriorityMessage) {
        is Response.Success -> priorityMessage = response.data
        is Response.Error -> Text(text = response.message)
        else -> {
            showLoader()
        }
    }

    Log.d("Firebase Operation", responsePriorityMessage.toString())

    Scaffold(
        topBar = { MessagesHeader() },
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showReplyBox = true },
                backgroundColor = Color(0xFF0D1B2A)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Message",
                    tint = Color.White
                )
            }
        },
        backgroundColor = Color(0xFFF6F6F6)
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            if(priorityMessage.isEmpty()){
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(), // take full screen
                    contentAlignment = Alignment.Center // center children
                ) {
                    Image(
                        painter = painterResource(R.drawable.no_data),
                        contentDescription = "No Data",
                        modifier = Modifier.height(500.dp)
                    )
                }
            }else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(priorityMessage) { message ->
                            // Load chat detail for each message safely
                            val otherUserChat by produceState<Chat?>(
                                initialValue = null,
                                key1 = message.message.chatId
                            ) {
                                value = priorityViewModel.getChatDetail(message.message.chatId)
                            }

                            PriorityMessageCard(
                                data = message,
                                otherChat = otherUserChat ?: Chat(),
                                onReplyClick = {

                                    val chatJson = Gson().toJson(otherUserChat)
                                    val encodedChatJson = URLEncoder.encode(chatJson, "UTF-8")
                                    navController.navigate(
                                        HomeRoute.ChatDetail.createRoute(
                                            encodedChatJson
                                        )
                                    )
                                    curMessage = message

                                },
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }

                    AnimatedVisibility(visible = showReplyBox) {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .background(Color.White),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = replyText,
                                    onValueChange = { replyText = it },
                                    placeholder = { Text("Type your reply...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.textFieldColors(
                                        backgroundColor = Color(0xFFF0F0F0),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )

                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar("Reply sent")
                                    }
                                    replyText = ""
                                    showReplyBox = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = Color(0xFF0D1B2A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesHeader() {
    TopAppBar(
        title = { Text("Priority Messages", style = MaterialTheme.typography.titleLarge) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier.shadow(4.dp)
    )
}

data class NotificationData(
    val name: String,
    val urgencyLabel: String,
    val timeAgo: String,
    val message: String,
    val category: String,
    @DrawableRes val avatarRes: Int
)


@Composable
fun PriorityMessageCard(
    data: PriorityMessage,
    otherChat : Chat,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        val context = LocalContext.current
        Row(modifier = Modifier.padding(16.dp)) {
            CircularUserImage(
                context, otherChat.profilePicture, null, Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = otherChat.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFEAEA), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Urgent",
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    val timestampInMillis = data.message.timestamp.seconds * 1000L
                    Text(
                        text = formatTimestamp(timestampInMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = data.message.message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Messenger",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onReplyClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B2A))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_reply),
                                contentDescription = "reply",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Reply", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


//
//@Preview(showBackground = true)
//@Composable
//fun NotificationCardPreview() {
//    val sampleData = NotificationData(
//        name = "Lisa Park",
//        urgencyLabel = "URGENT",
//        timeAgo = "20 minutes ago",
//        message = "Security breach detected in building 3. Immediate lockdown protocol initiated.",
//        category = "Security",
//        avatarRes = R.drawable.chat_img3 // Replace with your drawable
//    )
//
//    MaterialTheme {
//        NotificationCard(data = sampleData, onReplyClick = { /* Do something */ })
//    }
//}
