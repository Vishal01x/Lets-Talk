package com.exa.android.letstalk.presentation.auth.signIn

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.presentation.navigation.component.AuthRoute
import com.exa.android.letstalk.presentation.auth.viewmodels.AuthViewModel
import com.exa.android.letstalk.presentation.auth.components.AccountInfo
import com.exa.android.letstalk.presentation.auth.components.ShowLoader
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.showToast


@Composable
fun LoginScreen(navController: NavController) {
    val viewModel : AuthViewModel = hiltViewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val authStatus by viewModel.authStatus.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(authStatus) {
        when (authStatus) {
            is Response.Success -> {
                isLoading = false
                showToast(context, "User Successfully Login")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display error message if present
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Login Button with loading indicator
        Button(
            onClick = {
                if (email.isBlank()) {
                    errorMessage = "Please enter an email."
                } else if(password.length < 8){
                    errorMessage = "Please enter least 8 characters password"
                }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    errorMessage = "Enter correct email"
                } else {
                    // Start loading and perform login operation
                    viewModel.loginUser(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            if (isLoading) {
                ShowLoader()
            } else {
                Text(text = "Login")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row {
            AccountInfo("Create New Account") {
                navController.navigate(AuthRoute.Register.route)
            }

            AccountInfo(" ? Forget Password") {
                navController.navigate(AuthRoute.ForgetPassword.route)
            }
        }
    }
}
