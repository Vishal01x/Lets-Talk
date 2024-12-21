package com.exa.android.letstalk.presentation.Main.Home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.exa.android.letstalk.R

@Composable
fun ZoomPhoto(modifier: Modifier = Modifier, imageId: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .height(240.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = "to pop back stack",
            tint = Color.Black,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(alignment = Alignment.TopStart)
                .size(24.dp)
                .clickable {
                    onBack()
                }
        )
        Image(
            painter = painterResource(id = imageId), contentDescription = "ZoomedImage",
            contentScale = ContentScale.Crop
        )
    }
}
