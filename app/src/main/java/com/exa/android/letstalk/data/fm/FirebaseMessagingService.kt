package com.exa.android.letstalk.data.fm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.exa.android.letstalk.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FireStore Operation", "From: ${message.from} Data: ${message.notification}")
        message.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "messages"
        val notificationId = Random().nextInt(1000)

        Firebase.auth.currentUser?.let {
            if(title == it.uid || message?.contains(it.displayName.toString()) == true) return
        }

        // ✅ Check and Request Notification Permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e("Notification", "Notification permission not granted!")
                return //  Exit without showing notification
            }
        }

        // ✅ Create Notification Channel (For Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for chat messages"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // ✅ Build and Show Notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title ?: "New Message")
            .setContentText(message ?: "You have a new message")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

}
