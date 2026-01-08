package com.exa.android.letstalk.presentation.auth.signIn

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.letstalk.presentation.auth.components.InputField
import com.exa.android.letstalk.presentation.auth.components.isValidEmailAndPassword
import com.exa.android.letstalk.presentation.auth.signUp.HandleForgetPassword
import com.exa.android.letstalk.presentation.auth.viewmodels.AuthViewModel
import com.exa.android.letstalk.ui.theme.LetsTalkTheme

@Composable
fun LoginScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf("Email") }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabButton("Phone", selectedTab) { selectedTab = "Phone" }
            TabButton("Email", selectedTab) { selectedTab = "Email" }
            TabButton("Social", selectedTab) { selectedTab = "Social" }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            "Phone" -> HandlePhoneLogin()
            "Email" -> HandleEmailLogin(
                navController = navController
            ){
                selectedTab = "ForgetPassword"
            }
            "Social" -> SocialLoginButtons()
            else -> {HandleForgetPassword{
                selectedTab = "Email"
            }}
        }

    }
}

@Composable
fun TabButton(title: String, selectedTab: String, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(if (selectedTab == title) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary),
        elevation = ButtonDefaults.buttonElevation(4.dp),
        shape = CircleShape
    ) {
        Text(
            text = title,
            color = if (selectedTab == title) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary
        )
    }
}


@Composable
fun SocialLoginButtons() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SocialButton("Continue with Apple", Icons.Default.AccountCircle)
        SocialButton("Continue with Google", Icons.Default.AccountCircle)
        SocialButton("Continue with Facebook", Icons.Default.AccountCircle)
        SocialButton("Continue with Twitter", Icons.Default.AccountCircle)
    }
}

@Composable
fun SocialButton(text: String, icon: ImageVector) {
    Button(
        onClick = { /* Handle social login */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
    ) {
        Icon(icon, contentDescription = text, tint = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.Black)
    }
}


@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        //border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.size(40.dp) // Adjust size as needed
    ) {
        IconButton(onClick = onClick, modifier = modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                modifier = modifier,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(name = "Phone - Small", device = "spec:width=320dp,height=640dp,dpi=320")
@Preview(name = "Phone - Medium", device = "spec:width=411dp,height=891dp,dpi=440")
@Preview(name = "Phone - Large", device = "spec:width=540dp,height=960dp,dpi=480")
@Composable
fun PreviewJoinUsScreen() {
    LetsTalkTheme(darkTheme = true, dynamicColor = false) {
        LoginScreen(
            navController = NavController(LocalContext.current)
        )
    }
}

