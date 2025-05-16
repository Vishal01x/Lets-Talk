package com.exa.android.letstalk.data.domain.main.repository

import android.util.Log
import com.exa.android.letstalk.utils.CurChatManager.activeChatId
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.PriorityMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PriorityRepository @Inject constructor(
    private val db : FirebaseFirestore
){

    val userCollection = db.collection("users")

    suspend fun sendPriorityMessage(userId : String, priorityMessage : PriorityMessage){
        try {
            val userDoc = userCollection.document(userId).collection("priority")
            userDoc.document(priorityMessage.message.messageId).set(priorityMessage).await()

        }catch (e : Exception){
            Log.d("FireStore Service", "priorityMessage sent failed - ${e.message}")
        }
    }


    fun getPriorityMessages(userId: String): Flow<Response<List<PriorityMessage>>> =
        flow {
            emit(Response.Loading)

            try {
                val snapshotFlow = callbackFlow {
                    val listenerRegistration = userCollection.document(userId)
                        .collection("messages")
                        .orderBy("timestamp")
                        .addSnapshotListener { snapshot, exception ->
                            if (exception != null) {
                                trySend(
                                    Response.Error(
                                        exception.message ?: "Unknown error"
                                    )
                                ).isFailure
                            } else {
                                val messages =
                                    snapshot?.toObjects(PriorityMessage::class.java) ?: emptyList()

                                val result = trySend(Response.Success(messages))
                                if (result.isFailure) {
                                    // Log or handle the failure (optional)
                                    Log.e(
                                        "FireStore Operation",
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

}
