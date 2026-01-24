package com.exa.android.letstalk.data.repository

import com.exa.android.letstalk.core.utils.Response
import com.exa.android.letstalk.domain.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db : FirebaseFirestore
) {
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun getUid(): String = firebaseAuth.currentUser?.uid ?: ""

    fun registerUser(email: String, password: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val result = suspendCancellableCoroutine { continuation ->
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Resume the coroutine with a successful result
                            continuation.resume(true)
                        } else {
                            // Resume the coroutine with an exception for error handling, from here it will direct to catch block
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown Error")
                            )
                        }
                    }
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            // Emit an error response when an exception is caught
            emit(Response.Error(e.localizedMessage ?: "Error in registration of User"))
        }
    }

    fun loginUser(email : String, password : String) : Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val result = suspendCancellableCoroutine { continuation ->
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Resume the coroutine with a successful result
                            continuation.resume(true)
                        } else {
                            // Resume the coroutine with an exception for error handling, from here it will direct to catch block
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown Error")
                            )
                        }
                    }
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            // Emit an error response when an exception is caught
            emit(Response.Error(e.localizedMessage ?: "User Login Failed"))
        }
    }

    fun logOutUser() = firebaseAuth.signOut()


    fun resetPassword(email : String) : Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            val result = suspendCancellableCoroutine { continuation ->
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Resume the coroutine with a successful result
                            continuation.resume(true)
                        } else {
                            // Resume the coroutine with an exception for error handling, from here it will direct to catch block
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown Error")
                            )
                        }
                    }
            }
            emit(Response.Success(result))
        } catch (e: Exception) {
            // Emit an error response when an exception is caught
            emit(Response.Error(e.localizedMessage ?: "Reset Password Failed"))
        }
    }

    suspend fun updateOrCreateUser(userId : String?, user : User) {
        val curUser = userId ?: FirebaseAuth.getInstance().currentUser?.uid
        if(curUser == null)throw Exception("User not logged in")
        val userDocRef = db.collection("users").document(curUser)
        user.userId = curUser
        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)

            if (!snapshot.exists()) {
                // Store the User directly at the root of the doc
                transaction.set(userDocRef, user, SetOptions.merge())
            } else {
                transaction.set(userDocRef, user, SetOptions.merge())
            }
        }.await()
    }

}