package com.exa.android.letstalk.presentation.Main.Home.ChatDetail

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.data.repositories.main.ViewModel.ChatViewModel
import com.exa.android.letstalk.data.repositories.main.ViewModel.UserViewModel
import com.exa.android.khacheri.screens.Main.Home.ChatDetail.ChatHeader
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.MessageList
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.NewMessageSection
import com.exa.android.letstalk.presentation.navigation.component.ChatInfo
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.ScreenPurpose
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.helperFun.getUserIdFromChatId
import com.exa.android.letstalk.utils.models.Call
import com.exa.android.letstalk.utils.models.CallType
import com.exa.android.letstalk.utils.showToast
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun DetailChatScreen(navController: NavController, chat: Chat) {
    val viewModel: ChatViewModel =
        hiltViewModel() // ChatViewModel for communicating with FireStore Service
    val userVM: UserViewModel =
        hiltViewModel()  // UserViewModel for communicating with User Repository
    val responseChatMessages by viewModel.messages.collectAsState() // all the chats of cur and other User
    val curUserId by viewModel.curUser.collectAsState()  // cur User Id
    //val responseChatRoomDetail by userVM.chatRoomDetail.collectAsState() // Other User Details in form of response from UserViewModel
    val responseMembersDetail by viewModel.membersDetail.collectAsState() // Members Details in form of response from ChatViewModel
    val chatRoomStatus by userVM.chatRoomStatus.observeAsState() // Other User Status like online, offline, typing

    val curCall by viewModel.curCall.collectAsState()

    // val chatRoomDetail: MutableState<Chat?> = remember { mutableStateOf(Chat()) } // fetching data from responseUserDetail when Success
    val chatMessages: MutableState<List<Message>> = remember { mutableStateOf(emptyList()) }
    var members by remember { mutableStateOf(emptyList<User>()) }


    var replyMessage by remember { mutableStateOf<Message?>(null) } // to track is message replied
    var selectedMessages by remember { mutableStateOf<Set<Message>>(emptySet()) } // to track the Id's of messages selected to operate HeaderWithOptions
    val focusRequester = remember { FocusRequester() } // to request focus of keyboard
    val focusManager = LocalFocusManager.current // handling focus like show or not show keyboard
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope =
        rememberCoroutineScope() // to handle asynchronous here for calling viewMode.delete

    LaunchedEffect(chat.id) { // on ChatClick

        viewModel.getMessages(chat.id)
        viewModel.fetchChatMembersDetails(chat.id)

        userVM.observeChatRoomStatus(chat.id, chat.group)
        //userVM.getChatRoomDetail(chat.id) // getting otherUser Details
    }

//    when (val response = responseChatRoomDetail) { // fetching otherUserDetails form response on success
//        is Response.Loading -> {}
//        is Response.Success -> {
//            Log.d("Detail Chat", "Success in userDetail")
//            chatRoomDetail.value = response.data
//        }
//
//        else -> {
//            Log.d("Detail Chat", "Error in userDetail")
//        }
//    }


    when (val response = responseChatMessages) {
        is Response.Loading -> {
            Text(text = "Messages Rendering")
        }

        is Response.Success -> {
            chatMessages.value = response.data
        }

        is Response.Error -> {
            Text(text = response.message)
        }
    }

    when (val response = responseMembersDetail) {
        is Response.Loading -> {
            Text(text = "Messages Rendering")
        }

        is Response.Success -> {
            members = response.data
        }

        is Response.Error -> {
            Text(text = response.message)
        }
    }

    DisposableEffect(key1 = Unit) {// when the user while typing navigate to somewhere else then update its typingTo null
        onDispose {
            if (chat.group)
                userVM.setTypingStatus(chat.id, "")
            else
                userVM.setTypingStatus(curUserId, "")

        }
    }
    // Handle keyboard visibility
//    val insets = LocalWindowInfo.current
//    val isKeyboardVisible = insets.isWindowFocused


    Scaffold(
        topBar = {
            ChatHeader(
                chat,
                chatRoomStatus,
                curUserId,
                members,
                selectedMessages,
                onProfileClick = {
                    val chatJson = Gson().toJson(chat)
                    //navController.navigate(ChatInfo.ProfileScreen.createRoute(chatJson))
                },
                onBackClick = { navController.popBackStack() },
                onVoiceCallClick = {
                    val call = Call(
                        callerId = curUserId,
                        receiverId = getUserIdFromChatId(chat.id, curUserId),
                        isVideoCall = CallType.VOICE
                    )
                    viewModel.makeCall(
                        call,
                        onSuccess = {
                            showToast(context = context, "Wait for recipient response")
                        },
                        onFailure = {
                            showToast(context = context, "Recipient is on Another Call, Try Again Later")
                        }
                    )
                },
                onVideoCallClick = {
                    val call = Call(
                        callerId = curUserId,
                        receiverId = getUserIdFromChatId(chat.id, curUserId),
                        isVideoCall = CallType.VIDEO
                    )
                },
                onUnselectClick = { selectedMessages = emptySet() },
                onCopyClick = {
                    val messageToCopy = selectedMessages
                    selectedMessages = emptySet()
                    coroutineScope.launch {
                        val selectedMessagesFormatted =
                            messageToCopy.joinToString(separator = "\n") { message ->
                                val dateTime = SimpleDateFormat(
                                    "dd-MM-yyyy HH:mm",
                                    Locale.getDefault()
                                ).format(
                                    Date(message.timestamp.seconds * 1000L)
                                )
                                "[$dateTime] ${message.senderId}: ${message.message}"
                            }
                        clipboardManager.setText(AnnotatedString(selectedMessagesFormatted))
                    }
                },


                onForwardClick = {
                    val messageToForward = selectedMessages
                    selectedMessages = emptySet()

                    val messagesText =
                        messageToForward.map { it.message }
                    val gson = Gson()
                    val jsonString = gson.toJson(messagesText)
                    navController.navigate(
                        HomeRoute.AllUserScreen.createRoute(
                            purpose = ScreenPurpose.FORWARD_MESSAGES,
                            messageListJson = jsonString
                        )
                    ) // navigate to forward screen where we have all users
                },
                onDeleteClick = { deleteFor ->
                    Log.d("DeleteOpr", deleteFor.toString())
                    val messageToDelete = selectedMessages.map { it.messageId }
                    selectedMessages = emptySet()
                    coroutineScope.launch {
                        viewModel.deleteMessages(
                            messageToDelete,
                            chat.id,
                            deleteFor
                        ) { // get lambda on complete
                            selectedMessages = emptySet()
                        }
                    }
                }
            )
        },
        bottomBar = {
            NewMessageSection(
                chat.group,
                replyMessage,
                members = members,
                curUserId, chat.id,
                userVM,
                focusRequester,
                onTextMessageSend = { text, replyTo ->
                    val membersId = members.map { it.userId }
                    if (chatMessages.value.isEmpty() && !chat.group) {
                        viewModel.createChat(chat) {
                            viewModel.createChatAndSendMessage(
                                chat.id,
                                text,
                                replyTo,
                                membersId
                            )
                        }
                    } else {
                        viewModel.createChatAndSendMessage(
                            chat.id,
                            text,
                            replyTo,
                            membersId
                        )
                    }
                },
                onRecordingSend = { /*TODO*/ },
                onAddClick = {},
                onSendOrDiscard = { replyMessage = null },
                onDone = {
                    focusManager.clearFocus()  // remove keyboard from focus
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
//                .padding(
//                    bottom = with(LocalDensity.current) {
//                        WindowInsets.ime
//                            .getBottom(this)
//                            .toDp()
//                    }
//                )
        ) {
            // Chat Messages (LazyColumn) in the center

            MessageList(
                chatMessages.value,
                curUserId,
                members,
                chat.unreadMessages.toInt(),
                selectedMessages,
                updateMessages = { newMessages ->
                    selectedMessages = newMessages
                    Log.d("checkingSelected", selectedMessages.toString())
                },
                onReply = { message ->
                    replyMessage = message
                    focusRequester.requestFocus()
                }
            )

        }
    }
}


//data class Message(val isSentByCurrentUser : Boolean, val message : String)
//@Preview(showBackground = true)
//@Composable
//fun PreviewDashedCircle() {
//    DetailChat()
//}
