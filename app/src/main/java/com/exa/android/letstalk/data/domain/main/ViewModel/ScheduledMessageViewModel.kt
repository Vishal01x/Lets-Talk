package com.exa.android.letstalk.data.domain.main.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.letstalk.data.local.room.toEntity
import com.exa.android.letstalk.data.usecase.CancelMessageUseCase
import com.exa.android.letstalk.data.usecase.ScheduleMessageUseCase
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.ScheduleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduledMessageViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleMessageUseCase,
    private val cancelUseCase: CancelMessageUseCase
) : ViewModel() {

    private val _scheduleTime = MutableStateFlow(0L)
    val scheduleTime : StateFlow<Long> = _scheduleTime

    private val _scheduleMessageType = MutableStateFlow(ScheduleType.NONE)
    val scheduleMessageType: StateFlow<ScheduleType> = _scheduleMessageType


    fun setTime(time : Long) = _scheduleTime.tryEmit(time)

    fun updateScheduleMessageType(type : ScheduleType) = _scheduleMessageType.tryEmit(type)


    fun scheduleMessage(message : Message, time: Long) {
        val scheduleMessage = message.toEntity()
        scheduleMessage.scheduledTime = time
        viewModelScope.launch {
            scheduleUseCase.scheduleMessage(scheduleMessage)
        }
    }

    fun cancelMessage(messageId: String, time: Long) {
        viewModelScope.launch {
            cancelUseCase.cancelMessage(messageId, time)
        }
    }
}
