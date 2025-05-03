package com.exa.android.letstalk.data.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.exa.android.letstalk.data.worker.SendMessagesWorker

class MessageReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {
        val time = intent.getLongExtra("SCHEDULED_TIME", 0L)
        Log.d("ScheduleMessage", "Sending Time: $time")
        if (time > 0) {
            val workRequest = OneTimeWorkRequestBuilder<SendMessagesWorker>()
                .setInputData(workDataOf("SCHEDULED_TIME" to time))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when online
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
