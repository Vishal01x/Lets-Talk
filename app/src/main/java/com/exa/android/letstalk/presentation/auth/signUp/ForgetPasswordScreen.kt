package com.exa.android.letstalk.presentation.auth.signUp

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.presentation.auth.components.InputField
import com.exa.android.letstalk.presentation.auth.components.ShowLoader
import com.exa.android.letstalk.presentation.auth.viewmodels.AuthViewModel
import com.exa.android.letstalk.core.utils.Response
import com.exa.android.letstalk.core.utils.showToast

@Composable
fun HandleForgetPassword(onComplete : () -> Unit) {
    val viewModel: AuthViewModel = hiltViewModel()
    val authStatus by viewModel.authStatus.collectAsState()
    val context = LocalContext.current
    var isLoading by remember {
        mutableStateOf(false)
    }
    var email by remember {
        mutableStateOf("")
    }
    var errorMessage by remember {
        mutableStateOf("")
    }
    LaunchedEffect(authStatus) {
        when (authStatus) {
            is Response.Success -> {
                isLoading = false
                showToast(context,"Reset Password link is shared to email")
                onComplete()
            }

            is Response.Error -> {
                isLoading = false
                errorMessage = (authStatus as Response.Error).message

            }

            is Response.Loading -> {
                isLoading = true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {


        InputField(
            "youremail@xyz.com", Icons.Default.Email, email, errorMessage,
            onValueChange = { email = it },
            onImeAction = {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.resetPassword(email)
                } else {
                    errorMessage = "Correct Your Email"
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty() && errorMessage != "User not logged in") {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Email Login ->",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable {
                onComplete()
            })

        Spacer(Modifier.weight(1f))

        val isEnabled = email.isNotEmpty()


        Button(
            onClick = {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.resetPassword(email)
                } else {
                    errorMessage = "Correct Your Email"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(if (isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface),
            enabled = isEnabled
        ) {
            if (isLoading) ShowLoader()
            else {
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


   /* // Animate button color and background gradient
    val backgroundColor by animateColorAsState(
        targetValue = if (isLoading) Color(0xFFB3E5FC) else Color(0xFF03A9F4),
        animationSpec = tween(durationMillis = 500)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF03A9F4), Color(0xFFB3E5FC))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Forget Password",
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display error message if present
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() -> errorMessage = "Please enter an email."
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorMessage =
                            "Please enter correct Email"
                        else -> {
                            isLoading = false
                            viewModel.resetPassword(email)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(buttonScale),
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                enabled = email.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = "Retrive Password", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AccountInfo("Back to Login -> ") {
                navController.navigate(AuthRoute.Login.route)
            }

        }
    }*/
}