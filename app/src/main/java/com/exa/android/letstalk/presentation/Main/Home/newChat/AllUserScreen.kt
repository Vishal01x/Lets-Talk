package com.exa.android.letstalk.presentation.Main.Home.newChat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.data.domain.main.ViewModel.ChatViewModel
import com.exa.android.letstalk.data.domain.main.ViewModel.UserViewModel
import com.exa.android.letstalk.presentation.Main.components.CircularUserImage
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.ScreenPurpose
import com.exa.android.letstalk.utils.helperFun.generateChatId
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.utils.Response
import com.google.gson.Gson
import java.net.URLEncoder

@Composable
fun AllUserScreen(
    navController: NavController,
    purpose: ScreenPurpose,
    forwardMessages: List<String>,
    newGroupViewModel : NewGroupViewModel
) {
    val chatViewModel : ChatViewModel = hiltViewModel()

    val curUserId  = chatViewModel.curUserId.collectAsState()
    when (purpose) {
        ScreenPurpose.NEW_CHAT -> {
            AllUserUi(navController){user->
                val chatId = generateChatId(curUserId.value, user.userId)
                val chat = Chat(id = chatId, name = user.name, profilePicture = user.profilePicture)
                val chatJson = Gson().toJson(chat)
                val encodedChatJson = URLEncoder.encode(chatJson, "UTF-8")
                navController.navigate(
                    HomeRoute.ChatDetail.createRoute(
                        encodedChatJson
                    )
                )
            }
        }

        ScreenPurpose.NEW_GROUP -> {
            val selectedUsers by newGroupViewModel.selectedUsers
            var createGroup by remember { mutableStateOf(false) }
            var loading by remember{ mutableStateOf(false) }

            LaunchedEffect(createGroup) {
                if(createGroup) {
//                    val selectedUsersID = selectedUsers.map{it.userId}
//                    chatViewModel.createGroup("Just Chill", selectedUsersID) {chatId->
//                        val chat = Chat(id = chatId, name = "Just Chill", profilePicture = "", group = true)
//                        val chatJson = Gson().toJson(chat)
//                        val encodedChatJson = URLEncoder.encode(chatJson, "UTF-8")
//                        navController.navigate(HomeRoute.ChatDetail.createRoute(encodedChatJson)){
//                            popUpTo(HomeRoute.AllUserScreen.route){
//                                inclusive = true
//                            }
//                        }
//                    }
                }
            }

            if(loading){
                Log.d("ChatRepo", "show loader")
                LoadingScreen()
            }

            AllUserUi(navController = navController, selectedUsers = selectedUsers,
                onForwardClick = {
                    navController.navigate(HomeRoute.CreateGroup.route)
                    createGroup = true
                }
            ) {user->
                handleUserClick(
                    selectedUsers,
                    user
                ) { updatedSet -> newGroupViewModel.updateSelectedUsers(updatedSet) }
            }
        }

        ScreenPurpose.FORWARD_MESSAGES -> {
            val chatViewModel : ChatViewModel = hiltViewModel()

            var selectedUsers by remember { mutableStateOf(emptySet<User>()) }
            var canForward by remember { mutableStateOf(false) }

            LaunchedEffect(canForward) {
                if(canForward) {
                    val receivers = selectedUsers.map { it }
                    chatViewModel.forwardMessages(forwardMessages, receivers)
                }
            }

            AllUserUi(navController = navController, selectedUsers = selectedUsers,
                onForwardClick = {
                    canForward = true
                    navController.popBackStack(HomeRoute.ChatDetail.route, inclusive = true)
                }) {user->
                handleUserClick(
                    selectedUsers,
                    user
                ) { updatedSet -> selectedUsers = updatedSet }
            }
        }
    }
}

@Composable
fun AllUserUi(navController: NavController, selectedUsers: Set<User>? = null, onForwardClick: (() -> Unit)? = null, onUserClick: (User) -> Unit) {

    val userViewModel: UserViewModel = hiltViewModel()

    val responseAllUsers by userViewModel.allUsers.collectAsState()
    var allUsers by remember { mutableStateOf(emptyList<User?>()) }

    LaunchedEffect(Unit) {
        userViewModel.getAllUsers()
    }

    when (val response = responseAllUsers) {
        is Response.Loading -> LoadingScreen()
        is Response.Success -> allUsers = response.data
        is Response.Error -> ErrorScreen(response.message)
    }

    Scaffold(
        topBar = {
            AllUserTopBar(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        },
        bottomBar = {
            if (!selectedUsers.isNullOrEmpty()) {
                SelectedUsersBar(
                    selectedUsers,
                    onForwardClick = {
                        onForwardClick!!() // definitely onForwardClick pass when selectedUsers is passed
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(allUsers) { user ->
                user?.let {
                    if(user.name.isNotBlank()) {
                        AllUsersListItem(
                            user,
                            isSelected = selectedUsers?.contains(user) ?: false,
                            onUserClick = {
                                onUserClick(user)
                            },
                            zoomImage = { imageId ->
                                navController.navigate(HomeRoute.ZoomImage.createRoute(imageId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllUserTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Select User",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Choose a user from the list",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick =  onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Handle search */ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search User",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { /* Handle more options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = Color.Black
                )
            }
        },
        backgroundColor = Color.White
    )
}

@Composable
fun SelectedUsersBar(
    selectedUsers: Set<User>,
    onForwardClick : () -> Unit
) {
    val usersName = selectedUsers.joinToString { it.name }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = usersName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(0.8f)
            )
            IconButton(
                onClick = {
                   onForwardClick()
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward Icon",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun AllUsersListItem(
    user: User,
    isSelected: Boolean,
    onUserClick: () -> Unit,
    zoomImage: (Int) -> Unit
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Box {
            CircularUserImage(context, user.profilePicture ?: "", imageUri = null, Modifier
                .size(48.dp)
                .clip(CircleShape))
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected User",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp) // Adjust the size of the check icon
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .padding(2.dp)
                        .background(Color.Black)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = user.name,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

fun handleUserClick(
    selectedUsers: Set<User>,
    user: User,
    onSelect: (Set<User>) -> Unit
) {
        onSelect(if (selectedUsers.contains(user)) selectedUsers - user else selectedUsers + user)

}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Loading users...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Error: $message",
            color = Color.Red,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
