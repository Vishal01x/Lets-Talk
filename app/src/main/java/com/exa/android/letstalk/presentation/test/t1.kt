package com.exa.android.letstalk.presentation.test

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.exa.android.letstalk.utils.showToast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

// Cloudinary response data
data class CloudinaryUploadResponse(
    val secure_url: String
)

// Cloudinary Retrofit API
interface CloudinaryApi {
    @Multipart
    @POST("v1_1/dgqxusedq/raw/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryUploadResponse
}

@Composable
fun FileUploaderDownloaderUI() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uploadedUrl by remember { mutableStateOf<String?>(null) }
    var showDownloading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val file = createTempFileFromUri(context, uri)
//                val url = uploadFileToCloudinary(file)
//                uploadedUrl = url
//                url?.let { saveUrlToFirestore(it) }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showToast(context, "Permission denied. Media download not allowed.")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { filePickerLauncher.launch("*/*") }) {
            Text("Upload File to Cloudinary")
        }

        Spacer(Modifier.height(16.dp))

        uploadedUrl?.let { url ->
            Text("Uploaded URL:\n$url", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(permission)
                } else {
                    showDownloading = true
                    downloadFile(context, url, "downloaded_file")
                }
            }) {
                Text("Download File")
            }
        }

        if (showDownloading) {
            Text("Downloading started...", style = MaterialTheme.typography.bodySmall)
        }
    }
}



suspend fun createTempFileFromUri(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
    Log.d("Storage Cloudinary", "Creating temp file from URI: $uri")
    val inputStream = context.contentResolver.openInputStream(uri)
    val fileExtension = MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "tmp"
    val file = File.createTempFile("upload", ".$fileExtension", context.cacheDir)
    file.outputStream().use { output ->
        inputStream?.copyTo(output)
    }
    Log.d("Storage Cloudinary", "Temp file created: ${file.absolutePath}")
    file
}

fun downloadFile(context: Context, url: String, fileName: String) {
    Log.d("Storage Cloudinary", "Starting download from $url")

    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle(fileName)
        .setDescription("Downloading file...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "$fileName.${getFileExtensionFromUrl(url)}"
        )
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)

    Log.d("Storage Cloudinary", "Download enqueued for $fileName")
}

//fun getFileExtensionFromUrl(url: String): String {
//    return Uri.parse(url).lastPathSegment?.substringAfterLast('.', "pdf") ?: "pdf"
//}

fun getFileExtensionFromUrl(url: String): String {
    val uri = Uri.parse(url)
    val path = uri.lastPathSegment ?: return "pdf"

    val extension = path.substringAfterLast('.', "").lowercase()
    if (extension.isNotBlank()) return extension

    // Fallback: Guess from MIME type
    val mimeType = MimeTypeMap.getFileExtensionFromUrl(url)
    return if (mimeType.isNullOrBlank()) "pdf" else mimeType.lowercase()
}

