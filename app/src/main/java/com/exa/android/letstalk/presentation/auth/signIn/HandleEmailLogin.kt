package com.exa.android.letstalk.presentation.auth.signIn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.presentation.auth.viewmodels.AuthViewModel
import com.exa.android.letstalk.presentation.auth.components.InputField
import com.exa.android.letstalk.presentation.auth.components.ShowLoader
import com.exa.android.letstalk.presentation.auth.components.isValidEmailAndPassword
import com.exa.android.letstalk.presentation.navigation.component.AuthRoute
import com.exa.android.letstalk.core.utils.Response


@Composable
fun HandleEmailLogin(
    navController: NavController,
    onForgetPasswordClick: () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
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
                //showToast(context, "User Successfully Login")
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


    Column(modifier = Modifier.fillMaxSize()) {
        //  Text(text = "Login", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

        InputField(
            "youremail@xyz.com", Icons.Default.Email, email, "errorMessage",
            onValueChange = { email = it }
        ) { }

        Spacer(modifier = Modifier.height(16.dp))

        InputField(
            "password", Icons.Default.Lock, password,
            error = errorMessage,
            onValueChange = { password = it }
        ) {
            isValidEmailAndPassword(email, password) {
                if (it.isEmpty()) viewModel.loginUser(email, password)
                else errorMessage = it
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty() && errorMessage != "User not logged in") {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Forget Password ->",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable {
                onForgetPasswordClick()
            })

        Spacer(modifier = Modifier.weight(1f))

        val isEnabled = email.isNotEmpty() && password.isNotEmpty()


        Button(
            onClick = {
                isValidEmailAndPassword(email, password) { // errorMessage
                    if (it.isEmpty()) viewModel.loginUser(email, password)
                    else errorMessage = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface),
            enabled = isEnabled
        ) {
            if (isLoading) ShowLoader()
            else {
                Text(
                    text = "Continue",
                    color = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        .7f
                    ),
                    fontWeight = FontWeight.Medium
                )
            }
        }

    }

//        Spacer(modifier = Modifier.height(48.dp))
//
//        Row {
//            AccountInfo("Create New Account") {
//                navController.navigate(AuthRoute.Register.route)
//            }
//
//            AccountInfo(" ? Forget Password") {
//                navController.navigate(AuthRoute.ForgetPassword.route)
//            }
//        }
}

