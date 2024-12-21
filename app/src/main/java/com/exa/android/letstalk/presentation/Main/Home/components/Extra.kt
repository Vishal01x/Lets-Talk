package com.exa.android.letstalk.presentation.Main.Home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.khacheri.utils.helperFun.formatTimestamp
import com.exa.android.khacheri.utils.models.Chat
import com.exa.android.letstalk.R







/*
@Composable
fun HomeListItem(chat: String, zoomImage: (Int) -> Unit, openChat: () -> Unit) {
     Card(
          elevation = CardDefaults.cardElevation(8.dp),
          modifier = Modifier
              .padding(vertical = 8.dp, horizontal = 16.dp)
              .fillMaxWidth()
              .clickable {
                  openChat()
              },
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)

      ) {
          Row(
              modifier = Modifier
                  .padding(16.dp)
                  .fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
          ) {
              Image(
                  painter = painterResource(id = R.drawable.ic_launcher_background),
                  contentDescription = "Profile Picture",
                  modifier = Modifier
                      .size(56.dp)
                      .clip(CircleShape)
                      .shadow(8.dp, shape = CircleShape)
                      .clickable {
                          zoomImage(R.drawable.ic_launcher_background)
                      },
                  contentScale = ContentScale.Crop
              )

              Spacer(modifier = Modifier.width(12.dp))

              Column(
                  modifier = Modifier
                      .weight(1f)
                      .padding(end = 8.dp)

              ) {
                  Text(
                      text = chat,
                      fontSize = 16.sp,
                      maxLines = 1,
                      fontWeight = FontWeight.Bold,
                      overflow = TextOverflow.Ellipsis
                  )
                  Text(
                      text = "What is the status of your work What is the status of your work",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      style = MaterialTheme.typography.bodyMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                  )
              }
              Column(
                  horizontalAlignment = Alignment.End,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                  Text(
                      text = "12.20",
                      fontSize = 12.sp,
                      color = Color.Gray
                  )
                  val unreadMessage = 2
                  if (unreadMessage > 0) {
                      Box(
                          modifier = Modifier
                              .size(24.dp)
                              .background(Color.Red, shape = CircleShape),
                          contentAlignment = Alignment.Center
                      ) {
                          Text(
                              text = unreadMessage.toString(),
                              fontSize = 12.sp,
                              color = Color.White,
                              fontWeight = FontWeight.Bold
                          )
                      }
                  }
              }
          }
      }
}
*/
