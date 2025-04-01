package com.exa.android.letstalk.data.domain.main.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.domain.main.repository.UserRepository
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.Status
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.models.ScheduleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _chatRoomStatus = MutableLiveData<Status>(Status()) // Default status
    val chatRoomStatus: LiveData<Status> = _chatRoomStatus

    private val _chatRoomDetail =
        MutableStateFlow<Response<Chat?>>(Response.Loading) // Default loading state
    val chatRoomDetail: StateFlow<Response<Chat?>> = _chatRoomDetail

    private val _allUsers = MutableStateFlow<Response<List<User?>>>(Response.Loading)
    val allUsers: StateFlow<Response<List<User?>>> = _allUsers

    private val _curUserId = MutableStateFlow<String?>(null)
    val curUserId: StateFlow<String?> = _curUserId

    init {
        _curUserId.value = userRepository.currentUser
    }


    fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        viewModelScope.launch {
            userRepository.updateUserStatus(userId, isOnline)
        }
    }

    fun setTypingStatus(userId: String, toUserId: String?) {
        viewModelScope.launch {
            userRepository.setTypingStatus(userId, toUserId)
        }
    }

    fun updateUnreadMessages(chatId : String) {
        viewModelScope.launch {
            userRepository.updateUnreadMessages(chatId)
        }
    }

    fun observeUserConnectivity() {
        viewModelScope.launch {
            userRepository.observeUserConnectivity()
        }
    }

    fun observeChatRoomStatus(chatId: String, isGroup : Boolean = false) {
        userRepository.getChatRoomStatus(chatId, isGroup).observeForever { status ->
            _chatRoomStatus.value = status ?: Status() // Ensure non-null status
        }
    }

    fun getChatRoomDetail(chatId : String) {
        viewModelScope.launch {
            userRepository.getChatRoomDetail(chatId)
                .catch { exception ->
                    _chatRoomDetail.value =
                        Response.Error(exception.localizedMessage ?: "Error fetching user")
                }
                .collect { response ->
                    _chatRoomDetail.value = response
                }
        }
    }

    fun getAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUser()
                .catch { exception ->
                    _allUsers.value =
                        Response.Error(exception.localizedMessage ?: "Failed in getting All users")
                }
                .collect { response ->
                    _allUsers.value = response
                }
        }
    }

}
