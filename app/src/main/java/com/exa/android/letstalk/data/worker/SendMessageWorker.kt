package com.exa.android.letstalk.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.exa.android.letstalk.data.domain.main.repository.FirestoreService
import com.exa.android.letstalk.data.local.room.ScheduledMessageEntity
import com.exa.android.letstalk.data.local.room.toMessage
import com.exa.android.letstalk.data.usecase.ScheduledMessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltWorker
class SendMessagesWorker@AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduledMessageRepository: ScheduledMessageRepository,
    private val firestoreService: FirestoreService
) : CoroutineWorker(context, workerParams) {

//

    init{
        Log.d("ScheduleMessage", "Sending message: init")
    }

//    @Inject
//    lateinit var scheduledMessageRepository: ScheduledMessageRepository
//    @Inject
//    lateinit var firestoreService: FirestoreService

    override suspend fun doWork(): Result {
        val time = inputData.getLong("SCHEDULED_TIME", 0L)
        Log.d("ScheduleMessage", "Sending message: start")
        if (time > 0) {
            val messages = scheduledMessageRepository.getMessagesAtTime(time)
            Log.d("ScheduleMessage", "Sending message: $messages")
            messages.forEach { message ->
                sendMessageToFirebase(message)
            }

            // Remove messages from Room database after sending
            scheduledMessageRepository.removeAllMessagesAtTime(time)
        }
        Log.d("ScheduleMessage", "Sending message: completed")
        return Result.success()
    }

    private suspend fun sendMessageToFirebase(scheduleMessage: ScheduledMessageEntity) {
        val message = scheduleMessage.toMessage()
        Log.d("ScheduleMessage", "Sending message: $message")
        firestoreService.createChatAndSendMessage(message)
    }
}
