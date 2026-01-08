package com.exa.android.letstalk.presentation.auth.signUp

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.presentation.auth.components.InputField
import com.exa.android.letstalk.presentation.auth.components.ShowLoader
import com.exa.android.letstalk.presentation.auth.signIn.CircularIconButton
import com.exa.android.letstalk.presentation.auth.signIn.TabButton
import com.exa.android.letstalk.presentation.auth.viewmodels.AuthViewModel
import com.exa.android.letstalk.presentation.navigation.component.AuthRoute
import com.exa.android.letstalk.presentation.navigation.component.ProfileRoute
import com.exa.android.letstalk.presentation.navigation.component.ProfileType
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.showToast

@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val viewModel: AuthViewModel = hiltViewModel()
    val authStatus by viewModel.authStatus.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authStatus) {
        when (authStatus) {
            is Response.Success -> {
                Log.d("LoginScreen", "navigate to profile")
                isLoading = false
                navController.navigate(AuthRoute.CreateUser.route)
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
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            //.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button in Circular Card
            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = { navController.popBackStack() }
            )

            // More Options Button in Circular Card
            CircularIconButton(
                icon = Icons.Default.MoreVert,
                contentDescription = "More Options",
                modifier = Modifier.rotate(90f),
                onClick = { /* Handle More Options */ }
            )
        }
        Spacer(modifier = Modifier.weight(.2f))

        // Title
        Text(
            text = "Let's join with us",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Enter your phone number/social \n          " +
                    "account to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(.1f))

        // Tab Selector
        Text(
            "Register With Email", color = MaterialTheme.colorScheme.onTertiary,
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        InputField("xyz@gmail.com", Icons.Default.Email, email, onValueChange = { email = it })

        Spacer(modifier = Modifier.height(16.dp))

        InputField(
            "New Password",
            Icons.Default.Lock,
            newPassword,
            onValueChange = { newPassword = it })

        Spacer(modifier = Modifier.height(16.dp))

        InputField(
            "Confirm Password",
            Icons.Default.Lock,
            confirmPassword,
            onValueChange = { confirmPassword = it })

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty() && errorMessage != "User not logged in") {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        val isEnabled = email.isNotEmpty()


        Button(
            onClick = {
                when {
                    email.isBlank() -> errorMessage = "Please enter an email."
                    newPassword.isBlank() || newPassword.length < 8 -> errorMessage =
                        "Please enter a 8 char password."

                    confirmPassword.isBlank() -> errorMessage = "Please confirm your password."
                    newPassword != confirmPassword -> errorMessage = "Passwords do not match."
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorMessage =
                        "Please enter correct Email"

                    else -> {
                        isLoading = false
                        viewModel.registerUser(email, newPassword)
                    }
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
}

