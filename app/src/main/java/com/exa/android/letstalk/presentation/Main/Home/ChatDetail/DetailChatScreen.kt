package com.exa.android.letstalk.presentation.Main.Home.ChatDetail

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.exa.android.khacheri.screens.Main.Home.ChatDetail.ChatHeader
import com.exa.android.letstalk.AppManager.switchSheetState
import com.exa.android.letstalk.data.domain.main.ViewModel.ChatViewModel
import com.exa.android.letstalk.data.domain.main.ViewModel.MediaSharingViewModel
import com.exa.android.letstalk.data.local.room.ScheduledMessageViewModel
import com.exa.android.letstalk.data.domain.main.ViewModel.UserViewModel
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.MessageList
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.NewMessageSection
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.ScreenPurpose
import com.exa.android.letstalk.presentation.Main.Home.components.MessageSchedulerDialog
import com.exa.android.letstalk.presentation.navigation.component.ProfileRoute
import com.exa.android.letstalk.utils.CurChatManager.activeChatId
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.clearChatNotifications
import com.exa.android.letstalk.utils.helperFun.generateMessage
import com.exa.android.letstalk.utils.helperFun.getUserIdFromChatId
import com.exa.android.letstalk.utils.models.Call
import com.exa.android.letstalk.utils.models.CallType
import com.exa.android.letstalk.utils.models.ScheduleType
import com.exa.android.letstalk.utils.showToast
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetailChatScreen(navController: NavController, chat: Chat) {
    val chatViewModel: ChatViewModel =
        hiltViewModel()  // ChatViewModel for communicating with FireStore Service
    val userViewModel: UserViewModel =
        hiltViewModel()   // UserViewModel for communicating with User Repository
    val scheduleMessageViewModel: ScheduledMessageViewModel = hiltViewModel()
    val mediaSharingViewModel: MediaSharingViewModel = hiltViewModel()

    val responseChatMessages by remember { chatViewModel.messages }.collectAsState() // all the chats of cur and other User
    val curUserId by chatViewModel.curUserId.collectAsState()  // cur User Id
    //val curUser by chatViewModel.curUser.collectAsState() // cur User Details
    //val responseChatRoomDetail by userVM.chatRoomDetail.collectAsState() // Other User Details in form of response from UserViewModel
    val responseMembersDetail by chatViewModel.membersDetail.collectAsState() // Members Details in form of response from ChatViewModel
    val chatRoomStatus by userViewModel.chatRoomStatus.observeAsState() // Other User Status like online, offline, typing
    val curCall by chatViewModel.curCall.collectAsState()

    // val chatRoomDetail: MutableState<Chat?> = remember { mutableStateOf(Chat()) } // fetching data from responseUserDetail when Success
    val chatMessages: MutableState<List<Message>> = remember { mutableStateOf(emptyList()) }
    var members by remember { mutableStateOf(emptyList<User>()) }

    var replyMessage by remember { mutableStateOf<Message?>(null) } // to track is message replied
    var selectedMessages by remember { mutableStateOf<Set<Message>>(emptySet()) } // to track the Id's of messages selected to operate HeaderWithOptions
    val focusRequester = remember { FocusRequester() } // to request focus of keyboard
    val focusManager = LocalFocusManager.current // handling focus like show or not show keyboard
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showScheduleDialog = remember { mutableStateOf(false) }
    val coroutineScope =
        rememberCoroutineScope() // to handle asynchronous here for calling viewMode.delete

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {


        }
    }

    LaunchedEffect(chat.id) { // on ChatClick
        chatViewModel.getMessages(chat.id)
        chatViewModel.fetchChatMembersDetails(chat.id)
        userViewModel.observeChatRoomStatus(chat.id, chat.group)
    }

    when (val response = responseChatMessages) {
        is Response.Success -> chatMessages.value = response.data
        is Response.Error -> Text(text = response.message)
        else -> {}
    }

    when (val response = responseMembersDetail) {
        is Response.Success -> members = response.data
        is Response.Error -> Text(text = response.message)
        else -> {}
    }

//    DisposableEffect(Unit) { // when the user while typing navigate to somewhere else then update its typingTo null
//        onDispose {
//            if (chat.group) userViewModel.setTypingStatus(chat.id, "")
//            else userViewModel.setTypingStatus(curUserId, "")
//            activeChatId = null
//        }
//    }

    //Lifecycle Observer: Ensures activeChatId updates when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    clearChatNotifications(context, chat.id)
                    activeChatId = chat.id // Update chat ID when app resumes
                    Log.d("ChatScreen", "App Resumed: ActiveChatId updated to $activeChatId")
                }

                Lifecycle.Event.ON_STOP -> {
                    activeChatId = null // Reset chat when app goes to background
                    Log.d("ChatScreen", "App in Background: ActiveChatId Cleared")
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            if (chat.group) userViewModel.setTypingStatus(chat.id, "")
            else userViewModel.setTypingStatus(curUserId, "")
            activeChatId = null // Reset when leaving chat screen

            //keyboardController?.hide()
        }
    }

    MediaPickerHandler(
        showAll = true,
        onLaunch = { uri ->
            coroutineScope.launch {
//                val file = mediaSharingRepository.createTempFileFromUri(context, uri)
                val media = mediaSharingViewModel.uploadFileToCloudinary(context, uri)
                Log.d("Storage Cloudinary", "Uploaded URL: ${media.toString()}")

                val membersId = members.map { it.userId }
                sendMessage(
                    chatViewModel, scheduleMessageViewModel,
                    generateMessage(curUserId, chat.id, "", media, null, membersId),
                    chat, chatMessages.value
                )
            }
        }
    )

    Scaffold(
        topBar = {
            ChatHeader(
                chat, chatRoomStatus, curUserId, members, selectedMessages,
                onProfileClick = {
                    val otherUserId = getUserIdFromChatId(chat.id, curUserId)
                    navController.navigate(ProfileRoute.OtherProfileScreen.createRoute(otherUserId))
                },
                onBackClick = { navController.popBackStack() },
                onVoiceCallClick = { makeVoiceCall(chatViewModel, curUserId, chat, context) },
                onVideoCallClick = { /*startVideoCall(zegoViewModel, members)*/ },
                onUnselectClick = { selectedMessages = emptySet() },
                onCopyClick = {
                    copyMessages(selectedMessages, clipboardManager, coroutineScope)
                    selectedMessages = emptySet()
                },
                onForwardClick = {
                    forwardMessages(selectedMessages, navController)
                    selectedMessages = emptySet()
                },
                onDeleteClick = { deleteFor ->
                    deleteMessages(
                        chatViewModel,
                        selectedMessages,
                        chat.id,
                        deleteFor,
                        coroutineScope
                    )
                    selectedMessages = emptySet()
                }
            )
        },
        bottomBar = {
            NewMessageSection(
                chat.group,
                replyMessage,
                members,
                curUserId,
                chat.id,
                userViewModel,
                scheduleMessageViewModel,
                focusRequester,
                onTextMessageSend = { text, replyTo ->
                    val membersId = members.map { it.userId }
                    sendMessage(
                        chatViewModel, scheduleMessageViewModel,
                        generateMessage(curUserId, chat.id, text, null, replyTo, membersId),
                        chat, chatMessages.value
                    )
                },
                onAddClick = {
                    mediaSharingViewModel.showMediaPickerSheet = true
                    //filePickerLauncher.launch("*/*")
                },
                onClockClick = {
                    if (scheduleMessageViewModel.scheduleMessageType.value == ScheduleType.NONE) {
                        showScheduleDialog.value = true
                    } else {
                        scheduleMessageViewModel.updateScheduleMessageType(ScheduleType.NONE)
                        showToast(context, "Message Scheduling Deactivated")
                    }
                },
                onSendOrDiscard = { replyMessage = null },
                onDone = { focusManager.clearFocus() },
                onRecordingSend = { }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            MessageList(
                chatMessages.value,
                curUserId,
                members,
                chat.unreadMessages.toInt(),
                selectedMessages,
                updateMessages = { selectedMessages = it },
                onReply = { replyMessage = it; focusRequester.requestFocus() }
            )

            if (showScheduleDialog.value) {
                MessageSchedulerDialog(
                    onDismiss = { showScheduleDialog.value = false },
                    onConfirm = { scheduledTime, scheduledType ->
                        scheduleMessageViewModel.updateScheduleMessageType(scheduledType)
                        scheduleMessageViewModel.setTime(scheduledTime)
                    }
                )
            }
        }
    }
}

private fun navigateToProfile(navController: NavController, chat: Chat) {
    val chatJson = Gson().toJson(chat)
    // navController.navigate(ChatInfo.ProfileScreen.createRoute(chatJson))
}

private fun makeVoiceCall(
    viewModel: ChatViewModel,
    curUserId: String,
    chat: Chat,
    context: Context
) {
    val call = Call(
        callerId = curUserId,
        receiverId = getUserIdFromChatId(chat.id, curUserId),
        isVideoCall = CallType.VOICE
    )
    viewModel.makeCall(call,
        onSuccess = { showToast(context, "Wait for recipient response") },
        onFailure = { showToast(context, "Recipient is on another call, try again later") }
    )
}

//private fun startVideoCall(zegoViewModel: ZegoViewModel, members: List<User>) {
//    val users = members.map { it.userId }
//    zegoViewModel.startCall(users, true)
//}

private fun copyMessages(
    selectedMessages: Set<Message>,
    clipboardManager: ClipboardManager,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        val formattedMessages = selectedMessages.joinToString("\n") {
            "[${
                SimpleDateFormat(
                    "dd-MM-yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(it.timestamp.seconds * 1000L))
            }] ${it.senderId}: ${it.message}"
        }
        clipboardManager.setText(AnnotatedString(formattedMessages))
    }
}

private fun forwardMessages(selectedMessages: Set<Message>, navController: NavController) {
    val messagesText = selectedMessages.map { it.message }
    val jsonString = Gson().toJson(messagesText)
    navController.navigate(
        HomeRoute.AllUserScreen.createRoute(
            ScreenPurpose.FORWARD_MESSAGES,
            jsonString
        )
    )
    // navigate to forward screen where we have all users
}

private fun deleteMessages(
    viewModel: ChatViewModel,
    selectedMessages: Set<Message>,
    chatId: String,
    deleteFor: Int,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        viewModel.deleteMessages(
            selectedMessages.map { it.messageId }, chatId,
            deleteFor
        ) {
//            emptySelectedMessages()
        }
    }
}

private fun sendMessage(
    chatViewModel: ChatViewModel,
    scheduledMessageViewModel: ScheduledMessageViewModel,
    message: Message,
    chat: Chat,
    chatMessages: List<Message>
) {

    if (chatMessages.isEmpty() && !chat.group) {
        chatViewModel.createChat(chat) {
            if (scheduledMessageViewModel.scheduleMessageType.value != ScheduleType.NONE) {
                scheduledMessageViewModel.scheduleMessage(
                    message,
                    scheduledMessageViewModel.scheduleTime.value,
                    chat.name,
                    chat.profilePicture ?: ""
                )
                if (scheduledMessageViewModel.scheduleMessageType.value == ScheduleType.ONCE)
                    scheduledMessageViewModel.updateScheduleMessageType(ScheduleType.NONE)
            } else {
                chatViewModel.createChatAndSendMessage(
                    message, chat.profilePicture
                )
            }
        }
    } else {
        if (scheduledMessageViewModel.scheduleMessageType.value != ScheduleType.NONE) {
            scheduledMessageViewModel.scheduleMessage(
                message,
                scheduledMessageViewModel.scheduleTime.value,
                chat.name,
                chat.profilePicture ?: ""
            )
            if (scheduledMessageViewModel.scheduleMessageType.value == ScheduleType.ONCE)
                scheduledMessageViewModel.updateScheduleMessageType(ScheduleType.NONE)
        } else {
            chatViewModel.createChatAndSendMessage(
                message, chat.profilePicture
            )
        }
    }
}
