package com.exa.android.khacheri.mvvm.main.repository

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.exa.android.khacheri.utils.helperFun.getUserIdFromChatId
import com.exa.android.khacheri.utils.models.Chat
import com.exa.android.khacheri.utils.models.Status
import com.exa.android.khacheri.utils.models.User
import com.exa.android.letstalk.utils.Response
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val rdb: FirebaseDatabase // Realtime Database
) {

    val currentUser = auth.currentUser?.uid

    private val userCollection = db.collection("users")
    private val chatCollection = db.collection("chats")
    private val userStatusRef = rdb.getReference("/status/$currentUser")

    suspend fun updateUserStatus(curUser: String, isOnline: Boolean) {
        val data = mapOf(
            "isOnline" to if (isOnline) true else false,
            "lastSeen" to if (isOnline) null else Timestamp.now().seconds
        )
        try {
            userStatusRef.updateChildren(data).await()
            Log.d("Firebase Operation", "User status updated for $curUser.")
        } catch (e: Exception) {
            Log.e("Firebase Operation", "Failed to update user status", e)
        }
    }

    fun observeUserConnectivity() {
        val onlineStatus = mapOf(
            "isOnline" to true,
            "lastSeen" to null,
            "typingTo" to ""
        )
        val offlineStatus = mapOf(
            "isOnline" to false,
            "lastSeen" to Timestamp.now().seconds,
            "typingTo" to ""
        )

        val connectRef = rdb.getReference(".info/connected")
        connectRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userStatusRef.setValue(onlineStatus)
                    userStatusRef.onDisconnect().setValue(offlineStatus)
                    Log.d("Firebase Operation", "User is online, status updated.")
                } else {
                    userStatusRef.setValue(offlineStatus)
                    Log.d("Firebase Operation", "User is offline, status updated.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "Firebase Operation",
                    "Error observing connection status",
                    error.toException()
                )
            }
        })
    }

    suspend fun setTypingStatus(id: String, typingTo: String?) {
        val statusRef = rdb.getReference("/status/$id/typingTo")
        try {
            statusRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val curStatus = currentData.getValue(String::class.java) ?: ""
                    // Only update if the conditions are met
                    if (curStatus == "" || typingTo == "") {
                        currentData.value = typingTo
                    }
                    return Transaction.success(currentData)  // Indicate the transaction was successful
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null) {
                        Log.e("Firebase Operation", "Transaction failed: $error")
                    } else {
                        Log.d("Firebase Operation", "Transaction completed with success: $committed")
                    }
                }
            })

            // Set up onDisconnect to clear typing status
            statusRef.onDisconnect().setValue("").await()

            Log.d("Firebase Operation", "Typing status updated for $id.")
        } catch (e: Exception) {
            Log.e("Firebase Operation", "Failed to update typing status", e)
        }
    }




    suspend fun updateUnreadMessages(chatId : String) {
        val chatDocRef = chatCollection.document(chatId)
        try {
            chatDocRef.update("unreadMessages.$currentUser", 0).await()
            Log.d("Firebase Operation", "Unread messages for ${currentUser} successfully reset to 0.")
        } catch (e: Exception) {
            Log.e("Firebase Operation", "Failed to reset unread messages", e)
        }
    }

     fun getChatRoomDetail(chatId: String): Flow<Response<Chat?>> = callbackFlow {
        try {
            trySend(Response.Loading) // Emit loading state

            val listenerRegistration = chatCollection.document(chatId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(Response.Error(exception.localizedMessage ?: "Error fetching user"))
                    } else if (snapshot != null && snapshot.exists()) {
                        val chat = snapshot.toObject(Chat::class.java)
                        if (chat != null) {
                            trySend(Response.Success(chat))
                        } else {
                            trySend(Response.Error("Failed to parse user data"))
                            Log.e("Firebae Operation", "Failed to parse user data")
                        }
                    } else {
                        trySend(Response.Error("User not found"))
                        Log.e("Firebae Operation", "User not found")
                    }
                }

            awaitClose {
                listenerRegistration.remove() // Ensure listener is removed on cancellation
            }
        } catch (e: Exception) {
            trySend(Response.Error(e.localizedMessage ?: "Unknown error"))
            Log.e("Firebae Operation", e.localizedMessage ?: "Unknown error")
        }
    }

    fun getChatRoomStatus(id: String, isGroup : Boolean): LiveData<Status?> {
        val chatId = if(isGroup)id else getUserIdFromChatId(id, currentUser!!)
        val path = rdb.getReference("/status/$chatId")
        val liveData = MutableLiveData<Status?>()
        path.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status =
                    snapshot.getValue(Status::class.java) ?: Status() // Default to Status()
                liveData.postValue(status)
                Log.d("Firebase Operation", status.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(null) // Handle the error by posting null
            }
        })
        return liveData
    }

    suspend fun getAllUser(): Flow<Response<List<User?>>> = flow {
        Log.d("FireStore Operation", " users - ")
        emit(Response.Loading)
        try {
            val snapshotFlow = callbackFlow {

                val usersPath = userCollection.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.d("FireStore Operation", " users - ${exception.message}")
                        exception.message?.let { Response.Error(it) }?.let { trySend(it) }?.isFailure
                    } else {
                        val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                        Log.d("FireStore Operation", " users - $users")
                        val updatedUsers = users.filter { it.userId != currentUser }
                        val result = trySend(Response.Success(updatedUsers))
                        if (result.isFailure) {
                            // Log or handle the failure (optional)
                            Log.e("FireStore Operation", "Failed to send messages to the flow.")
                        }
                    }
                }
                awaitClose {
                    usersPath.remove()
                }
            }
            emitAll(snapshotFlow)
        }catch (e : Exception){
            e.message?.let { Response.Error(it) }?.let { emit(it) }
        }

    }
}
