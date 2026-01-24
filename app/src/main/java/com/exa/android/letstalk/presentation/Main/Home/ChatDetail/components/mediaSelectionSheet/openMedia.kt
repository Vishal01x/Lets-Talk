package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.mediaSelectionSheet

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.exa.android.letstalk.domain.MediaType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun launchCameraWithPermission(
    activity: Activity,
    onPermissionDenied: () -> Unit,
    onImageUriReady: (Uri) -> Unit,
    launchCamera: (Uri) -> Unit
) {
    val permission = Manifest.permission.CAMERA
    when {
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED -> {
            val imageFile = createImageFile(activity)
            val imageUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.provider",
                imageFile
            )
            onImageUriReady(imageUri)
            launchCamera(imageUri)
        }
        else -> {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                1001
            )
            onPermissionDenied()
        }
    }
}

fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "JPEG_${timestamp}_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    return File.createTempFile(fileName, ".jpg", storageDir)
}


fun getMediaTypeFromUri(context: Context, uri: Uri?): MediaType {
    Log.d("Media type", uri.toString())
    if(uri == null)return MediaType.DOCUMENT

    val mimeType = context.contentResolver.getType(uri)
    val uriStr = uri.toString()

    return when {
        mimeType?.startsWith("image/") == true -> MediaType.IMAGE
        mimeType?.startsWith("video/") == true -> MediaType.VIDEO
        mimeType?.startsWith("audio/") == true -> MediaType.AUDIO
        mimeType == "application/pdf" -> MediaType.DOCUMENT
        mimeType?.contains("msword") == true ||
                mimeType?.contains("excel") == true ||
                mimeType?.contains("pdf") == true ||
                mimeType?.contains("powerpoint") == true -> MediaType.DOCUMENT

        mimeType?.contains("vcard") == true || uri.authority?.contains("contacts") == true -> MediaType.CONTACT
        uriStr.startsWith("geo:") || uriStr.contains("maps.google.com") -> MediaType.LOCATION
        else -> MediaType.DOCUMENT
    }
}