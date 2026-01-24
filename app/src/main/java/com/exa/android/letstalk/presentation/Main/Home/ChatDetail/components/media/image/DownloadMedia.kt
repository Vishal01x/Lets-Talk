package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.image

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.exa.android.letstalk.core.utils.Constants.APP_NAME

fun downloadImageToAppFolder(context: Context, imageUrl: String, fileName: String = "image.jpg") {
    val request = DownloadManager.Request(Uri.parse(imageUrl))
        .setTitle("Downloading image")
        .setDescription("Please wait...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(context, APP_NAME, fileName)

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}
