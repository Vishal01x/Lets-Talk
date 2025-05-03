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
import com.exa.android.letstalk.utils.helperFun.getOtherUserName

@Composable
fun ChatListItem(chat: Chat, zoomImage: (Int) -> Unit, openChat: (userId: String) -> Unit) {

    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openChat(chat.id) }
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {

        CircularUserImage(
            context, chat.profilePicture ?: "", Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable {
                    zoomImage(R.drawable.chat_img3)
                }
        )

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            chat.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            chat.lastMessage,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.DarkGray,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val timestampInMillis = chat.lastMessageTimestamp.seconds * 1000L
                        Text(
                            formatTimestamp(timestampInMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        if (chat.unreadMessages > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Yellow)
                            ) {
                                Text(
                                    "${chat.unreadMessages}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
    }
