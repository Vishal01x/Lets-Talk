package com.exa.android.letstalk.presentation.Main.Home.newChat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.letstalk.presentation.Main.Home.ViewModel.ChatViewModel
import com.exa.android.letstalk.presentation.Main.Home.ViewModel.MediaSharingViewModel
import com.exa.android.letstalk.presentation.Main.components.CircularUserImage
import com.exa.android.letstalk.presentation.Main.components.showLoader
import com.exa.android.letstalk.core.utils.Response
import com.exa.android.letstalk.domain.Chat
import com.exa.android.letstalk.core.utils.showToast
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler
import com.google.gson.Gson
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateGroupScreenUI(
    newGroupViewModel: NewGroupViewModel,
    onBack: () -> Unit,
    onGroupCreated: (String) -> Unit, chatViewModel: ChatViewModel = hiltViewModel(),
    mediaSharingViewModel : MediaSharingViewModel = hiltViewModel()
) {
    var groupName by remember { mutableStateOf(TextFieldValue("")) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var groupImage: Uri? by remember { mutableStateOf(null) }

    val members by newGroupViewModel.selectedUsers // CORRECT

    val context = LocalContext.current

    MediaPickerHandler(
        showAll = false,
        onLaunch = { uri ->
            groupImage = uri
        }
    )

    when(val response = newGroupViewModel.response.value){
        is Response.Error -> {}
        is Response.Loading -> {}
        is Response.Success -> {}
        null -> {}
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New group") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.onSecondary)
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newGroupViewModel.createGroup(
                        groupName.text,
                        groupImage,
                        members.toList(),
                        chatViewModel,
                        mediaSharingViewModel,
                        onSuccess = {chatId, profilePic->
                            val chat = Chat(id = chatId, name = groupName.text, profilePicture = profilePic, group = true)
                            val chatJson = Gson().toJson(chat)
                            val encodedChatJson = URLEncoder.encode(chatJson, "UTF-8")
                            onGroupCreated(encodedChatJson)
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create")
                }
            }
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        //.padding(padding)
                        //.padding(horizontal = 16.dp)
                        .fillMaxSize()
                ) {

                    DividerDefaults

                    Card(
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onSecondary),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable { /*showBottomSheet = true*/ mediaSharingViewModel.showMediaPickerSheet =
                                        true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (groupImage != null) {
                                    CircularUserImage(
                                        imageUri = groupImage,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Group Icon",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            TextField(
                                value = groupName,
                                onValueChange = { groupName = it },
                                label = {
                                    Text(
                                        "Group name",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        //Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Group Permissions",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = {}) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Members: ${members.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )


                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        members.forEach { user ->
                            val firstName = user.name.split(" ").firstOrNull() ?: "Name"
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularUserImage(
                                    imageUrl = user.profilePicture, modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )

                                Text(
                                    text = firstName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    if(newGroupViewModel.response.value is Response.Loading) {
                        showLoader(message = "Creating Group...")
                    }
                    if(newGroupViewModel.response.value is Response.Error){
                        showToast(context, "Group creation is failed. Try again")
                    }
                }

            }
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text("Group icon", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconOption(Icons.Default.PhotoCamera, "Camera") {}
                    IconOption(Icons.Default.PhotoLibrary, "Gallery") {}
                    IconOption(Icons.Default.Search, "Search web") {}
                }
            }
        }
    }
}

@Composable
fun IconOption(icon: ImageVector, label: String, onIconClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                .clickable { onIconClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary.copy(.5f)
        )
    }
}

//@Preview
//@Composable
//private fun prev() {
//    LetsTalkTheme {
//        CreateGroupScreenUI()
//    }
//}
