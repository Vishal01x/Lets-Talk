package com.exa.android.letstalk.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.exa.android.letstalk.data.domain.main.repository.FirestoreService
import com.exa.android.letstalk.data.local.room.ScheduledMessageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyWorkerFactory @Inject constructor(
    private val scheduledMessageRepository: ScheduledMessageRepository,
    private val firestoreService: FirestoreService
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            SendMessagesWorker::class.java.name -> SendMessagesWorker(
                appContext,
                workerParameters,
                scheduledMessageRepository,
                firestoreService
            )
            else -> null
        }
    }
}
