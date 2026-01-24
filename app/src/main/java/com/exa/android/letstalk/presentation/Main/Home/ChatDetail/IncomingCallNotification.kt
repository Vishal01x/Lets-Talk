package com.exa.android.letstalk.presentation.Main.Home.ChatDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.letstalk.R

@Composable
fun IncomingCallNotification(
    callerName: String,
    callerImage: Int = R.drawable.chat_img3,
    onDecline: () -> Unit,
    onAnswer: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = callerImage),
                    contentDescription = "Caller Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = callerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "Incoming voice call", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDecline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // Red
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DECLINE", color = Color.White)
                }
                Button(
                    onClick = onAnswer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Green
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ANSWER", color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewIncomingCallNotification() {
    IncomingCallNotification(
        callerName = "Aarti",
        callerImage = R.drawable.chat_img3, // Replace with actual drawable
        onDecline = {},
        onAnswer = {}
    )
}


