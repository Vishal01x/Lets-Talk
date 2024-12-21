package com.exa.android.letstalk.presentation.auth.signUp

import android.util.Patterns
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.khacheri.screens.navigation.component.AuthRoute
import com.exa.android.letstalk.presentation.auth.viewmodels.AuthViewModel
import com.exa.android.letstalk.presentation.auth.components.AccountInfo
import com.exa.android.letstalk.presentation.auth.components.ShowLoader
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController : NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val viewModel : AuthViewModel = hiltViewModel()
    val authStatus by viewModel.authStatus.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authStatus) {
        when (authStatus) {
            is Response.Success -> {
                isLoading = false
                showToast(context,"User Successfully Login")
                navController.navigate("main_app") {
                    popUpTo("auth") { inclusive = true }
                }
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



    // Animate button color and background gradient
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
            Text(text = "Register", fontSize = 28.sp, color = Color.White, modifier = Modifier.padding(bottom = 24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() -> errorMessage = "Please enter a name."
                        email.isBlank() -> errorMessage = "Please enter an email."
                        password.isBlank() || password.length<8 -> errorMessage = "Please enter a 8 char password."
                        confirmPassword.isBlank() -> errorMessage = "Please confirm your password."
                        password != confirmPassword -> errorMessage = "Passwords do not match."
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches()-> errorMessage = "Please enter correct Email"
                        else -> {
                            isLoading = false
                            viewModel.registerUser(email,password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(buttonScale),
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                enabled = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
            ) {
                if (isLoading) {
                    ShowLoader()
                } else {
                    Text(text = "Register", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AccountInfo("Already have an account -> ") {
                navController.navigate(AuthRoute.Login.route)
            }
        }
    }
}
