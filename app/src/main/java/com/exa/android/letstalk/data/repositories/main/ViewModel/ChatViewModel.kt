package com.exa.android.letstalk.data.repositories.main.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.repositories.main.repository.FirestoreService
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: FirestoreService
) : ViewModel() {

    private val _searchResult = MutableStateFlow<Response<User?>>(Response.Loading)
    val searchResult: StateFlow<Response<User?>> = _searchResult

    private val _messages = MutableStateFlow<Response<List<Message>>>(Response.Loading)
    val messages: StateFlow<Response<List<Message>>> = _messages

    private val _chatList = MutableStateFlow<Response<List<Chat>>>(Response.Loading)
    val chatList: StateFlow<Response<List<Chat>>> = _chatList

    private val _membersDetails = MutableStateFlow<Response<List<User>>>(Response.Loading)
    val membersDetail : StateFlow<Response<List<User>>> = _membersDetails

    val curUser = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Set current user and fetch chat list
            repo.currentUser?.let { user ->
                curUser.value = user
                getChatList()
            } ?: run {
                _chatList.value = Response.Error("Current user is null")
            }
        }
    }

    fun insertUser(userName: String, phone: String) {
        viewModelScope.launch {
            repo.insertUser(userName, phone)
        }
    }

    fun searchUser(phone: String) {
        viewModelScope.launch {
            repo.searchUser(phone).collect { response ->
                _searchResult.value = response
            }
        }
    }

    fun createChat(chat: Chat, onComplete: () -> Unit){
        viewModelScope.launch {
            repo.createChat(chat){
                onComplete()
            }
        }
    }

    fun createGroup(groupName : String, groupMembers : List<String>, onComplete : (String) -> Unit){
        viewModelScope.launch {
            repo.createGroup(groupName,groupMembers){chatId->
                Log.d("ChatRepo", "lambda retrived")
                onComplete(chatId)
            }
        }
    }

    fun updateUserChatList(currentUser : String, otherUser : String){
        viewModelScope.launch {
            repo.updateUserChatList(currentUser,otherUser)
        }
    }

    fun createChatAndSendMessage(chatId: String, message: String, replyTo : Message?, members : List<String>) {
        viewModelScope.launch {
            repo.createChatAndSendMessage(chatId, message, replyTo, members)
        }
    }

    fun forwardMessages(forwardMessages: List<String>, receivers: List<User>) {
        viewModelScope.launch {
            repo.forwardMessages(forwardMessages,receivers)
        }
    }

    fun deleteMessages(messages : List<String>, chatId: String, deleteFor : Int, onCleared :() -> Unit){
        viewModelScope.launch {
            repo.deleteMessages(messages,chatId,deleteFor){
                onCleared()
            }

        }
    }


    fun getMessages(chatId : String) {
        viewModelScope.launch {
            repo.getMessages(chatId).collect { response ->
                _messages.value = response
            }
        }
    }

    fun fetchChatMembersDetails(chatId: String) {
        viewModelScope.launch {
            repo.getChatMembers(chatId).flatMapConcat { userIds ->
                repo.getUsersDetails(userIds)
            }.collect { response ->
                _membersDetails.value = response
            }
        }
    }

    fun getChatList() {
        viewModelScope.launch {
            repo.getChatList(curUser.value).collect { response ->
                _chatList.value = response
            }
        }
    }
}
