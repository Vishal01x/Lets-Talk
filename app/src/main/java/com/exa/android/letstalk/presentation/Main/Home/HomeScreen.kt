package com.exa.android.letstalk.presentation.Main.Home

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.data.domain.main.ViewModel.ChatViewModel
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.presentation.navigation.component.ScreenPurpose
import com.exa.android.letstalk.presentation.navigation.component.SheetState
import com.exa.android.letstalk.AppManager.curBottomSheetState
import com.exa.android.letstalk.AppManager.setCurChat
import com.exa.android.letstalk.AppManager.switchSheetState
import com.exa.android.letstalk.R
import com.exa.android.letstalk.presentation.Main.Home.components.ChatListItem
import com.exa.android.letstalk.presentation.Main.Home.components.StoryItem
import com.exa.android.letstalk.presentation.auth.components.ShowLoader
import com.exa.android.letstalk.utils.Constants
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.helperFun.getOtherUserName
import com.google.gson.Gson

import kotlinx.coroutines.launch
import java.net.URLEncoder

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val curUser = viewModel.curUserId.collectAsState().value
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearching by remember { mutableStateOf(false) }

    BackHandler(true) {
        if(isSearching){
            isSearching = false
        }else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            HeaderSection(
                isSearching = isSearching,
                searchQuery = searchQuery,
                onQueryChange = { viewModel.searchQuery.value = it },
                onBackClick = {
                    isSearching = false
                    viewModel.searchQuery.value = ""
                },
                onSearchClick = { isSearching = true },
            )
        },
//        floatingActionButton = {
//            if (curBottomSheetState.value == SheetState.HIDE) {
//                androidx.compose.material3.FloatingActionButton(
//                    onClick = {
//                        switchSheetState()
//                    },
//                    shape = CircleShape,
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = MaterialTheme.colorScheme.onPrimary
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(28.dp))
//                }
//            }
//        }
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 2.dp)
            ) {

                ChatsSection(navController, viewModel)
            }
            if (curBottomSheetState.value == SheetState.SHOW) {
                BottomSheetSection(navController)
            }
        }
    }
}

@Composable
fun HeaderSection(
    isSearching: Boolean,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    if (isSearching) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            BasicTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(.8f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search chats...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Let's Talk",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clickable { onSearchClick() }
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun StoriesSection() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp), // No padding at the end
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add "Add Story" item
        item {
            AddStoryItem()
        }

        // Add stories
        items(storyList) { story ->
            StoryItem(image = story.image, name = story.name)
        }
    }
}

@Composable
fun AddStoryItem() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(start = 12.dp) // Padding for the first item
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Status",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Add Story",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black
        )
    }
}

@Composable
fun ChatsSection(navController: NavController, viewModel: ChatViewModel) {
    val chatList by viewModel.filteredChatList.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp), // Avoid spacing issues
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        item {
            Spacer(modifier = Modifier.height(12.dp))
            StoriesSection()
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Add a title to the chat list
        item {
            ChatTitle()
        }

        // Handle the state of the chat list
        when (val response = chatList) {
            is Response.Loading -> {
                // Show a loading indicator
                item {
                    ShowLoader()
                }
            }

            is Response.Success -> {
                // Display the chat list
                if (response.data.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No chats Yet",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate(HomeRoute.AllUserScreen.createRoute(purpose = ScreenPurpose.NEW_CHAT)) }) {
                                Text(text = "New Chat")
                            }
                        }
                    }
                } else {
                    items(response.data) { chat ->
                        ChatListItem(
                            chat = chat,
                            zoomImage = { imageId ->
                                navController.navigate("zoomImage/${Uri.encode(imageId)}")
                            },
                            openChat = { chatId ->
                                setCurChat(chat)
                                val chatJson = Gson().toJson(chat)
                                val encodedChatJson = URLEncoder.encode(chatJson, "UTF-8")
                                navController.navigate(
                                    HomeRoute.ChatDetail.createRoute(
                                        encodedChatJson
                                    )
                                )
                            }
                        )
                    }
                }
            }

            is Response.Error -> {
                // Show a friendly error message and optional retry button
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load chats. Please try again.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.getChatList() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ChatTitle(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "menu",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BottomSheetSection(navController: NavController) {
    CustomBottomSheet(
        onNewChatClick = {
            navController.navigate(HomeRoute.AllUserScreen.createRoute(purpose = ScreenPurpose.NEW_CHAT))
            switchSheetState()
        },
        onNewContactClick = { /*TODO*/ },
        onNewGroupClick = {
            switchSheetState()
            navController.navigate(HomeRoute.AllUserScreen.createRoute(purpose = ScreenPurpose.NEW_GROUP))
        }
    )
}

@Composable
fun CustomBottomSheet(
    onNewChatClick: () -> Unit,
    onNewContactClick: () -> Unit,
    onNewGroupClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        scrimColor = Color.Black.copy(alpha = 0.6f), // Dimming the background
        sheetContent = {
            BottomSheetContent(
                onNewChatClick = { onNewChatClick() },
                onNewContactClick = { onNewContactClick() },
                onNewGroupClick = { onNewGroupClick() },
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        switchSheetState()
                    }
                }
            )
        }
    ) {
        // Main screen content

    }

    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible && curBottomSheetState.value == SheetState.SHOW) {
            switchSheetState()
        }
    }
}

@Composable
fun BottomSheetContent(
    onNewChatClick: () -> Unit,
    onNewContactClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // New Chat Option
        BottomSheetOption(
            icon = R.drawable.ic_chat,
            title = "New Chat",
            description = "Send a message to your contact"
        ) {
            onNewChatClick()
        }

        Spacer(modifier = Modifier.height(4.dp))
        Divider(Modifier.background(Color.LightGray))
        Spacer(modifier = Modifier.height(4.dp))

        // New Contact Option
        BottomSheetOption(
            icon = R.drawable.ic_contact,
            title = "New Contact",
            description = "Add a contact to be able to send messages"
        ) {
            onNewContactClick()
        }

        Spacer(modifier = Modifier.height(4.dp))
        Divider(Modifier.background(Color.LightGray))
        Spacer(modifier = Modifier.height(4.dp))

        // New Community Option
        BottomSheetOption(
            icon = R.drawable.ic_community,
            title = "New Community",
            description = "Join the community around you"
        ) {
            onNewGroupClick()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel Button
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material.Text("Cancel", fontSize = 18.sp, color = Color.Black)
        }
    }
}

@Composable
fun BottomSheetOption(icon: Int, title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Icon",
            modifier = Modifier
                .padding(end = 16.dp)
                .size(28.dp),
            tint = Color.Black
        )
        Column {
            androidx.compose.material.Text(
                text = title,
                fontSize = 18.sp,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            androidx.compose.material.Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}


// Sample data classes
data class Story(val image: Int, val name: String)
data class Chat(
    val image: Int,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

// Sample data
val storyList = listOf(
    Story(R.drawable.chat_img1, "Terry"),
    Story(R.drawable.chat_img2, "Craig"),
    Story(R.drawable.chat_img3, "Roger"),
    Story(R.drawable.chat_img4, "Nolan"),
    Story(R.drawable.chat_img1, "Terry"),
    Story(R.drawable.chat_img2, "Craig"),
    Story(R.drawable.chat_img3, "Roger"),
    Story(R.drawable.chat_img4, "Nolan"),
    Story(R.drawable.chat_img1, "Terry"),
    Story(R.drawable.chat_img2, "Craig"),
    Story(R.drawable.chat_img3, "Roger"),
    Story(R.drawable.chat_img4, "Nolan")

)

val chatList = listOf(
    Chat(
        R.drawable.chat_img3,
        "Angel Curtis",
        "Please help me find a good monitor...",
        "02:11",
        2
    ),
    Chat(R.drawable.chat_img1, "Zaire Dorwart", "Gacor pisan kang", "02:11", 2),
    Chat(R.drawable.chat_img2, "Kelas Malam", "Bima: No one can come today?", "02:11", 2),
    Chat(R.drawable.chat_img3, "Jocelyn Gouse", "You're now an admin", "02:11", 0),
    Chat(R.drawable.chat_img4, "Jaylon Dias", "Buy back 10k gallons...", "02:11", 0),
    Chat(R.drawable.chat_img1, "Chance Rhiel Madsen", "Thank you mate!", "02:11", 2),
    Chat(
        R.drawable.chat_img3,
        "Angel Curtis",
        "Please help me find a good monitor...",
        "02:11",
        2
    ),
    Chat(R.drawable.chat_img1, "Zaire Dorwart", "Gacor pisan kang", "02:11", 2),
    Chat(R.drawable.chat_img2, "Kelas Malam", "Bima: No one can come today?", "02:11", 2),
    Chat(R.drawable.chat_img3, "Jocelyn Gouse", "You're now an admin", "02:11", 0),
    Chat(R.drawable.chat_img4, "Jaylon Dias", "Buy back 10k gallons...", "02:11", 0),
    Chat(R.drawable.chat_img1, "Chance Rhiel Madsen", "Thank you mate!", "02:11", 2),
    Chat(
        R.drawable.chat_img3,
        "Angel Curtis",
        "Please help me find a good monitor...",
        "02:11",
        2
    ),
    Chat(R.drawable.chat_img1, "Zaire Dorwart", "Gacor pisan kang", "02:11", 2),
    Chat(R.drawable.chat_img2, "Kelas Malam", "Bima: No one can come today?", "02:11", 2),
    Chat(R.drawable.chat_img3, "Jocelyn Gouse", "You're now an admin", "02:11", 0),
    Chat(R.drawable.chat_img4, "Jaylon Dias", "Buy back 10k gallons...", "02:11", 0),
    Chat(R.drawable.chat_img1, "Chance Rhiel Madsen", "Thank you mate!", "02:11", 2)
)

