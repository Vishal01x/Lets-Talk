//package com.exa.android.letstalk.presentation.Main.status
//import androidx.compose.runtime.State
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.exa.android.letstalk.utils.models.UserStatus
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.collect
//import kotlinx.coroutines.launch
//import java.util.Calendar
//import java.util.Date
//import javax.inject.Inject
//
//
//@HiltViewModel
//class StatusViewModel @Inject constructor(
//    private val repository: StatusRepository
//) : ViewModel() {
//    private val _statuses = mutableStateOf<List<UserStatus>>(emptyList())
//    val statuses: State<List<UserStatus>> = _statuses
//
//    private val _selectedStatus = mutableStateOf<UserStatus?>(null)
//    val selectedStatus: State<UserStatus?> = _selectedStatus
//
//    init {
//        getStatuses()
//    }
//
//    fun getStatuses() {
//        viewModelScope.launch {
//            repository.getUserStatuses().collect { result ->
//                when(result) {
//                    is Result.Success -> _statuses.value = result.data
//                    is Result.Error -> {
//                    // Handle error}
//                    }
//                }
//            }
//        }
//    }
//
//    fun createUpdateStatus(
//        content: String,
//        mediaUrl: String,
//        mediaType: String,
//        days: Int
//    ) {
//        val status = _selectedStatus.value?.copy(
//            content = content,
//            mediaUrl = mediaUrl,
//            mediaType = mediaType,
//            endTime = calculateEndDate(days)
//        ) ?: UserStatus(
//            userId = repository.currentUserId,
//            content = content,
//            mediaUrl = mediaUrl,
//            mediaType = mediaType,
//            endTime = calculateEndDate(days)
//        )
//
//        viewModelScope.launch {
//            if (_selectedStatus.value == null) {
//                repository.addStatus(status).collect()
//            } else {
//                repository.updateStatus(status).collect()
//            }
//        }
//    }
//
//    fun deleteStatus(statusId: String) {
//        viewModelScope.launch {
//            repository.deleteStatus(statusId).collect()
//        }
//    }
//
//    private fun calculateEndDate(days: Int): Date {
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DAY_OF_MONTH, days)
//        return calendar.time
//    }
//}