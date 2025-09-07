//package com.exa.android.letstalk.presentation.Main.status
//
//import com.exa.android.letstalk.utils.models.UserStatus
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ListenerRegistration
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.tasks.await
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class StatusRepository @Inject constructor(
//    private val db: FirebaseFirestore,
//    private val auth: FirebaseAuth
//) {
//    val currentUserId = auth.currentUser?.uid ?: ""
//
//    fun addStatus(status: UserStatus) = flow {
//        try {
//            db.collection("statuses").document(status.statusId).set(status).await()
//            emit(Result.Success(true))
//        } catch (e: Exception) {
//            emit(Result.Error(e))
//        }
//    }
//
//    fun updateStatus(status: UserStatus) = flow {
//        try {
//            db.collection("statuses").document(status.statusId)
//                .update(
//                    mapOf(
//                        "content" to status.content,
//                        "mediaUrl" to status.mediaUrl,
//                        "mediaType" to status.mediaType,
//                        "endTime" to status.endTime
//                    )
//                ).await()
//            emit(Result.Success(true))
//        } catch (e: Exception) {
//            emit(Result.Error(e))
//        }
//    }
//
//    fun deleteStatus(statusId: String) = flow {
//        try {
//            db.collection("statuses").document(statusId).delete().await()
//            emit(Result.Success(true))
//        } catch (e: Exception) {
//            emit(Result.Error(e))
//        }
//    }
//
//    fun getUserStatuses() = callbackFlow<Result<List<UserStatus>>> {
//        val snapshotListener = db.collection("statuses")
//            .whereEqualTo("userId", currentUserId)
//            .addSnapshotListener { snapshot, error ->
//                val result = if (snapshot != null) {
//                    val statuses = snapshot.toObjects(UserStatus::class.java)
//                    Result.Success(statuses)
//                } else {
//                    Result.Error(error ?: Exception("Unknown Firestore error"))
//                }
//                trySend(result).isSuccess
//            }
//        awaitClose { snapshotListener.remove() }
//    }
//}
//
//
//sealed class Result<out T> {
//    data class Success<out T>(val data: T) : Result<T>()
//    data class Error(val exception: Throwable) : Result<Nothing>()
//}
