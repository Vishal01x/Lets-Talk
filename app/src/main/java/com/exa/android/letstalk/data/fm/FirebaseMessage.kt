package com.exa.android.letstalk.data.fm

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.exa.android.letstalk.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

fun subscribeForNotifications(chatId: String, onComplete: (String) -> Unit) {
    FirebaseMessaging.getInstance().subscribeToTopic(chatId).addOnSuccessListener {
        Log.d("FireStore Operation", "Subscribed to topic : $chatId")
        val token =  FirebaseMessaging.getInstance().token.result
        onComplete(token)
    }.addOnFailureListener {e->
        Log.e("FireStore Operation", "Failed Subscribed to topic : $chatId, - ${e.message}")
    }
}


fun postNotificationToUsers(
    channelID: String,
    senderName: String,
    messageContent: String,
    appContext: Context
) {
    val fcmUrl = "https://fcm.googleapis.com/v1/projects/loop-it-d337e/messages:send"
    val jsonBody = JSONObject().apply {
        put("message", JSONObject().apply {
            put("topic", "$channelID")
            put("notification", JSONObject().apply {
                put("title", "New message in $channelID")
                put("body", "$senderName: $messageContent")
            })
        })
    }

    val requestBody = jsonBody.toString()

    val request = object : StringRequest(Method.POST, fcmUrl, Response.Listener {
        Log.d("ChatViewModel", "Notification sent successfully")
    }, Response.ErrorListener {
        Log.e("ChatViewModel", "Failed to send notification")
    }) {
        override fun getBody(): ByteArray {
            return requestBody.toByteArray()
        }

        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "Bearer ${getAccessToken(appContext)}"
            headers["Content-Type"] = "application/json"
            return headers
        }
    }
    val queue = Volley.newRequestQueue(appContext)
    queue.add(request)
}

private fun getAccessToken(context: Context): String {
    val inputStream = context.resources.openRawResource(R.raw.loopit_key)
    val googleCreds = GoogleCredentials.fromStream(inputStream)
        .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
    return googleCreds.refreshAccessToken().tokenValue
}