package com.exa.android.letstalk.data.domain.main.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.domain.main.repository.ZegoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZegoViewModel @Inject constructor(private val repository: ZegoRepository) : ViewModel() {

    val callActive = repository.callActive

    fun initZego(appID: Long, appSign: String, userID: String, userName: String) {
        viewModelScope.launch {
            repository.initZegoService(appID, appSign, userID, userName)
        }
        Log.d("CallEndListener", "Zego Service called with UserID: $userID")
    }

    fun startCall(inviteeList: List<String>, isVideo: Boolean) {
        viewModelScope.launch {
            repository.makeCall(inviteeList, isVideo)
        }
    }

    fun endCall() {
        viewModelScope.launch {
            repository.endCall()
        }
    }
}
