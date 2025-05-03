package com.exa.android.letstalk.presentation.Main.profile


import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exa.android.letstalk.R
import com.exa.android.letstalk.data.domain.main.ViewModel.MediaSharingViewModel
import com.exa.android.letstalk.data.domain.main.ViewModel.UserViewModel
import com.exa.android.letstalk.presentation.auth.viewmodels.AuthViewModel
import com.exa.android.letstalk.presentation.navigation.component.ProfileType
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.utils.showToast
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler


@Composable
fun UserProfileScreen(
    userId: String? = null,
    profileType: ProfileType,
    onProfileSave: (() -> Unit)? = null,
    mediaSharingViewModel: MediaSharingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val mockUser = User(
        name = "John Doe",
        about = "Android Developer",
        birthDate = "Jan 1, 1990",
        phone = "1234567890",
        socialMedia = "@johndoe",
        profilePicture = "https://res.cloudinary.com/dgqxusedq/raw/upload/v1745431643/1000238567_yswicn.jpg" // Provide image URL or empty
    )

    LaunchedEffect(userId) {
        if (profileType != ProfileType.SIGNUP_PROFILE) {
            userViewModel.getUserProfile(userId)
        }
    }

    ProfileScreen(userId, profileType,
        authViewModel, userViewModel, onSave = { user, imageUri ->
            authViewModel.updateOrCreateUser(
                userId,
                user,
                imageUri,
                mediaSharingViewModel
            )
        }, onImageEdit = {
            mediaSharingViewModel.showMediaPickerSheet = true
        }, onSettingsClick = {

        },
        onProfileSave = {
            if (onProfileSave != null) {
                onProfileSave()
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    profileType: ProfileType,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    onSave: (User, Uri?) -> Unit,
    onImageEdit: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileSave: () -> Unit
) {
    val user by userViewModel.userProfile.collectAsState()
    val saveResponse = authViewModel.userUpdateState.value

    var editableUser by remember { mutableStateOf(User()) }
    var isEditing by remember { mutableStateOf(profileType == ProfileType.SIGNUP_PROFILE) }
    var isSaving by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showScreenLoading by remember { mutableStateOf(false) }
    var showButtonLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    LaunchedEffect(user) {
        when (val response = user) {
            is Response.Error -> {
                showScreenLoading = false
                showSnackbar = true
                snackbarHostState.showSnackbar(
                    message = "Failed to Load Profile. Retry?",
                    actionLabel = "Retry"
                ).let {
                    if (it == SnackbarResult.ActionPerformed) {
                        userViewModel.getUserProfile(userId)
                    }
                }
            }

            Response.Loading -> {
                showScreenLoading = true
            }

            is Response.Success -> {
                showScreenLoading = false
                editableUser = response.data
            }

            null -> {}
        }
    }



    MediaPickerHandler(
        showAll = false,
        onLaunch = { uri ->
            imageUri = uri
        }
    )

    LaunchedEffect(saveResponse) {
        when (val response = saveResponse) {
            is Response.Error -> {
                showButtonLoading = false
                showSnackbar = true
                snackbarHostState.showSnackbar(
                    message = "Failed to save. Retry?",
                    actionLabel = "Retry"
                ).let {
                    if (it == SnackbarResult.ActionPerformed) {
                        onSave(editableUser, imageUri)
                    }
                }
            }

            Response.Loading -> {
                showButtonLoading = true
            }

            is Response.Success -> {
                showButtonLoading = false
                if (profileType == ProfileType.SIGNUP_PROFILE) {
                    onProfileSave()
                } else {
                    isEditing = false
                    showToast(context, "Profile updated successfully")
                }
            }

            null -> {}
        }
    }

    Scaffold(
        topBar = {
            if (profileType != ProfileType.SIGNUP_PROFILE) {
                TopAppBar(
                    title = { Text("Profile Screen", style = MaterialTheme.typography.titleLarge) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.shadow(4.dp),
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (isEditing) {
                FloatingActionButton(
                    onClick = {
                        if (editableUser.name.isBlank()) {
                            nameError = "Name can't be empty"
                        } else {
                            onSave(editableUser, imageUri)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (showButtonLoading) CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    else if (profileType == ProfileType.SIGNUP_PROFILE) {
                        Text("Save", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    } else Icon(Icons.Default.Check, "Save")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .clickable(enabled = isEditing) { onImageEdit() }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUri ?: editableUser.profilePicture)
                            .crossfade(true)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isEditing) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.BottomCenter)
                                    .offset(y = (-10).dp)
                            )
                        }
                    }
                }

                if (profileType == ProfileType.MY_PROFILE) {
                    IconButton(
                        onClick = { isEditing = !isEditing },
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = "Toggle Edit",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // User Information Section
            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                EditableTextField(
                    value = editableUser.name,
                    label = "Name",
                    isEditing = isEditing,
                    error = nameError,
                    onValueChange = {
                        nameError = null
                        editableUser = editableUser.copy(name = it)
                    }
                )

                Spacer(Modifier.height(12.dp))

                EditableTextField(
                    value = editableUser.about,
                    label = "About",
                    isEditing = isEditing,
                    multiLine = true,
                    onValueChange = { editableUser = editableUser.copy(about = it) }
                )
            }

            Divider(Modifier.padding(horizontal = 24.dp, vertical = 12.dp))

            Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text(
                    "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(8.dp))

                InfoRow(
                    icon = Icons.Default.Cake,
                    label = "Birth Date",
                    value = editableUser.birthDate,
                    isEditing = isEditing,
                    onValueChange = { editableUser = editableUser.copy(birthDate = it) }
                )

                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "Phone",
                    value = editableUser.phone,
                    isEditing = isEditing,
                    onValueChange = { editableUser = editableUser.copy(phone = it) }
                )

                InfoRow(
                    icon = Icons.Default.Link,
                    label = "Social Media",
                    value = editableUser.socialMedia,
                    isEditing = isEditing,
                    onValueChange = { editableUser = editableUser.copy(socialMedia = it) }
                )
            }
        }

        if (showScreenLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun EditableTextField(
    value: String,
    label: String,
    isEditing: Boolean,
    error: String? = null,
    multiLine: Boolean = false,
    onValueChange: (String) -> Unit
) {
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            maxLines = if (multiLine) 3 else 1
        )
    } else {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f)
            )
            Text(
                text = value.ifEmpty { "Not provided" },
                style = if (multiLine) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        EditableTextField(
            value = value,
            label = label,
            isEditing = isEditing,
            onValueChange = onValueChange
        )
    }
}



