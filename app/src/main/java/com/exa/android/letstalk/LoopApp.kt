package com.exa.android.letstalk

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.exa.android.letstalk.data.worker.MyWorkerFactory
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LoopApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
//WorkManager.initialize(this, Configuration.Builder().build()

    }
}