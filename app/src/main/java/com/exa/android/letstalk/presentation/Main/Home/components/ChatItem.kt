package com.exa.android.letstalk.presentation.Main.Home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.letstalk.utils.helperFun.formatTimestamp
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.R
import com.exa.android.letstalk.presentation.Main.components.CircularUserImage
import com.exa.android.letstalk.ui.theme.AppColors
import com.exa.android.letstalk.utils.helperFun.getOtherUserName

@Composable
fun ChatListItem(chat: Chat, zoomImage: (String?) -> Unit, openChat: (userId: String) -> Unit) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openChat(chat.id) }
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        // Avatar with online indicator
        Box {
            CircularUserImage(
                context, chat.profilePicture ?: "", null, Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable {
                        zoomImage(chat.profilePicture)
                    }
            )
            // Online indicator (small green dot) - you can make this conditional based on online status
            // Uncomment when you have online status logic
            /*
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(AppColors.onlineIndicator)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .align(Alignment.BottomEnd)
            )
            */
        }

        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                chat.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Double checkmark for read status (make conditional based on message status)
                if (chat.lastMessage.isNotEmpty()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_seen),
                        contentDescription = "Read",
                        tint = AppColors.checkmarkRead,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                Text(
                    chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val timestampInMillis = chat.lastMessageTimestamp.seconds * 1000L
            Text(
                formatTimestamp(timestampInMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            
            if (chat.unreadMessages > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(AppColors.unreadBadge)
                ) {
                    Text(
                        "${chat.unreadMessages}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.messageTextOnYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
