package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.video

sealed class DownloadState {
    object NotStarted : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Completed : DownloadState()
    object Failed : DownloadState()
}
