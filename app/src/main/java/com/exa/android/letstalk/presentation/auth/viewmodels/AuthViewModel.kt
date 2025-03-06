package com.exa.android.letstalk.presentation.auth.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.domain.auth.AuthRepository
import com.exa.android.letstalk.data.local.pref.UserPreferences
import com.exa.android.letstalk.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
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

    // ✅ Logout (Clear UID from local storage)
    fun logoutUser() {
        viewModelScope.launch {
            userPreferences.clearUser()
            //authRepository.logoutUser()
            _authStatus.value = Response.Error("")
        }
    }
}