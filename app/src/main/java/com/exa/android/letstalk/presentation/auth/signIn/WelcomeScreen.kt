package com.exa.android.letstalk.presentation.auth.signIn
/*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.exa.android.letstalk.R

@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        ChatPreviewList() // Chat UI

        Spacer(modifier = Modifier.height(20.dp))

        PageIndicator() // Dots Indicator

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Welcome to Mengobrol",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Share with anyone, anywhere.\nA home for all the groups in your life.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { /* Handle sign in */ },
            colors = ButtonDefaults.buttonColors(Color(0xFFFFC107)), // Yellow
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
        ) {
            Text(text = "Sign in", fontSize = 18.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { /* Handle create account */ },
            border = BorderStroke(1.dp, Color.LightGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
        ) {
            Text(text = "Create an account", fontSize = 18.sp, color = Color.Black)
        }
    }
}

@Composable
fun ChatPreviewList() {
    val chatList = listOf(
        ChatItem("Angel Curtis", "Please help me find a good monitor for t...", "02:11", 2),
        ChatItem("Zaire Dorwart", "✔️ Gacor pisan kang", "02:11", 0),
        ChatItem("Kelas Malam", "Bima: No one can come today?", "02:11", 1),
        ChatItem("Jocelyn Gouse", "You're now an admin", "02:11", 0)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Restrict height
    ) {
        items(chatList) { chat ->
            ChatItemRow(chat)
        }
    }
}

@Composable
fun ChatItemRow(chat: ChatItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Profile",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = chat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = chat.message, color = Color.Gray, fontSize = 14.sp)
        }

        Text(text = chat.time, color = Color.Gray, fontSize = 12.sp)

        if (chat.unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            BadgeBox(chat.unreadCount)
        }
    }
}

@Composable
fun BadgeBox(count: Int) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(Color(0xFFFFC107), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun PageIndicator() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == 1) 8.dp else 6.dp)
                    .background(if (index == 1) Color.Black else Color.LightGray, CircleShape)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

data class ChatItem(val name: String, val message: String, val time: String, val unreadCount: Int)



@Preview(showBackground = true)
@Composable
fun PreviewWelcomeScreen() {
    WelcomeScreen()
}*/
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exa.android.letstalk.R
import com.exa.android.letstalk.presentation.navigation.component.AuthRoute
import com.exa.android.letstalk.ui.theme.LetsTalkTheme

data class ChatItem(val name: String, val message: String, val time: String, val unreadCount: Int)

@Composable
fun WelcomeScreen(SignIn: () -> Unit, CreateAccount: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Spacer(Modifier.weight(1f))

        // Circular Background Behind Chat Items
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .2f), shape = CircleShape)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                ChatItemRow(ChatItem("Angel Curtis", "Please help me find a good monitor for t...", "02:11", 2))
                ChatItemRow(ChatItem("Zaire Dorwart", "✔️ Gacor pisan kang", "02:11", 0))
                ChatItemRow(ChatItem("Kelas Malam", "Bima : No one can come today?", "02:11", 2))
                //ChatItemRow(ChatItem("Jocelyn Gouse", "You're now an admin", "02:11", 5))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Page Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (it == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Title & Description
        Text(
            text = "Welcome to Let's Talk",
            style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Share with anyone, anywhere.\nA home for all the groups in your life.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f)),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        // Buttons
        Button(
            onClick = { SignIn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(text = "Sign in", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { CreateAccount() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(text = "Create an account", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun ChatItemRow(chat: ChatItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.chat_img3),
            contentDescription = "Profile",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = chat.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = chat.message,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if(chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
            )
        }

        Text(text = chat.time, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))

        if (chat.unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            BadgeBox(chat.unreadCount)
        }
    }
}

@Composable
fun BadgeBox(count: Int) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        )
    }
}

@Preview(name = "Phone - Small", device = "spec:width=320dp,height=640dp,dpi=320")
@Preview(name = "Phone - Medium", device = "spec:width=411dp,height=891dp,dpi=440")
@Preview(name = "Phone - Large", device = "spec:width=540dp,height=960dp,dpi=480")
@Composable
fun PreviewChatScreen() {
    LetsTalkTheme(darkTheme = false, dynamicColor = false) {
//        WelcomeScreen {
//            navController.navigate(AuthRoute.Login.route)
//        }
    }
}
