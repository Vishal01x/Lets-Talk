package com.exa.android.letstalk.presentation.auth.signIn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.letstalk.presentation.auth.components.InputField
import com.exa.android.letstalk.presentation.auth.components.isValidEmailAndPassword

@Composable
fun HandlePhoneLogin(modifier: Modifier = Modifier) {

    var phoneNumber by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        InputField(
            "Phone Number", Icons.Default.Phone, phoneNumber,
            error = error,
            onValueChange = { phoneNumber = it }
        ) {}


        Spacer(modifier = Modifier.weight(1f))

        val isEnabled = phoneNumber.isNotEmpty()


        Button(
            onClick = {

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(if (isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface),
            enabled = isEnabled
        ) {
            Text(
                text = "Continue",
                color = if (isEnabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    .7f
                ),
                fontWeight = FontWeight.Medium
            )
        }
    }

}