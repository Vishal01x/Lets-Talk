package com.exa.android.letstalk.presentation.Main.Home.newChat

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.domain.main.ViewModel.ChatViewModel
import com.exa.android.letstalk.data.domain.main.ViewModel.MediaSharingViewModel
import com.exa.android.letstalk.presentation.navigation.component.HomeRoute
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.isNetworkAvailable
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.User
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.URLEncoder
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@HiltViewModel
class NewGroupViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedUsers = mutableStateOf<Set<User>>(emptySet())
    val selectedUsers: State<Set<User>> = _selectedUsers

    val response = mutableStateOf<Response<Unit>?>(null)

    fun updateSelectedUsers(users: Set<User>) {
        _selectedUsers.value = users.toSet() // must create new set to trigger recomposition
    }
    fun createGroup(
        groupName: String,
        imageUri: Uri?,
        selectedUsers: List<User>,
        chatViewModel: ChatViewModel,
        mediaSharingViewModel: MediaSharingViewModel,
        onSuccess: (String, String?) -> Unit
    ) {
        viewModelScope.launch {
            response.value = Response.Loading

            try {
                // Run image upload in parallel if imageUri is available
                val imageUploadDeferred = async {
                    if (imageUri != null) {
                        if (!isNetworkAvailable(context)) throw Exception("No Internet Connection")
                        mediaSharingViewModel.uploadFileToCloudinary(context, imageUri)?.mediaUrl
                            ?: throw Exception("Image upload failed")
                    } else null
                }

                val selectedUserIds = selectedUsers.map { it.userId }

                // Start group creation concurrently
                val groupCreationDeferred = async {
                    val imageUrl = imageUploadDeferred.await()
                    suspendCancellableCoroutine<Unit> { continuation ->
                        chatViewModel.createGroup(groupName, imageUrl, selectedUserIds) { chatId, error ->
                            if (error == null) {
                                response.value = Response.Success(Unit)
                                onSuccess(chatId, imageUrl)
                                continuation.resume(Unit) {}
                            } else {
                                continuation.resumeWithException(error)
                            }
                        }
                    }
                }

                groupCreationDeferred.await()

            } catch (e: Exception) {
                response.value = Response.Error(e.localizedMessage)
            }
        }
    }

}