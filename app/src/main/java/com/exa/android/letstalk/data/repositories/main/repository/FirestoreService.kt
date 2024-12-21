package com.exa.android.khacheri.mvvm.main.repository

import android.util.Log
import com.exa.android.khacheri.utils.helperFun.generateChatId
import com.exa.android.khacheri.utils.helperFun.getUserIdFromChatId
import com.exa.android.khacheri.utils.models.Chat
import com.exa.android.khacheri.utils.models.Message
import com.exa.android.khacheri.utils.models.User
import com.exa.android.letstalk.utils.Response
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreService @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val userCollection = db.collection("users")
    private val chatCollection = db.collection("chats")
    val currentUser = auth.currentUser?.uid

    //search user based on the phone number
    fun searchUser(phone: String): Flow<Response<User?>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = userCollection.whereEqualTo("phone", phone).get().await()
            if (!snapshot.isEmpty) {
                val user = snapshot.documents[0].toObject(User::class.java)
                emit(Response.Success(user))
            } else {
                emit(Response.Success(null)) // No user found
            }
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "An unexpected error occurred"))
        }
    }


    fun insertUser(userName: String, phone: String) {
        val userId = auth.currentUser?.uid
        val user = User(
            name = userName,
            phone = phone,
            profilePicture = "https://example.picture",
            userId = userId!!
        )
        try {
            userCollection.document(user.userId).set(user)
                .addOnSuccessListener { Log.d("FireStoreService", "New user added Successfully") }
                .addOnFailureListener { Log.d("FireStoreService", "New user added Failed") }
        } catch (e: Exception) {
            // handel Exception
        }
    }


    suspend fun createChat(
        chat: Chat,
        onComplete: () -> Unit
    ) {
        val chatId = chat.id
        val otherUser = getUserIdFromChatId(chatId, currentUser!!)

        updateUserChatList(currentUser, chatId)
        updateUserChatList(otherUser, chatId)
        updateChatParticipants(chatId, listOf(currentUser, otherUser))
        updateChatDetail(chat) {
            onComplete()
        }
    }

    suspend fun createGroup(
        groupName: String,
        groupMembers: List<String>,
        groupProfile: String = "example.com",
        onComplete: (String) -> Unit
    ) {
        val chatId = generateChatId()
        val chatRef = chatCollection.document(chatId)
        updateChatDetail(
            Chat(
                id = chatId,
                name = groupName,
                profilePicture = groupProfile,
                isGroup = true
            )
        )

        val allGroupMembers = groupMembers + currentUser

        val aboutData = mapOf(
            "admin" to listOf(currentUser),
            "groupMembers" to allGroupMembers
        )

        allGroupMembers.forEach { member ->
            if (member != null) {
                updateUserChatList(member, chatId)
            }
        }

        // Save the 'about' section
        chatRef.collection("about").document("info").set(aboutData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FireStore Operation", "Successfully Group is Created")
                onComplete(chatId) // Call onComplete when successful
            } else {
                // Handle errors if necessary
                task.exception?.printStackTrace()
            }
        }
    }

    private suspend fun checkAndCreateChat(receiver: User) {
        val receiverId = receiver.userId
        val chatId = generateChatId(currentUser!!, receiverId)
        val chatExists = doesChatIdExist(chatCollection, chatId)
        if (!chatExists) {
            createChat(
                Chat(
                    id = chatId,
                    name = receiver.name,
                    profilePicture = receiver.profilePicture
                ), {})
        }
    }

    suspend fun doesChatIdExist(chatCollection: CollectionReference, chatId: String): Boolean {
        val querySnapshot = chatCollection
            .whereEqualTo("id", chatId)
            .limit(1) // Use limit to minimize query time
            .get()
            .await()

        return !querySnapshot.isEmpty
    }


    suspend fun createChatAndSendMessage(
        chatId: String,
        text: String,
        replyTo: Message?,
        members: List<String?>
    ) {
        val message = Message(
            senderId = currentUser!!,
            message = text,
            replyTo = replyTo,
            members = members.ifEmpty { listOf(currentUser, getUserIdFromChatId(chatId,currentUser)) }
        )
        try {
            val messageRef = chatCollection.document(chatId).collection("messages")

            messageRef.document(message.messageId).set(message)
                .addOnSuccessListener {
                    Log.d(
                        "FireStore Operation",
                        "New message added Successfully"
                    )
                }
                .addOnFailureListener { Log.d("FireStoreService", "New message added Failed") }

            // Update last message and timestamp in the chat document
            updateLastMessage(chatId, message.message)

            //Update last message cnt for each member
            updateUserLastMessageCnt(chatId, message.senderId)


            // Insert or update the chat document for both users
            /* val chat1 = mapOf("users" to listOf(userId1, userId2))
             val chat2 = mapOf("users" to listOf(userId2, userId1))

             val chat1Doc = chatCollection.document(chatId).get().await()
             if (!chat1Doc.exists()) {
                 chatCollection.document(chatId).set(chat1).await()
             }

             val chat2Doc = chatCollection.document(chatId).get().await()
             if (!chat2Doc.exists()) {
                 chatCollection.document(chatId).set(chat2).await()
             }*/
        } catch (e: Exception) {
            Log.d("FireStore Operation", "error in sending message - ${e.message}")
        }
    }


    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun forwardMessages(messages: List<String>, receivers: List<User>) {
        scope.launch {
            // Perform the message forwarding logic here
            try {
                receivers.forEach { receiver ->
                    messages.forEach { message ->
                        checkAndCreateChat(receiver)
                        createChatAndSendMessage(
                            generateChatId(currentUser, receiver.userId),
                            message,
                            null,
                            listOf(currentUser, receiver.userId)
                        ) // Call your existing logic
                    }
                }
                //cancelAllOperations()
            } catch (e: Exception) {
                Log.e("UserRepository", "Error forwarding messages: ${e.message}")
            }
            //cancelAllOperations()
        }
    }

    private fun cancelAllOperations() {
        job.cancel()
    }


    private suspend fun updateLastMessage(
        chatId: String,
        message: String
    ) {
        val chatDoc = chatCollection.document(chatId)
        val snapshot = chatDoc.get().await()

        if (snapshot.exists()) {
            // Document exists, perform update
            chatDoc.update(
                "lastMessage", message,
                "lastMessageTimestamp", Timestamp.now(),
                // "lastMessageCnt" to FieldValue.increment(1)
//                "unreadMessages.$userId1", 0
            ).await()
        }
    }

    private fun updateUserLastMessageCnt(
        chatId: String,
        userId: String,
        lastMessageCnt: Long? = null
    ) {
        val chatListRef = userCollection.document(userId).collection("chat_list").document(chatId)
        chatListRef.update("lastMessageCnt", lastMessageCnt ?: FieldValue.increment(1))
            .addOnSuccessListener { println("User chats updated successfully") }
            .addOnFailureListener { e -> println("Error updating user chats: ${e.message}") }
    }

    private fun updateMessageStatusToSeen(chatId: String, messages: List<Message>) {
        val batch = db.batch()
        for (message in messages.reversed()) {
            if (message.senderId == currentUser || message.status == "seen") break
            val messageRef = chatCollection
                .document(chatId)
                .collection("messages")
                .document(message.messageId)

            //chatCollection.document(chatId).update("unreadMessages.$currentUser", 0)
            batch.update(messageRef, "status", "seen")
        }

        batch.commit().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("FireStore Operation", "All messages status updated to 'seen'")
            } else {
                Log.e("FireStore Operation", "Error updating messages status", it.exception)
            }
        }
    }

    fun updateUserChatList(currentUser: String, newChat: String) {
        val chatListRef =
            userCollection.document(currentUser).collection("chat_list").document(newChat)
        chatListRef.set(mapOf("chatId" to newChat, "lastMessageCnt" to 0))
            .addOnSuccessListener { println("User chats updated successfully") }
            .addOnFailureListener { e -> println("Error updating user chats: ${e.message}") }
    }

    private suspend fun updateChatParticipants(chatId: String, userIds: List<String>) {
        val participantRef = chatCollection.document(chatId).collection("about").document("info")
        participantRef.set(mapOf("groupMembers" to userIds)).await()
    }

    private suspend fun updateChatDetail(
        chat: Chat,
        onComplete: (() -> Unit)? = null
    ) {
        val chatRef = chatCollection.document(chat.id)
        chatRef.set(chat).await()
        if (onComplete != null) {
            onComplete()
        }
    }

    suspend fun deleteMessages(
        messages: Set<String>,
        chatId: String,
        deleteFor: Int = 1,
        onCleared: () -> Unit
    ) {

        val chatRef = chatCollection.document(chatId).collection("messages")

        val batch = db.batch()
        try {
            for (messageId in messages) {
                val messageRef = chatRef.document(messageId)
                if (deleteFor == 2) {
                    batch.update(messageRef, mapOf("message" to "deleted"))
                } else {
                    batch.update(messageRef, mapOf("members" to FieldValue.arrayRemove(currentUser)))
                    val members = messageRef.get().await().get("members") as List<String>
                    if(members.isEmpty())batch.delete(messageRef)
                }
            }
            batch.commit().await()

            Log.d("FireStore Operation", "Messages Deleted Successfully")
            onCleared()
        } catch (e: Exception) {
            Log.d("FireStore Operation", "Error in Message Deletion - ${e.message}")
        }
    }

    fun getMessages(chatId: String): Flow<Response<List<Message>>> =
        flow {
            emit(Response.Loading)

            try {
                val snapshotFlow = callbackFlow {
                    val listenerRegistration = chatCollection.document(chatId)
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
                                    snapshot?.toObjects(Message::class.java) ?: emptyList()

                                updateMessageStatusToSeen(chatId, messages)
                                // try to optimise it
                                chatCollection.document(chatId)
                                    .addSnapshotListener { chatSnapshot, chatException ->
                                        val lastMessageCnt =
                                            chatSnapshot?.getLong("lastMessageCnt") ?: 0
                                        updateUserLastMessageCnt(
                                            chatId,
                                            currentUser!!,
                                            lastMessageCnt
                                        )
                                    }

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


//    suspend fun getChatMembers(chatId : String) : Flow<Response<List<User?>>> = callbackFlow{
//        trySend(Response.Loading)
//
//        val members =
//
//        val memberRef = chatCollection.document(chatId).collection("about").document("info")
//        val members = mutableListOf<User>()
//
//        val memberDocumentListener = memberRef.addSnapshotListener{membersSnapshot, exception->
//            if(exception != null){
//                trySend(
//                    Response.Error(
//                        exception.message ?: "Error fetching user details"
//                    )
//                ).isFailure
//                return@addSnapshotListener
//            }
//
//            val membersIds = membersSnapshot?.get("members") as List<String>
//
//            membersIds.forEach{ userId->
//                val user = userCollection.document(userId).get()
//                members.add(user)
//            }
//        }
//    }


    fun getChatMembers(chatId: String): Flow<List<String>> = callbackFlow {
        val memberRef = chatCollection.document(chatId)
            .collection("about")
            .document("info")

        val listener = memberRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val members = snapshot?.get("groupMembers") as? List<String> ?: emptyList()
            trySend(members)
        }

        awaitClose { listener.remove() }
    }

    fun getUsersDetails(userIds: List<String>): Flow<Response<List<User>>> = callbackFlow {
        trySend(Response.Loading)

        val userDetails = mutableListOf<User>()
        var remainingUsers = userIds.size

        for (userId in userIds) {
            userCollection.document(userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject<User>()?.copy(userId = userId)

                    if (user != null && user.userId != currentUser) {
                        userDetails.add(user)
                    }
                    remainingUsers--
                    if (remainingUsers == 0) {
                        trySend(Response.Success(userDetails))
                        close()
                    }
                }
                .addOnFailureListener { error ->
                    trySend(Response.Error("Failed to fetch user details: ${error.message}"))
                    close(error)
                }
        }

        awaitClose {}
    }


    fun getChatList(userId: String): Flow<Response<List<Chat>>> = callbackFlow {
        trySend(Response.Loading)

        val userDocument = userCollection.document(userId)
        val chatListeners = mutableListOf<ListenerRegistration>()
        val chatList = mutableListOf<Chat>()

        // Step 1: Listen for updates on the user's document to fetch user list
        val userDocumentListener = userDocument.collection("chat_list")
            .addSnapshotListener { userSnapshot, userException ->
                if (userException != null) {
                    trySend(
                        Response.Error(
                            userException.message ?: "Error fetching chat list"
                        )
                    ).isFailure
                    return@addSnapshotListener
                }

                val chatIdCnt = mutableMapOf<String, Long>()

                userSnapshot?.documents?.mapNotNull { document ->
                    val chatId = document.getString("chatId")
                    val lastMessageCnt = document.getLong("lastMessageCnt") ?: 0

                    if (chatId != null && lastMessageCnt != null) {
                        chatIdCnt[chatId] = lastMessageCnt
                    }
                }

                if (chatIdCnt.isNullOrEmpty()) {
                    trySend(Response.Success(emptyList())).isFailure
                } else {
                    // Clear previous listeners to avoid duplicate data
//                    chatListeners.forEach { it.remove() }
//                    chatListeners.clear()
//                    chatList.clear()

                    // Step 2: Add listeners for each user's chat
                    chatIdCnt.forEach { (chatId, lastMessageCnt) ->
                        val chatDocument = chatCollection.document(chatId)

                        val chatListener =
                            chatDocument.addSnapshotListener { chatSnapshot, chatException ->
                                if (chatException != null) {
                                    trySend(
                                        Response.Error(
                                            chatException.message ?: "Error fetching chat data"
                                        )
                                    ).isFailure
                                    return@addSnapshotListener
                                }

                                if (chatSnapshot != null) {
                                    val chat = chatSnapshot.toObject(Chat::class.java)
                                    // Replace or update the chat entry for this user
                                    chatList.removeIf { it.id == chatId }
                                    if (chat != null) {
                                        chat.unreadMessages = chat.lastMessageCnt - lastMessageCnt
                                        chatList.add(chat)
                                    }

                                    val sortedChatList =
                                        chatList.sortedByDescending { it.lastMessageTimestamp.toDate() }
                                    // Send the updated chat list to the flow
                                    trySend(Response.Success(sortedChatList)).isFailure
                                }
                            }
                        chatListeners.add(chatListener)
                    }
                }
            }

        // Cleanup listeners when the flow is canceled
        awaitClose {
            userDocumentListener.remove()
            chatListeners.forEach { it.remove() }
        }
    }
}


/*suspend fun forwardMessages(
        messages: List<String>,
        receiverIds: List<String>
    ): Flow<Response<Unit>> = callbackFlow {
        scope.launch {
            try {
                trySend(Response.Loading)

                receiverIds.forEach { receiverId ->
                    messages.forEach { message ->
                        async(Dispatchers.IO) {
                            val chatId = generateChatId(currentUser!!, receiverId)

                            val forwardMessage = Message(
                                senderId = currentUser,
                                message = message,
                                receiverId = receiverId,
                            )

                            chatCollection.document(chatId)
                                .collection("messages")
                                .document(forwardMessage.messageId)
                                .set(forwardMessage)
                                .await()
                        }
                        Log.d("AllUsersScreen", "receiverId - $receiverId, message - ${message}")
                    }

                    updateLastMessage(
                        currentUser!!,
                        receiverId,
                        messages.last(),
                        messages.size.toLong()
                    )
                }
                trySend(Response.Success(Unit))
            } catch (e: Exception) {
                trySend(Response.Error(e.localizedMessage ?: "Error forwarding messages"))
            }
        }
        awaitClose {
            cancelAllOperations()
        }
    }.flowOn(Dispatchers.IO)
*/
