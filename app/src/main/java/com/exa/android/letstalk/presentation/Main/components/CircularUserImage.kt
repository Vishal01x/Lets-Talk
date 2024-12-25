package com.exa.android.letstalk.presentation.Main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.exa.android.letstalk.R

@Composable
fun CircularUserImage(imageId : Int = R.drawable.chat_img3, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.chat_img3),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}