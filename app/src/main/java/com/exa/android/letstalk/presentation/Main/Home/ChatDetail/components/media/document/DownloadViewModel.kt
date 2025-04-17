package com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.document

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DocumentDownloadViewModel @Inject constructor(
    private val context: Context,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _downloadState = MutableLiveData<DownloadState>()
    val downloadState: LiveData<DownloadState> = _downloadState

    private var downloadJob: Job? = null
    private var currentFile: File? = null

    fun startDownload(url: String, fileName: String) {
        // Downloading logic (with pause, resume, retry)
        downloadJob = viewModelScope.launch {
            _downloadState.value = DownloadState.Downloading(0)
            try {
                val file = fileRepository.downloadFile(url, fileName) { progress ->
                    _downloadState.value = DownloadState.Downloading(progress)
                }
                currentFile = file
                _downloadState.value = DownloadState.Completed(file)
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Failed(e)
            }
        }
    }

    fun pauseDownload() {
        downloadJob?.cancel()
        _downloadState.value = DownloadState.Paused
    }

    fun resumeDownload(url: String, fileName: String) {
        startDownload(url, fileName) // Resuming by restarting download
    }

    fun retryDownload(url: String, fileName: String) {
        startDownload(url, fileName) // Retry logic
    }

    fun openDocument() {
        currentFile?.let {
            val uri = FileProvider.getUriForFile(
                context,
                "com.exa.android.letstalk.provider", // Make sure this matches the manifest authority
                it
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }
}


sealed class DownloadState {
    object Paused : DownloadState()
    data class Failed(val error: Exception) : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data class Completed(val file: File) : DownloadState()
}
