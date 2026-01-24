package com.exa.android.letstalk.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.exa.android.letstalk.data.repository.FirestoreService
import com.exa.android.letstalk.data.local.room.ScheduledMessageEntity
import com.exa.android.letstalk.data.local.room.toMessage
import com.exa.android.letstalk.data.usecase.ScheduledMessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

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
        Log.d("ScheduleMessage", "Sending message: start")
        
        // Use current time plus a small buffer (e.g., 5 seconds) to catch any slightly future messages involved in this wake-up
        // or any past messages that were missed.
        val currentTime = System.currentTimeMillis() + 5000 
        
        val messages = scheduledMessageRepository.getMessagesScheduledOnOrBefore(currentTime)
        Log.d("ScheduleMessage", "Sending message: found ${messages.size} messages")
        
        if (messages.isNotEmpty()) {
            messages.forEach { message ->
                sendMessageToFirebase(message)
            }

            // Mark messages as sent instead of deleting
            scheduledMessageRepository.markMessagesAsSent(currentTime)
        }
        Log.d("ScheduleMessage", "Sending message: completed")
        return Result.success()
    }

    private suspend fun sendMessageToFirebase(scheduleMessage: ScheduledMessageEntity) {
        val message = scheduleMessage.toMessage()
        Log.d("ScheduleMessage", "Sending message: $message")
        firestoreService.sendMessage(message)
    }
}
