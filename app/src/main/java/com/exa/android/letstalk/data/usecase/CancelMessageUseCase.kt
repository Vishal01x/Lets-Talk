package com.exa.android.letstalk.data.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.exa.android.letstalk.data.broadcast.MessageReceiver
import com.exa.android.letstalk.data.local.room.ScheduledMessageRepository
import javax.inject.Inject

class CancelMessageUseCase @Inject constructor(
    private val context: Context,
    private val repository: ScheduledMessageRepository
) {

    suspend fun cancelMessage(messageId: String, time: Long) {
        repository.removeScheduledMessage(messageId)

        // If no other messages exist at this time, cancel alarm
        val remainingMessages = repository.getMessageCountAtTime(time)
        if (remainingMessages == 0) {
            cancelAlarm(time)
        }
    }

    private fun cancelAlarm(time: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MessageReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context, time.toInt(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}
