package com.exa.android.khacheri.screens.Main.Home.ChatDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exa.android.khacheri.utils.helperFun.formatTimestamp
import com.exa.android.khacheri.utils.models.Chat
import com.exa.android.khacheri.utils.models.Status
import com.exa.android.khacheri.utils.models.User
import com.exa.android.letstalk.R


@Composable
fun ChatHeader(
//    profilePictureUrl: String,
    chat : Chat, // chatRoomDetails
    status: Status?, // status of other User
    curUser: String, // cur User Id
    members: List<User>, // all the members of chat
    selectedMessages: Int, // messages Selected to show its count
    onProfileClick: () -> Unit, // when otherUserProfile Click show its details
    onBackClick: () -> Unit, // navigate to ChatListDetail
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onUnselectClick: () -> Unit, // unselect all the messages
    onCopyClick: () -> Unit,
    onForwardClick: () -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        shape = RectangleShape,
        modifier = Modifier.clickable(enabled = (selectedMessages <= 0)) { onProfileClick() }
    ) {
        if (selectedMessages > 0) { // if messages are selected then show Header options and hide other profile
            HeaderWithOptions(
                selectedMessages = selectedMessages,
                onUnselectClick = { onUnselectClick() },
                onCopyClick = { onCopyClick() },
                onForwardClick = { onForwardClick() },
                onDeleteClick = { it -> onDeleteClick(it)}
                )
        } else {
            HeaderWithProfile( // when selectedMessages are 0 now show profile
                chat = chat,
                status = status,
                curUser = curUser,
                members = members,
                onBackClick = { onBackClick() },
                onCallClick = { /*TODO*/ },
                onVideoCallClick = { /*TODO*/ }
            )
        }
    }
}

@Composable
fun HeaderWithOptions(
    selectedMessages : Int,
    onUnselectClick: () -> Unit,
    onCopyClick: () -> Unit,
    onForwardClick: () -> Unit,
    onDeleteClick: (Int) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }

    PlaceIcons( // see docs
        selectedMessages = selectedMessages,
        startIcons = listOf(
            IconData(
                iconType = IconType.VectorIcon(Icons.AutoMirrored.Filled.ArrowBack),
                contentDescription = "Unselect fields",
                onClick = { onUnselectClick() }
            )
        ),
        endIcons = listOf(
            IconData(
                iconType = IconType.PainterIcon(R.drawable.ic_copy),
                contentDescription = "Copy Selected",
                onClick = { onCopyClick() }
            ),
            IconData(
                iconType = IconType.PainterIcon(R.drawable.ic_forward),
                contentDescription = "Forward Selected",
                onClick = { onForwardClick() }
            ),
            IconData(
                iconType = IconType.VectorIcon(Icons.Default.Delete),
                contentDescription = "Delete Selected",
                onClick = { showDialog = true }
            )
        )
    ) { iconData, index ->
        val rotation = if(index == 1)90f else 0f
        ShowIcon(iconData = iconData, rotationAngle = rotation)
        // since we use want to use lambda for only one icon but now we need to manage for all
    }

    if(showDialog){
        DeleteMessageDialog(
            onDelete = {deleteOption ->
                if(deleteOption == "Delete for Me"){
                    onDeleteClick(1)
                }else{
                    onDeleteClick(2)
                }
                showDialog = false
            },
            onCancel = {
                showDialog = false
            }
        )
    }
}

@Composable
fun PlaceIcons(
    selectedMessages : Int,
    startIcons: List<IconData>,
    endIcons: List<IconData>,
    iconContent: @Composable (iconData: IconData, index: Int) -> Unit = { iconData, _ ->
        ShowIcon(iconData = iconData) // it is created to maintain some operations on particular icon
        // like adding some modifiers, rotation etc functioning we make it default like you can utilize
        // when needed else it is optional, here we use it for forward icon in which we set its rotation to 180
        // and for the remaining one it remains no change and we directly call ShowIcon
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            startIcons.forEachIndexed { index, iconData ->
                iconContent(iconData, index)
            }

            Text(
                text = selectedMessages.toString(),
                color = Color.Black,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // for keeping endIcons at last

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            endIcons.forEachIndexed { index, iconData ->
                iconContent(iconData, index)
            }
        }
    }
}

@Composable
fun ShowIcon(iconData: IconData, rotationAngle : Float = 0f) {
    IconButton(onClick = { iconData.onClick() }) {
        when(val icon = iconData.iconType){
            is IconType.VectorIcon ->{
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = iconData.contentDescription,
                    modifier = Modifier
                        .rotate(rotationAngle).size(24.dp),
                    tint = Color.Black,
                    // for forward icon we rotate it opposite of reply
                )
            }
            is IconType.PainterIcon ->{
                Icon(
                    painter = painterResource(id = icon.painter),
                    contentDescription = iconData.contentDescription,
                    modifier = Modifier
                        .rotate(rotationAngle).size(24.dp),
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun HeaderWithProfile(
    chat : Chat,
    status: Status? = Status(),
    curUser: String,
    members: List<User>,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Profile Picture
        Image(
            painter = painterResource(id = R.drawable.chat_img3),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Black, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        // User Name and Status
        Column {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            if(!chat.isGroup){
            Text(
                text = when {
                    status!!.typingTo == curUser -> "typing..."
                    status.isOnline -> "Online"
                    status.lastSeen != null -> {
                        val timestamp = status.lastSeen * 1000L // already in seconds
                        val time = formatTimestamp(timestamp)
                        "last seen at ${time}"
                    }

                    else -> {
                        "Offline"
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )}else{
                Text(
                    text = when {
                        status!!.typingTo.isNotEmpty() -> "${status.typingTo}..."
                        else -> {
                            val membersName = members.joinToString(", ") { it.name }
                            membersName
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Video Call Icon
        IconButton(onClick = onVideoCallClick) {
            Icon(
                painter = painterResource(id = R.drawable.video),
                contentDescription = "Video Call",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // Call Icon
        IconButton(onClick = onCallClick) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun DeleteMessageDialog(
    onDelete: (deleteOption: String) -> Unit,
    onCancel: () -> Unit
) {
    // State to track the selected option
    var selectedOption by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            onCancel()
        },
        dismissButton = {
            OutlinedButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedOption?.let { onDelete(it) }
                },
                enabled = selectedOption != null, // Enabled only if an option is selected
                colors = ButtonDefaults.buttonColors(
                    if (selectedOption != null) Color.Red else Color.Gray
                )
            ) {
                Text(text = "Delete", color = Color.White)
            }
        },
        title = {
            Text(text = "Delete message?")
        },
        text = {
            Column {
                Text(text = "You can delete messages for everyone or just for yourself.")

                Spacer(modifier = Modifier.height(16.dp))

                // Radio Buttons for options
                val options = listOf("Delete for Me", "Delete for Everyone")
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == option,
                            onClick = { selectedOption = option }
                        )
                        Text(
                            text = option,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        modifier = Modifier.background(Color.White)
    )
}


sealed class IconType { // Since icon can be imageVector or painterResource we create a sealed class and utilize it further
    data class VectorIcon(val imageVector: ImageVector) : IconType()
    data class PainterIcon(val painter: Int) : IconType()
}

data class IconData(
    val iconType: IconType,
    val contentDescription: String,
    val onClick: () -> Unit
)// it's data class, we created it pass details of icon in form of list to another fun
// actually we are creating same ui for all icons so instead of redundant code we create a separate composable and pass list