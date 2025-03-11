package com.exa.android.letstalk.presentation.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.letstalk.R


@Composable
fun ChatMessage(
    profileImage: Painter,
    senderName: String,
    messageText: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Circular Profile Image
        Image(
            painter = profileImage,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Message Bubble
        Column {
            Text(
                text = senderName,
                color = Color(0xFFBB86FC), // Purple shade
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Surface(
                color = Color(0xFF2A2A2A), // Dark bubble background
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = messageText,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = time,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewChatMessage() {
    ChatMessage(
        profileImage = painterResource(id = R.drawable.chat_img3), // Replace with actual image
        senderName = "Ankit LNCT",
        messageText = "https://youtu.be/n5bqjJSJzuo?s=i-6YaNmhKue5y6UW3U",
        time = "00:12"
    )
}
