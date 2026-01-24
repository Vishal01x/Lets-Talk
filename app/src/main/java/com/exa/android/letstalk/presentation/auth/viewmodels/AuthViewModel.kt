package com.exa.android.letstalk.presentation.auth.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.core.utils.Response
import com.exa.android.letstalk.core.utils.isNetworkAvailable
import com.exa.android.letstalk.data.repository.AuthRepository
import com.exa.android.letstalk.presentation.Main.Home.ViewModel.MediaSharingViewModel
import com.exa.android.letstalk.data.local.pref.UserPreferences
import com.exa.android.letstalk.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context : Context
) : ViewModel() {

    private val _authStatus = MutableStateFlow<Response<Boolean>>(Response.Loading)
    val authStatus: StateFlow<Response<Boolean>>
        get() = _authStatus

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        viewModelScope.launch {
            val uid = userPreferences.getUserUid()  // Waits for UID retrieval
            _authStatus.value = if (uid != null) {
                Response.Success(true)  // User is logged in
            } else {
                Response.Error("User not logged in")  // Show login screen
            }
        }
    }


    fun registerUser(email: String, password : String) {
        viewModelScope.launch {
            authRepository.registerUser(email, password).collect { response ->
                if(response is Response.Success){
                    val uid = authRepository.getUid()
                    uid.let {
                        userPreferences.saveUser(it) // Save locally
                    }
                }
                _authStatus.value = response
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            authRepository.loginUser(email, password).collect { response ->
                if (response is Response.Success) {
                    val uid = authRepository.getUid()
                    uid.let {
                        userPreferences.saveUser(it) // Save locally
                    }
                }
                _authStatus.value = response
            }
        }
    }

    fun resetPassword(email : String){
        viewModelScope.launch {
            authRepository.resetPassword(email).collect{ response ->
                _authStatus.value = response
            }
        }
    }

    private val _userUpdateState = mutableStateOf<Response<Unit>?>(null)
    val userUpdateState: State<Response<Unit>?> = _userUpdateState

    fun updateOrCreateUser(
        userId: String?,
        user: User,
        imageUri: Uri?,
        mediaSharingViewModel: MediaSharingViewModel
    ) {
        viewModelScope.launch {
            _userUpdateState.value = Response.Loading

            try {
                var updatedUser = user

                if (imageUri != null) {
                    if (!isNetworkAvailable(context)) {
                        throw Exception("No Internet Connection")
                    }

                    val uploadResult = mediaSharingViewModel.uploadFileToCloudinary(context, imageUri)
                    val imageUrl = uploadResult?.mediaUrl
                        ?: throw Exception("Image upload failed")

                    updatedUser = user.copy(profilePicture = imageUrl)
                }

                authRepository.updateOrCreateUser(userId, updatedUser)
                _userUpdateState.value = Response.Success(Unit)

            } catch (e: Exception) {
                _userUpdateState.value = Response.Error(e.message ?: "Unknown error")
            }
        }
    }


    // ✅ Logout (Clear UID from local storage)
    fun logoutUser() {
        viewModelScope.launch {
            userPreferences.clearUser()
            authRepository.logOutUser()
            _authStatus.value = Response.Error("")
        }
    }
}