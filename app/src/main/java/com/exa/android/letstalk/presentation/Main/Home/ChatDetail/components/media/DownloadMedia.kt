package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log

fun downloadMedia(context: Context, mediaUrl: String, fileName: String) {
    Log.d("Storage Cloudinary", "Starting download from $mediaUrl")

    val request = DownloadManager.Request(Uri.parse(mediaUrl))
        .setTitle(fileName)
        .setDescription("Downloading file...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(
            context, "Let's Talk", fileName
        )
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)

    Log.d("Storage Cloudinary", "Download enqueued for $fileName")
}