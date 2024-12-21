package com.exa.android.letstalk.presentation.auth.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.repositories.auth.AuthRepository
import com.exa.android.letstalk.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authStatus = MutableStateFlow<Response<Boolean>>(Response.Loading)
    val authStatus: StateFlow<Response<Boolean>>
        get() = _authStatus

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        _authStatus.value = if (authRepository.isUserLoggedIn()) {
            Response.Success(true)
        } else {
            Response.Error("")
        }
    }

    fun registerUser(email: String, password : String) {
        viewModelScope.launch {
            authRepository.registerUser(email, password).collect { response ->
                _authStatus.value = response
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            authRepository.loginUser(email, password).collect { response ->
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
}