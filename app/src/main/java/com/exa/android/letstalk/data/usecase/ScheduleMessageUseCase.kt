package com.exa.android.letstalk.data.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.exa.android.letstalk.core.broadcast.MessageReceiver
import com.exa.android.letstalk.core.worker.SendMessagesWorker
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

        Log.d("ScheduleMessage", "Scheduling alarm for: $time")

        val pendingIntent = PendingIntent.getBroadcast(
            context, time.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                } else {
                     Log.e("ScheduleMessage", "Cannot schedule exact alarm: Permission denied")
                     // Fallback to non-exact? Or prompt user? For now just log.
                     // A real fix implies navigating user to settings.
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("ScheduleMessage", "SecurityException scheduling alarm", e)
        }
    }

    suspend fun restoreScheduledAlarms() {
        val currentTime = System.currentTimeMillis()
        Log.d("ScheduleMessage", "Restoring alarms for messages after: $currentTime")
        
        // 1. Re-schedule alarms for future messages
        val futureMessages = repository.getFutureScheduledMessages(currentTime)
        val distinctTimes = futureMessages.map { it.scheduledTime }.distinct()
        distinctTimes.forEach { time ->
             scheduleAlarm(time)
        }
        Log.d("ScheduleMessage", "Restored ${distinctTimes.size} alarms")
        
        // 2. Trigger worker immediately to handle any past due messages (missed while phone was off)
        // The worker logic fetches all messages <= Now, so this will catch missed ones.
        Log.d("ScheduleMessage", "Triggering worker for missed messages")
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<SendMessagesWorker>()
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .build()
        androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
    }
}
