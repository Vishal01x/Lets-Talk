package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media

import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

fun getFileNameFromUrl(url: String): String {
        return Uri.parse(url).lastPathSegment ?: "downloaded_file"
    }

fun getMimeType(file: File): String {
    val extension = file.extension.lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "webp" -> "image/*"
        "mp4", "mkv", "webm", "3gp" -> "video/*"
        "mp3", "wav", "m4a" -> "audio/*"
        "pdf" -> "application/pdf"
        "doc", "docx" -> "application/msword"
        "ppt", "pptx" -> "application/vnd.ms-powerpoint"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "txt", "csv", "json", "xml" -> "text/plain"
        "zip" -> "application/zip"
        else -> "*/*" // fallback
    }
}
