package com.exa.android.letstalk.presentation.Main.Home.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.repository.UserRepository
import com.exa.android.letstalk.domain.Chat
import com.exa.android.letstalk.domain.Status
import com.exa.android.letstalk.domain.User
import com.exa.android.letstalk.core.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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

    private val _userProfile = MutableStateFlow<Response<User>?>(null)
    val userProfile: StateFlow<Response<User>?> = _userProfile

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

    fun updateUnreadMessages(chatId: String) {
        viewModelScope.launch {
            userRepository.updateUnreadMessages(chatId)
        }
    }

    fun observeUserConnectivity() {
        viewModelScope.launch {
            userRepository.observeUserConnectivity()
        }
    }

    fun observeChatRoomStatus(chatId: String, isGroup: Boolean = false) {
        userRepository.getChatRoomStatus(chatId, isGroup).observeForever { status ->
            _chatRoomStatus.value = status ?: Status() // Ensure non-null status
        }
    }

    fun getChatRoomDetail(chatId: String) {
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


    fun getUserProfile(userId: String?) {
        viewModelScope.launch {
            userRepository.getUserProfile(userId)
                .collect { response ->
                    _userProfile.value = response
                }
        }
    }

    /**
     * Fetch user by ID synchronously
     * Used by CallViewModel to get caller info when receiving calls
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            val response = userRepository.getUserProfile(userId).first()
            when (response) {
                is Response.Success -> response.data
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

}
