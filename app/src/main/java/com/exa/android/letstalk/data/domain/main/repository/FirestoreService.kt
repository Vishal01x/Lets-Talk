package com.exa.android.letstalk.data.domain.main.repository

import android.content.Context
import android.util.Log
import com.exa.android.letstalk.data.fm.postNotificationToUsers
import com.exa.android.letstalk.data.fm.subscribeForNotifications
import com.exa.android.letstalk.utils.CurChatManager.activeChatId
import com.exa.android.letstalk.utils.helperFun.generateChatId
import com.exa.android.letstalk.utils.helperFun.getUserIdFromChatId
import com.exa.android.letstalk.utils.models.Chat
import com.exa.android.letstalk.utils.models.Message
import com.exa.android.letstalk.utils.models.User
import com.exa.android.letstalk.utils.Response
import com.exa.android.letstalk.utils.helperFun.generateMessage
import com.exa.android.letstalk.utils.helperFun.getOtherUserName
import com.exa.android.letstalk.utils.models.Call
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreService @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationContext val context: Context
) {
    private val userCollection = db.collection("users")
    private val chatCollection = db.collection("chats")
    val currentUserId = auth.currentUser?.uid

    suspend fun getCurUser(): User? {
        val user = userCollection.document(currentUserId!!).get().await()
        return user.toObject(User::class.java)
    }

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


    suspend fun createChat(chat: Chat, onComplete: () -> Unit) = coroutineScope {
        val chatId = chat.id
        val currentUserId = currentUserId ?: return@coroutineScope  // Prevent null crash
        val otherUserId = getUserIdFromChatId(chatId, currentUserId)

        try {
            // Launch parallel tasks
             val updateCurrentUserChat = async { updateUserChatList(currentUserId, chatId) }
             val updateOtherUserChat = async { updateUserChatList(otherUserId, chatId) }
             val updateParticipants =
                 async { updateChatParticipants(chatId, listOf(currentUserId, otherUserId)) }
             val updateChatDetails = async { updateChatDetail(chat) }

             // Wait for all tasks to complete
             updateCurrentUserChat.await()
             updateOtherUserChat.await()
             updateParticipants.await()
             updateChatDetails.await()

//            updateUserChatList(currentUserId, chatId)
//            updateUserChatList(otherUserId, chatId)
//            updateChatParticipants(chatId, listOf(currentUserId, otherUserId))
//            updateChatDetail(chat)

            // Subscribe to push notifications
            subscribeForNotifications(chatId) { token ->
                Log.d("FireStore Operation", "Subscribed to topic: $token")
            }

            // Call completion handler after all operations are successful
            onComplete()
        } catch (e: Exception) {
            Log.e("FireStore Operation", "Error creating chat: ${e.message}")
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

        coroutineScope {
            // Create chat detail
            val updateChatDetailTask = async {
                updateChatDetail(
                    Chat(
                        id = chatId,
                        name = groupName,
                        profilePicture = groupProfile,
                        group = true
                    )
                )
            }

            // Prepare all group members
            val allGroupMembers = groupMembers + currentUserId
            val aboutData = mapOf(
                "admin" to listOf(currentUserId),
                "groupMembers" to allGroupMembers
            )

            // Update each user's chat list in parallel
            val updateUserChatTasks = allGroupMembers.map { member ->
                async {
                    if (member != null) {
                        updateUserChatList(member, chatId)
                    }
                }
            }

            // Save the 'about' section
            val updateAboutTask = async {
                chatRef.collection("about").document("info").set(aboutData).await()
            }

            // Wait for all Firestore operations to complete
//            updateChatDetailTask.await()
//            updateUserChatTasks.awaitAll()
//            updateAboutTask.await()

            // Subscribe to notifications after successful creation
            subscribeForNotifications(chatId) {
                Log.d("FireStore Operation", "Subscribed to topic : $it")
            }

            Log.d("FireStore Operation", "Successfully Group is Created")
            onComplete(chatId) // Call onComplete when everything is done
        }
    }


    private suspend fun checkAndCreateChat(receiver: User) {
        val receiverId = receiver.userId
        val chatId = generateChatId(currentUserId!!, receiverId)
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

    private suspend fun doesChatIdExist(
        chatCollection: CollectionReference,
        chatId: String
    ): Boolean {
//        val querySnapshot = chatCollection
//            .whereEqualTo("id", chatId)
//            .limit(1) // Use limit to minimize query time
//            .get()
//            .await()

//        return !querySnapshot.isEmpty

        val document = chatCollection.document(chatId).get().await()
        return document.exists()
    }


    suspend fun createChatAndSendMessage(
        message: Message, user: User? = null, imageUrl: String? = null // for post notification
    ) {
        try {
            val messageRef = chatCollection.document(message.chatId).collection("messages")

            messageRef.document(message.messageId).set(message)
                .addOnSuccessListener {
                    postNotificationToUsers(
                        channelID = message.chatId,
                        senderName = user?.name.toString() ?: message.senderId,
                        senderId = message.senderId,
                        messageContent = message.message,
                        imageUrl = imageUrl,
                        appContext = context
                    )
                    Log.d(
                        "FireStore Operation",
                        "New message added Successfully"
                    )
                }
                .addOnFailureListener { Log.d("FireStore Operation", "New message added Failed") }

            // Update last message and timestamp in the chat document
            updateLastMessage(message.chatId, message.message)

            //Update last message cnt for each member
            updateUserLastMessageCnt(message.chatId, message.senderId)


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
                            generateMessage(
                                currentUserId!!,
                                generateChatId(currentUserId, receiver.userId),
                                message,
                                null,
                                null,
                                listOf(currentUserId, receiver.userId)
                            ),
                            receiver
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

        try {
            if (!snapshot.exists()) {
                // If the document doesn't exist, create it
                chatDoc.set(
                    mapOf(
                        "lastMessage" to message,
                        "lastMessageTimestamp" to Timestamp.now(),
                        "lastMessageCnt" to 1
                    )
                ).await()
            } else {
                // If the document exists, update it
                chatDoc.update(
                    mapOf(
                        "lastMessage" to message,
                        "lastMessageTimestamp" to Timestamp.now(),
                        "lastMessageCnt" to FieldValue.increment(1)
                    )
                ).await()
            }
            Log.d("0", "Chat document updated successfully!")
        } catch (e: Exception) {
            Log.e("Firestore Update", "Error updating chat document: ${e.message}")
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
            if (message.senderId == currentUserId || message.status == "seen") break
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
        messages: List<String>,
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
                    batch.update(
                        messageRef,
                        mapOf("members" to FieldValue.arrayRemove(currentUserId))
                    )
                    val members = messageRef.get().await().get("members") as List<String>
                    if (members.isEmpty()) batch.delete(messageRef)
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

                                if (activeChatId == chatId) {
                                    updateMessageStatusToSeen(chatId, messages)
                                    // try to optimise it
                                    chatCollection.document(chatId)
                                        .addSnapshotListener { chatSnapshot, chatException ->
                                            val lastMessageCnt =
                                                chatSnapshot?.getLong("lastMessageCnt") ?: 0

                                            if(activeChatId == chatId) {
                                                updateUserLastMessageCnt(
                                                    chatId,
                                                    currentUserId!!,
                                                    lastMessageCnt
                                                )
                                            }
                                        }
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

                    //if (user != null && user.userId != currentUser) {
                    if (user != null) {
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
                    Log.d("FireStore Operation", "user - ${userException.message.toString()}")
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


                    CoroutineScope(Dispatchers.IO).launch {
                        chatIdCnt.forEach { (chatId, lastMessageCnt) -> // each device is needed to subscribe for push notification
                            subscribeForNotifications(
                                chatId,
                                onComplete = { token ->
                                    Log.d(
                                        "FireStore Operation",
                                        "Subscribed to topic: $token for $chatId"
                                    )
                                }
                            )
                        }
                    }


                    // Step 2: Add listeners for each user's chat
                    chatIdCnt.forEach { (chatId, lastMessageCnt) ->

                        val chatDocument = chatCollection.document(chatId)

                        val chatListener =
                            chatDocument.addSnapshotListener { chatSnapshot, chatException ->
                                if (chatException != null) {
                                    Log.d("FireStore Operation", chatException.message.toString())
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
                                        chat.name = if (!chat.group) {
                                            getOtherUserName(chat.name, chat.id, currentUserId!!)
                                        } else {
                                            chat.name
                                        }
                                        chatList.add(chat) // Ensures it's inside the if block
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


    fun makeCall(call: Call, onSuccess: () -> Unit, onFailure: () -> Unit) {
        userCollection.document(call.receiverId).update("call", call).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure()
        }
    }

    fun trackCall(): Flow<Response<Call?>> = callbackFlow {
        trySend(Response.Loading)
        var lastKnownCall: Call? = null
        try {
            val listenerRegistration =
                userCollection.document(currentUserId!!)
                    .addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            trySend(Response.Error(exception.message ?: "Error tracking call"))
                            return@addSnapshotListener
                        }

                        snapshot?.let {
                            val newCall = it.get("call") as? Call

                            // Only emit if the call field has changed
                            if (newCall != lastKnownCall) {
                                lastKnownCall = newCall
                                trySend(Response.Success(newCall))
                            }
                        }
                    }

            // Close callbackFlow when cancelled
            awaitClose {
                listenerRegistration.remove()
            }
        } catch (e: Exception) {
            trySend(Response.Error(e.message ?: "Error tracking call"))
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






