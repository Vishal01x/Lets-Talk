package com.exa.android.letstalk.data.local.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.usecase.CancelMessageUseCase
import com.exa.android.letstalk.data.usecase.ScheduleMessageUseCase
import com.exa.android.letstalk.data.usecase.ScheduledMessageRepository
import com.exa.android.letstalk.domain.Message
import com.exa.android.letstalk.domain.ScheduleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScheduledMessageViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleMessageUseCase,
    private val cancelUseCase: CancelMessageUseCase,
    private val repository: ScheduledMessageRepository
) : ViewModel() {

    private val _scheduleTime = MutableStateFlow(0L)
    val scheduleTime : StateFlow<Long> = _scheduleTime

    private val _scheduleMessageType = MutableStateFlow(ScheduleType.NONE)
    val scheduleMessageType: StateFlow<ScheduleType> = _scheduleMessageType

    // Expose Scheduled Messages (Pending)
    val scheduledMessages: StateFlow<List<MessageListItem>> = repository.getScheduledMessages()
        .map { messages ->
            groupMessagesByDate(messages)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expose Sent Messages (History)
    val sentMessages: StateFlow<List<MessageListItem>> = repository.getSentMessages()
        .map { messages ->
            groupMessagesByDate(messages)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun groupMessagesByDate(messages: List<ScheduledMessageEntity>): List<MessageListItem> {
        val grouped = messages.groupBy { message ->
            when {
                isToday(message.scheduledTime) -> "Today"
                isYesterday(message.scheduledTime) -> "Yesterday"
                else -> getFormattedDate(message.scheduledTime)
            }
        }

        return grouped.entries.flatMap { (date, messages) ->
            listOf(MessageListItem.DateHeader(date)) + messages.map { MessageListItem.MessageItem(it) }
        }
    }


    fun deleteMessage(message: ScheduledMessageEntity) {
        viewModelScope.launch {
            repository.deleteMessage(message)
            // No need to call loadScheduledMessages() again - Flow will update automatically
        }
    }

    // Keep existing schedule/cancel methods
    fun scheduleMessage(message: Message, time: Long, recipientName: String, profileImageUri: String) {
        val scheduleMessage = message.toEntity(recipientName, profileImageUri)
        scheduleMessage.scheduledTime = time
        viewModelScope.launch {
            scheduleUseCase.scheduleMessage(scheduleMessage)
        }
    }

    fun setTime(time : Long) = _scheduleTime.tryEmit(time)

    fun updateScheduledMessage(newMessage : ScheduledMessageEntity, oldMessage : ScheduledMessageEntity){
        viewModelScope.launch {
            cancelUseCase.cancelMessage(oldMessage.messageId, oldMessage.scheduledTime)
            scheduleUseCase.scheduleMessage(newMessage)
        }
    }


    fun updateScheduleMessageType(type : ScheduleType) = _scheduleMessageType.tryEmit(type)


//    fun scheduleMessage(message : Message, time: Long) {
//        val scheduleMessage = message.toEntity("", "")
//        scheduleMessage.scheduledTime = time
//        viewModelScope.launch {
//            scheduleUseCase.scheduleMessage(scheduleMessage)
//        }
//    }

    fun cancelMessage(messageId: String, time: Long) {
        viewModelScope.launch {
            cancelUseCase.cancelMessage(messageId, time)
        }
    }



    // Date helper functions
    private fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }
        return today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                today.get(Calendar.YEAR) == date.get(Calendar.YEAR)
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }
        return yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR)
    }

    private fun getFormattedDate(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}



// MessageModels.kt
sealed class MessageListItem {
    data class DateHeader(val date: String) : MessageListItem()
    data class MessageItem(val message: ScheduledMessageEntity) : MessageListItem()
}