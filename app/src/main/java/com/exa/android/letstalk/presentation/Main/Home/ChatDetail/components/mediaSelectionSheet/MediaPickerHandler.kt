package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.letstalk.data.domain.main.ViewModel.MediaSharingViewModel
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.mediaSelectionSheet.launchCameraWithPermission

@Composable
fun MediaPickerHandler(
    showAll : Boolean,
    onLaunch : (Uri) -> Unit,
    mediaSharingViewModel: MediaSharingViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current
    val showBottomSheet = remember {
        derivedStateOf {
            mediaSharingViewModel.showMediaPickerSheet
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            onLaunch(uri)
            // Default to image, determine type later if needed
        }
    }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        cameraImageUri.value?.let { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val byteArray = inputStream?.readBytes()

                if (byteArray != null && byteArray.isNotEmpty()) {
                    onLaunch(uri)
                } else {
                   // Log.e("Upload", "Captured image is empty")
                }
            } catch (e: Exception) {
               // Log.e("Upload", "Error reading image: ${e.message}")
            }
        }
    }


    if (showBottomSheet.value) {
        MediaPickerBottomSheet(
            showAll,
            onImageClick = {
                mediaSharingViewModel.showMediaPickerSheet = false
                launcher.launch("image/*")
            },
            onVideoClick = {
                mediaSharingViewModel.showMediaPickerSheet = false
                launcher.launch("video/*")
            },
            onDocumentClick = {
                mediaSharingViewModel.showMediaPickerSheet = false
                launcher.launch("*/*")
            },
            onCameraClick = {
                mediaSharingViewModel.showMediaPickerSheet = false
                // TODO: Add camera capture logic here with permission check
                if (activity != null) {
                    launchCameraWithPermission(
                        activity = activity,
                        onPermissionDenied = { },
                        onImageUriReady = { uri -> cameraImageUri.value = uri },
                        launchCamera = { uri -> takePictureLauncher.launch(uri) }
                    )
                }

            },
            onDismiss = { mediaSharingViewModel.showMediaPickerSheet = false }
        )
    }
}
