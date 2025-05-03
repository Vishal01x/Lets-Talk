package com.exa.android.letstalk.presentation.Main.Home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.exa.android.letstalk.R
import com.exa.android.letstalk.presentation.Main.components.CircularUserImage

@Composable
fun StoryItem(image: Int, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {


        Image(painter = painterResource(R.drawable.chat_img3),
            contentDescription = "",
            modifier = Modifier
            .size(58.dp)
            .clip(CircleShape))

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            name,
            style = MaterialTheme.typography.titleSmall,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold
        )
    }
}
