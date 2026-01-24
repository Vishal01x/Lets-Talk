package com.exa.android.letstalk.core.fm

import android.Manifest
import android.app.Notification
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
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.exa.android.letstalk.MainActivity
import com.exa.android.letstalk.R
import com.exa.android.letstalk.core.utils.CurChatManager.activeChatId
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FireStore Operation", "From: ${message.from} Data: ${message.notification}")
        val senderId = message.data["senderId"] ?: ""
        if (!senderId.isNullOrEmpty() && senderId == Firebase.auth.currentUser?.uid) return
        val chatId = message.data["chatId"] ?: ""
        if (!chatId.isNullOrEmpty() && chatId == activeChatId) return
        // val imageUrl = message.data["imageUrl"] ?: "https://fakestoreapi.com/img/71-3HjGNDUL._AC_SY879._SX._UX._SY._UY_.jpg"
        val imageUrl = "https://www.w3schools.com/w3images/avatar2.png"
        //change it to above when using images
        val title = message.data["title"]
        val body = message.data["body"]

        showNotification(chatId, title, body, imageUrl)
        // showNotification(it.title, it.body, imageUrl)
        //}
    }

    private fun showNotification(
        chatId: String?,
        senderName: String?,
        message: String?,
        imageUrl: String?
    ) {
        if (chatId == null || message == null) return

        val channelId = "messages"
        val notificationId = chatId.hashCode()

        // 🚫 Android 13+ requires POST_NOTIFICATIONS permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("FirebaseMsgService", "Notification permission not granted!")
            return
        }

        // ✅ Notification channel for Android O+
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

        // ✅ Intent to open MainActivity and go to the chat
        val intent = Intent(this, MainActivity::class.java).apply {
            Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("senderId", senderName)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Build Person object for MessagingStyle
        val personBuilder = Person.Builder().setName(senderName ?: "User")
        if (imageUrl.isNullOrEmpty()) {
            personBuilder.setIcon(IconCompat.createWithResource(this, R.drawable.chat_img3))
        }
        val person = personBuilder.build()

        // ✅ Load previous messages from SharedPreferences (per chat)
        val sharedPreferences = getSharedPreferences("chat_notifications", MODE_PRIVATE)
        val messagesSet = sharedPreferences.getStringSet(chatId, mutableSetOf())!!.toMutableSet()
        messagesSet.add("$senderName: $message") // 👈 Append latest message
        sharedPreferences.edit().putStringSet(chatId, messagesSet).apply()

        // ✅ Use MessagingStyle to group messages
        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle(senderName)
        messagesSet.forEach {
            messagingStyle.addMessage(it, System.currentTimeMillis(), person)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_chat)
            .setStyle(messagingStyle)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShortcutId(senderName ?: "chat")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        // ✅ Load image as large icon using Glide (optional)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .transform(CircleCrop())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        builder.setLargeIcon(resource)
                        NotificationManagerCompat.from(this@FirebaseMessageService)
                            .notify(notificationId, builder.build())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        } else {
            NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        }
    }
}