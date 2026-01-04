package com.exa.android.letstalk.presentation.Main.Home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.exa.android.letstalk.R

@Composable
fun ZoomPhoto(
    imageUrl: String,
    onBack: () -> Unit
) {
    var imageState by remember {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // Async image
        AsyncImage(
            model = imageUrl,
            contentDescription = "Zoomed Photo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            onState = { state ->
                imageState = state
            }
        )


        if (imageState is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black
            )
        }

        if (imageState is AsyncImagePainter.State.Error) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.placeholder),
                    contentDescription = "Error",
                    modifier = Modifier.size(120.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Failed to load image",
                    color = Color.Gray
                )
            }
        }

        // 🔙 Back Button (always visible)
        Icon(
            painter = painterResource(R.drawable.arrow_back),
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
                .align(Alignment.TopStart)
                .clickable { onBack() }
        )
    }
}
