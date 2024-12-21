package com.exa.android.letstalk.presentation.auth.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AccountInfo(text : String, onClick : () -> Unit) {
    Text(text, style = MaterialTheme.typography.titleSmall,
        color = Color.Blue, modifier = Modifier.clickable {
            onClick()
        })
}