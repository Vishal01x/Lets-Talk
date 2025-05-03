package com.exa.android.letstalk.data.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.exa.android.letstalk.data.broadcast.MessageReceiver
import com.exa.android.letstalk.data.local.room.ScheduledMessageEntity
import javax.inject.Inject

class ScheduleMessageUseCase @Inject constructor(
    private val context: Context,
    private val repository: ScheduledMessageRepository
) {

    suspend fun scheduleMessage(message: ScheduledMessageEntity) {
        repository.saveScheduledMessage(message)

        // If no alarm exists for this time, register one
        val messageCount = repository.getMessageCountAtTime(message.scheduledTime)
        Log.d("ScheduleMessage", "Sending message: $messageCount")
        if (messageCount == 1) { // Means it's the first message for this time
            scheduleAlarm(message.scheduledTime)
        }
    }

    private fun scheduleAlarm(time: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MessageReceiver::class.java).apply {
            putExtra("SCHEDULED_TIME", time)
        }

        Log.d("ScheduleMessage", "Sending message: $time")

        val pendingIntent = PendingIntent.getBroadcast(
            context, time.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }
}
