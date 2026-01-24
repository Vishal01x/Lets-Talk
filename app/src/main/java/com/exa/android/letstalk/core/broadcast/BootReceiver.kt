package com.exa.android.letstalk.core.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.exa.android.letstalk.data.usecase.ScheduleMessageUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleMessageUseCase: ScheduleMessageUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed received. Restoring alarms...")
            
            // GoAsync is needed because we are launching a coroutine
            val pendingResult = goAsync()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    scheduleMessageUseCase.restoreScheduledAlarms()
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error restoring alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
