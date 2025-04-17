package com.exa.android.letstalk.presentation.test

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun MediaUploaderScreen(context: Context) {
    val coroutineScope = rememberCoroutineScope()

    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            coroutineScope.launch {
                uploadMediaToCloudinary(context, uri) { uploadedUrl ->
                    saveMediaUrlToFirebase(uploadedUrl)
                }
            }
        }
    }

    Button(onClick = { mediaLauncher.launch("*/*") }) {
        Text("Select Media")
    }
}

suspend fun uploadMediaToCloudinary(
    context: Context,
    uri: Uri,
    onResult: (String) -> Unit
) {
    val cloudinary = Cloudinary(ObjectUtils.asMap(
        "cloud_name", "dgqxusedq", // cloud name
        "api_key", "777611464262553",  //"YOUR_API_KEY",
        "api_secret", "Ed236uVJCWisZSKj2VGLqGCBuYM"  //"YOUR_API_SECRET"
    ))

    val file = getFileFromUri(context, uri)

    withContext(Dispatchers.IO) {
        try {
            val uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
            val url = uploadResult["secure_url"] as String
            withContext(Dispatchers.Main) {
                onResult(url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun getFileFromUri(context: Context, uri: Uri): File {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "tempFile")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}

fun saveMediaUrlToFirebase(mediaUrl: String) {
    val message = hashMapOf(
        "mediaUrl" to mediaUrl,
        "timestamp" to System.currentTimeMillis()
    )
    FirebaseFirestore.getInstance()
        .collection("media")
        .add(message)
        .addOnSuccessListener { Log.d("Firebase", "URL saved") }
        .addOnFailureListener { Log.e("Firebase", "Failed to save") }
}

fun downloadMediaToDevice(context: Context, url: String, fileName: String) {
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("Downloading media")
        .setDescription("Please wait...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}
