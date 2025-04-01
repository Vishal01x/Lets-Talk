package com.exa.android.letstalk.data.fm

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.Person
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.exa.android.letstalk.MainActivity
import com.exa.android.letstalk.R
import com.exa.android.letstalk.utils.CurChatManager.activeChatId
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FireStore Operation", "From: ${message.from} Data: ${message.notification}")
//        message.notification?.let {
        val senderId = message.data["senderId"] ?: ""
        if (!senderId.isNullOrEmpty() && senderId == Firebase.auth.currentUser?.uid) return
        val chatId = message.data["chatId"] ?: ""
        if (!chatId.isNullOrEmpty() && chatId == activeChatId) return
       // val imageUrl = message.data["imageUrl"] ?: "https://fakestoreapi.com/img/71-3HjGNDUL._AC_SY879._SX._UX._SY._UY_.jpg"
        val imageUrl = "https://www.w3schools.com/w3images/avatar2.png"
        //change it to above when using images
        val title = message.data["title"]
        val body = message.data["body"]

        showNotification(title, body, imageUrl)
        // showNotification(it.title, it.body, imageUrl)
        //}
    }

    private fun showNotification(title: String?, message: String?, imageUrl: String?) {
        val channelId = "messages"
        val notificationId = Random().nextInt(1000)

        // ✅ Check and Request Notification Permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Post Notification", "Notification permission not granted!")
                return
            }
        }

        // ✅ Create Notification Channel (For Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat messages"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // ✅ Intent to open MainActivity and navigate to ChatScreen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("senderId", title)  // Pass senderId for navigation
        }

        // ✅ Create PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Define "Person" for the sender
        val personBuild = Person.Builder()
            .setName(title ?: "User")


        // Optional: Use a default user icon
        if (imageUrl.isNullOrEmpty()) personBuild.setIcon(
            IconCompat.createWithResource(
                this,
                R.drawable.chat_img3
            )
        )

        val person = personBuild.build()

        // ✅ Use MessagingStyle for better message categorization
        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle(title)
            .addMessage(message ?: "New Message", System.currentTimeMillis(), person)

        // ✅ Build Notification with the "Conversation" style
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_chat)
            .setStyle(messagingStyle)  // 🔥 Makes it a conversation notification
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShortcutId(title ?: "chat") // 🔥 Groups messages from the same sender
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // ✅ Load Profile Image (Optional)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .transform(CircleCrop())
                .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        builder.setLargeIcon(resource) // Set circular image as large icon
                        NotificationManagerCompat.from(this@FirebaseMessageService)
                            .notify(notificationId, builder.build())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        } else {
            NotificationManagerCompat.from(this)
                .notify(notificationId, builder.build())
        }
    }
}