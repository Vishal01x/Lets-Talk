package com.exa.android.letstalk.data.domain.main.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.domain.main.repository.FirestoreService
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.helperFun.generateMessage
import com.exa.android.letstalk.utils.models.Call
import com.exa.android.letstalk.utils.models.ScheduleType
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

    private val _curCall = MutableStateFlow<Call?>(null)
    val curCall : StateFlow<Call?> = _curCall

    val curUserId = MutableStateFlow("")
    val curUser = MutableStateFlow<User?>(null)

    init {
        viewModelScope.launch {
            // Set current user and fetch chat list
            repo.currentUserId?.let { user ->
                curUserId.value = user
                getChatList()
                getCurUser()
                repo.trackCall()
            } ?: run {
                _chatList.value = Response.Error("Current user is null")
            }
        }
    }

    private fun getCurUser(){
        viewModelScope.launch {
            curUser.value = repo.getCurUser()
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
        chat.name = "${curUser.value?.name}-${chat.name}"
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

    fun createChatAndSendMessage(message: Message) {
        viewModelScope.launch {
            repo.createChatAndSendMessage(message)
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
                repo.getChatList(curUserId.value).collect { response ->
                    _chatList.value = response
                }
        }
    }



    fun makeCall(call : Call, onSuccess : () -> Unit, onFailure : () -> Unit){
        viewModelScope.launch {
            repo.makeCall(call, onSuccess = onSuccess, onFailure = onFailure)
        }
    }

    fun trackCall(){
        viewModelScope.launch {
            repo.trackCall().collect { response ->
                when(val r = response){
                    is Response.Success -> {
                        _curCall.value = r.data
                    }
                    else ->{

                    }

                }

            }
        }
    }

}
