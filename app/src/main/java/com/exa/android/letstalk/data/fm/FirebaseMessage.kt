package com.exa.android.letstalk.data.fm

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.exa.android.letstalk.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock

private val tokenLock = ReentrantLock()
private var cachedToken: String? = null

/**
 * Subscribes the current user to a chat topic for notifications.
 */
suspend fun subscribeForNotifications(chatId: String, onComplete: (String) -> Unit) {
    try {
        FirebaseMessaging.getInstance().subscribeToTopic(chatId).await()
        Log.d("FireStore Operation", "Subscribed to topic: $chatId")

        val token = FirebaseMessaging.getInstance().token.await()
        onComplete(token)
    } catch (e: Exception) {
        Log.e("FireStore Operation", "Failed to subscribe to topic: $chatId, Error: ${e.message}")
    }
}

/**
 * Sends a push notification to all users subscribed to a given topic.
 */
fun postNotificationToUsers(
    channelID: String,
    senderName: String,
    senderId : String,
    messageContent: String,
    imageUrl: String?,
    appContext: Context
) {
    val fcmUrl = "https://fcm.googleapis.com/v1/projects/loop-it-d337e/messages:send"

    val jsonBody = JSONObject().apply {
        put("message", JSONObject().apply {
            put("topic", channelID)
//            put("notification", JSONObject().apply {
//                put("title", "$senderName")
//                put("body", "$messageContent")
//            })
            put("data", JSONObject().apply {
                put("title", "$senderName")
                put("chatId", "$channelID")
                put("body", "$messageContent")
                put("imageUrl", "$imageUrl")
                put("senderId", "$senderId")
            })
        })
    }

    val requestBody = jsonBody.toString()

    val request = object : StringRequest(Method.POST, fcmUrl, Response.Listener {
        Log.d("FireStore Operation", "Notification sent successfully")
    }, Response.ErrorListener { error ->
        Log.e("FireStore Operation", "Failed to send notification: ${error.message}")
    }) {
        override fun getBody(): ByteArray = requestBody.toByteArray(Charsets.UTF_8)

        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "Bearer ${getAccessToken(appContext)}"
            headers["Content-Type"] = "application/json"
            return headers
        }
    }

    Volley.newRequestQueue(appContext).add(request)
}

/**
 * Retrieves the Firebase Cloud Messaging (FCM) access token using Google credentials.
 */
private fun getAccessToken(context: Context): String {
    tokenLock.lock()
    return try {
        cachedToken?.let { return it }  // Use cached token if available

        val inputStream = context.resources.openRawResource(R.raw.loopit_key)
        val googleCreds = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        val token = googleCreds.refreshAccessToken().tokenValue

        cachedToken = token  // Cache the token
        token
    } catch (e: IOException) {
        Log.e("FireStore Operation", "Error fetching access token: ${e.message}")
        throw RuntimeException("Failed to get access token", e)
    } finally {
        tokenLock.unlock()
    }
}
