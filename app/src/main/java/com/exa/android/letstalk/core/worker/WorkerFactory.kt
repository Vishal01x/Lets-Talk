package com.exa.android.letstalk.core.worker

/*
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
*/