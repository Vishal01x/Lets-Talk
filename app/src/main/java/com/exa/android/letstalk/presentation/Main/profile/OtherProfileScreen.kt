package com.exa.android.letstalk.presentation.Main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exa.android.letstalk.R
import com.exa.android.letstalk.presentation.Main.Home.ViewModel.UserViewModel
import com.exa.android.letstalk.presentation.Main.components.CircularUserImage
import com.exa.android.letstalk.ui.theme.LetsTalkTheme
import com.exa.android.letstalk.core.utils.Response
import com.exa.android.letstalk.domain.User

@Composable
fun OtherProfileScreen(
    userId: String?,
    onCloseClick: () -> Unit,
    onVoiceClick: (User) -> Unit = {},
    onVideoClick: (User) -> Unit = {},
    userViewModel: UserViewModel = hiltViewModel()
) {
    val userResponse by userViewModel.userProfile.collectAsState()

    var user by remember { mutableStateOf(User()) }
    val context = LocalContext.current

    when (val response = userResponse) {
        is Response.Error -> {}
        Response.Loading -> {}
        is Response.Success -> {
            user = response.data
        }

        null -> {}
    }

    LaunchedEffect(Unit) {
        userViewModel.getUserProfile(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {

            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
                    .clip(CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Profile Picture
        CircularUserImage(context,user.profilePicture ?: "",null,Modifier.size(120.dp)
            .align(Alignment.CenterHorizontally)
            .padding(top = 4.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape))

        // Name and Bio
        Text(
            text = user.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp)
        )
        Text(
            text = user.about.ifBlank { "Be better, be proud of yourself." },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary.copy(.8f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Location and Phone
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary.copy(.8f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "New Delhi, India",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(.8f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                Icons.Default.Call,
                contentDescription = null,
                Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary.copy(.8f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = user.phone.ifBlank { "+000 000 000" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(.8f)
            )
        }

        // Audio, Video, Search Buttons with Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {onVoiceClick(user)},
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Audio", color = MaterialTheme.colorScheme.tertiary)
            }
            Button(
                onClick = {onVideoClick(user)},
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Video", color = MaterialTheme.colorScheme.tertiary)
            }
//            Button(
//                onClick = {},
//                shape = CircleShape,
//                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary.copy(.1f))
//            ) {
//                Icon(
//                    Icons.Default.Search,
//                    contentDescription = null,
//                    modifier = Modifier.size(18.dp),
//                    tint = MaterialTheme.colorScheme.secondary
//                )
//                Spacer(modifier = Modifier.width(6.dp))
//                Text("Search", color = MaterialTheme.colorScheme.secondary)
//            }
        }

        HorizontalDivider()

        // Story Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Story",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                "View all story",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(.8f)
            )
        }

        // Story List - 6 Dummy Stories (same for all users)
        if (true) {
            LazyRow(contentPadding = PaddingValues(end = 8.dp)) {
                items(6) { index ->
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .padding(end = 12.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(when (index) {
                                    0 -> R.drawable.chat_img1
                                    1 -> R.drawable.chat_img2
                                    2 -> R.drawable.chat_img3
                                    3 -> R.drawable.chat_img4
                                    4 -> R.drawable.status_img
                                    else -> R.drawable.chat_img1
                                })
                                .crossfade(true)
                                .placeholder(R.drawable.status_img)
                                .error(R.drawable.status_img)
                                .build(),
                            contentDescription = "Story Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        // Overlay text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(
                                        bottomStart = 12.dp,
                                        bottomEnd = 12.dp
                                    )
                                )
                                .padding(6.dp)
                        ) {
                            Text(
                                text = when (index) {
                                    0 -> "Cakep banget ini taneman, cocok bgt"
                                    1 -> "Kursi aja berdua, kamu madu sendiri 😁"
                                    2 -> "Ngopi dulu, septelgu dimana?"
                                    3 -> "Rural ang berdua, kamu masa sendiri 🥲"
                                    4 -> "Weekend vibes 🌟"
                                    else -> "New beginnings!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Preferences
        Text(
            "Preferences",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Media
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF1F1F1),
                tonalElevation = 2.dp,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.PermMedia,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Media",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Divider()

        // Starred Messages
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF1F1F1),
                tonalElevation = 2.dp,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Started Messages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}


@Preview(
    name = "Small Screen",
    widthDp = 320,
    heightDp = 640,
    showBackground = true
)
@Composable
private fun SmallScreenPreview() {
    LetsTalkTheme {
        Surface {
            OtherProfileScreen(null, {})
        }
    }
}

@Preview(
    name = "Medium Screen",
    widthDp = 411,
    heightDp = 891,
    showBackground = true
)
@Composable
private fun MediumScreenPreview() {
    LetsTalkTheme {
        Surface {
            OtherProfileScreen(null, {})
        }
    }
}

@Preview(
    name = "Large Screen",
    widthDp = 600,
    heightDp = 1024,
    showBackground = true
)
@Composable
private fun LargeScreenPreview() {
    LetsTalkTheme {
        Surface {
            OtherProfileScreen(null, {})
        }
    }
}
