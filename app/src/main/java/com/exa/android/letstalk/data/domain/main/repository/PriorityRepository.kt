package com.exa.android.letstalk.data.domain.main.repository

import android.util.Log
import com.exa.android.letstalk.utils.CurChatManager.activeChatId
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.helperFun.getOtherProfilePic
import com.exa.android.letstalk.utils.helperFun.getOtherUserName
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.PriorityMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PriorityRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db : FirebaseFirestore
){

    val userCollection = db.collection("users")
    val chatCollection = db.collection("chats")

    val currentUserId = auth.currentUser?.uid

    suspend fun sendPriorityMessage(userId : String, priorityMessage : PriorityMessage){
        try {
            val userDoc = userCollection.document(userId).collection("priority")
            userDoc.document(priorityMessage.message.messageId).set(priorityMessage).await()
            Log.d("Vishal", "priorityMessage sent success")
        }catch (e : Exception){
            Log.d("Vishal", "priorityMessage sent failed - ${e.message}")
        }
    }


    fun getPriorityMessages(userId: String = currentUserId!!): Flow<Response<List<PriorityMessage>>> =
        flow {
            emit(Response.Loading)

            try {
                val snapshotFlow = callbackFlow {
                    val listenerRegistration = userCollection.document(userId)
                        .collection("priority")
                        .addSnapshotListener { snapshot, exception ->
                            if (exception != null) {
                                trySend(
                                    Response.Error(
                                        exception.message ?: "Unknown error"
                                    )
                                ).isFailure
                            } else {
                                val messages =
                                    snapshot?.toObjects(PriorityMessage::class.java) ?.sortedByDescending { it.message.timestamp.seconds } // Sort here
                                        ?: emptyList()

                                Log.d("Vishal", messages.toString())

                                val result = trySend(Response.Success(messages))
                                if (result.isFailure) {
                                    // Log or handle the failure (optional)
                                    Log.e(
                                        "Vishal",
                                        "Failed to send messages to the flow."
                                    )
                                }

                            }
                        }
                    awaitClose { listenerRegistration.remove() }
                }
                emitAll(snapshotFlow)
            } catch (e: Exception) {
                emit(Response.Error(e.message ?: "Failed to load messages"))
            }
        }



    suspend fun getChatDetails(chatId: String): Chat? {
        val snapshot = chatCollection
            .document(chatId)
            .get()
            .await()

        val chat =  snapshot.toObject(Chat::class.java)
        val updateChat = chat?.let { correctChatNameAndImage(it,chat.lastMessageCnt) }
        return updateChat
    }

    private fun correctChatNameAndImage(chat: Chat, lastMessageCnt: Long = 0): Chat {
        chat.unreadMessages = chat.lastMessageCnt - lastMessageCnt
        chat.name = if (!chat.group) {
            getOtherUserName(chat.name, chat.id, currentUserId!!)
        } else {
            chat.name
        }

        chat.profilePicture = if (!chat.group) {
            getOtherProfilePic(
                chat.profilePicture ?: "",
                chat.id,
                currentUserId!!
            )
        } else {
            chat.profilePicture
        }
        return chat
    }

}
