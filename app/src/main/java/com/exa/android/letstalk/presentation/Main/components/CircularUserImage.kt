package com.exa.android.letstalk.presentation.Main.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exa.android.letstalk.R

@Composable
fun CircularUserImage(context : Context = LocalContext.current, imageUrl : String? = null, imageUri : Uri? = null, modifier: Modifier = Modifier) {
    val image = imageUrl ?: imageUri ?: ""
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(image)
            .crossfade(true)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .build(),
        contentDescription = "Profile Picture",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}