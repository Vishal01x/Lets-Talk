package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components

import androidx.compose.runtime.Composable
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.Status
import com.exa.android.letstalk.utils.models.User

/*@Composable
fun HeaderWithProfile(
    chat: Chat,
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
}*/