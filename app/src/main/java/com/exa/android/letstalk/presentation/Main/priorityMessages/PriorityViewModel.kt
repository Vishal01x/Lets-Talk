package com.exa.android.letstalk.presentation.Main.priorityMessages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.repository.PriorityRepository
import com.exa.android.letstalk.core.utils.Response
import com.exa.android.letstalk.domain.Chat
import com.exa.android.letstalk.domain.Message
import com.exa.android.letstalk.domain.PriorityMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriorityViewModel @Inject constructor(private val repository: PriorityRepository) : ViewModel() {

    val isPriority = MutableStateFlow(false)

    val curUserId = MutableStateFlow("")

    private val _messages = MutableStateFlow<Response<List<PriorityMessage>>>(Response.Loading)
    val messages: StateFlow<Response<List<PriorityMessage>>> = _messages

    init {
        repository.currentUserId?.let { user ->
            curUserId.value = user
        }
    }

    fun sendPriority(userId : String, msg: Message, chat: Chat) {
        viewModelScope.launch {
            val priorityMessage = PriorityMessage(message = msg)
            repository.sendPriorityMessage(
                priorityMessage = priorityMessage,
                userId = userId
            )
            //isPriority.value = false
        }
    }


    fun getPriorityMessages() {
        viewModelScope.launch {
            repository.getPriorityMessages().collect {
                _messages.value = it
            }
        }
    }

    suspend fun getChatDetail(chatId : String) : Chat?{
        val chatDetail = repository.getChatDetails(chatId)
        return chatDetail
    }


}