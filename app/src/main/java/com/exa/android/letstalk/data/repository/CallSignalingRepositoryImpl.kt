package com.exa.android.letstalk.data.repository

import android.util.Log
import com.exa.android.letstalk.domain.CallAnswer
import com.exa.android.letstalk.domain.CallOffer
import com.exa.android.letstalk.domain.CallStatus
import com.exa.android.letstalk.domain.IceCandidate
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.get

/**
 * Repository for WebRTC signaling using Firestore
 * Manages call offers, answers, and ICE candidates through Firestore
 */
@Singleton
class CallSignalingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CallSignalingRepository {
    private val TAG = "WEBRTC_CALL"

    private val callsCollection = firestore.collection("calls")

    /**
     * Create a new call offer in Firestore
     */
    override suspend fun createCallOffer(callOffer: CallOffer): Result<String> {
        return try {
            val callDoc = callsCollection.document(callOffer.callId)
            callDoc.set(callOffer).await()
            Log.d(TAG, "Call offer created: ${callOffer.callId}")
            Result.success(callOffer.callId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create call offer", e)
            Result.failure(e)
        }
    }

    /**
     * Send call answer to Firestore
     */
    override suspend fun sendCallAnswer(callId: String, answer: CallAnswer): Result<Unit> {
        return try {
            callsCollection.document(callId)
                .set(mapOf("answer" to answer), SetOptions.merge())
                .await()
            Log.d(TAG, "Call answer sent for: $callId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send call answer", e)
            Result.failure(e)
        }
    }

    /**
     * Add ICE candidate to Firestore
     */
    override suspend fun addIceCandidate(
        callId: String,
        userId: String,
        candidate: IceCandidate
    ): Result<Unit> {
        return try {
            val candidatesCollection = callsCollection.document(callId)
                .collection("candidates")

            candidatesCollection.add(
                mapOf(
                    "userId" to userId,
                    "sdpMid" to candidate.sdpMid,
                    "sdpMLineIndex" to candidate.sdpMLineIndex,
                    "sdp" to candidate.sdp
                )
            ).await()

            Log.d(TAG, "ICE candidate added for call: $callId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add ICE candidate", e)
            Result.failure(e)
        }
    }

    /**
     * Clean up old RINGING calls that are no longer valid
     */
    private suspend fun cleanupOldCalls(userId: String, beforeTime: Timestamp) {
        try {
            val oldCalls = callsCollection
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", CallStatus.RINGING.name)
                .whereLessThan("timestamp", beforeTime)
                .get()
                .await()

            Log.d(TAG, "🧹 [CLEANUP] Found ${oldCalls.documents.size} old RINGING calls")

            oldCalls.documents.forEach { doc ->
                doc.reference.update(mapOf("status" to CallStatus.MISSED.name)).await()
                Log.d(TAG, "✅ [CLEANUP] Marked ${doc.id} as MISSED")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ [CLEANUP] Failed", e)
        }
    }

    /**
     * Listen for incoming calls - ONLY detects calls created AFTER listener starts
     */
    override fun observeIncomingCalls(userId: String): Flow<CallOffer> = callbackFlow {
        Log.d(TAG, "🔔 [RECEIVER] Setting up listener for: $userId")

        val listenerStartTime = Timestamp.now()
        Log.d(TAG, "⏰ [RECEIVER] Start time: $listenerStartTime")

        // Clean up old calls in background
        CoroutineScope(Dispatchers.IO).launch {
            cleanupOldCalls(userId, listenerStartTime)
        }

        val listener = callsCollection
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("status", CallStatus.RINGING.name)
            .whereGreaterThan("timestamp", listenerStartTime) // ONLY NEW calls
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "❌ [RECEIVER] Error", error)
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val callOffer = change.document.toObject(CallOffer::class.java)
                        // Double-check timestamp
                        if (callOffer.timestamp.seconds > listenerStartTime.seconds) {
                            Log.d(TAG, "✅ [RECEIVER] NEW call: ${callOffer.callId}")
                            trySend(callOffer)
                        } else {
                            Log.d(TAG, "🚫 [RECEIVER] OLD call ignored: ${callOffer.callId}")
                        }
                    }
                }
            }

        awaitClose {
            Log.d(TAG, "🛑 [RECEIVER] Removing listener")
            listener.remove()
        }
    }

    /**
     * Listen for call answer
     */
    override fun observeCallAnswer(callId: String): Flow<CallAnswer?> = callbackFlow {
        val listener = callsCollection.document(callId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing call answer", error)
                    return@addSnapshotListener
                }

                snapshot?.let { doc ->
                    val answerMap = doc.get("answer") as? Map<*, *>
                    if (answerMap != null) {
                        val answer = CallAnswer(
                            callId = answerMap["callId"] as? String ?: "",
                            sdpAnswer = answerMap["sdpAnswer"] as? String ?: "",
                            accepted = answerMap["accepted"] as? Boolean ?: false
                        )
                        trySend(answer)
                        Log.d(TAG, "Call answer received for: $callId")
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Listen for ICE candidates from remote peer
     */
    override fun observeIceCandidates(callId: String, remotePeerId: String): Flow<IceCandidate> =
        callbackFlow {
            val listener = callsCollection.document(callId)
                .collection("candidates")
                .whereEqualTo("userId", remotePeerId)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing ICE candidates", error)
                        return@addSnapshotListener
                    }

                    snapshots?.documentChanges?.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED) {
                            val data = change.document.data
                            val candidate = IceCandidate(
                                sdpMid = data["sdpMid"] as? String ?: "",
                                sdpMLineIndex = (data["sdpMLineIndex"] as? Long)?.toInt() ?: 0,
                                sdp = data["sdp"] as? String ?: ""
                            )
                            trySend(candidate)
                            Log.d(TAG, "ICE candidate received for: $callId")
                        }
                    }
                }

            awaitClose { listener.remove() }
        }

    /**
     * Update call status
     */
    override suspend fun updateCallStatus(callId: String, status: CallStatus): Result<Unit> {
        return try {
            callsCollection.document(callId)
                .update("status", status.name)
                .await()
            Log.d(TAG, "Call status updated: $callId -> $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update call status", e)
            Result.failure(e)
        }
    }

    /**
     * End call - update status and cleanup
     */
    override suspend fun endCall(callId: String, endedBy: String): Result<Unit> {
        return try {
            callsCollection.document(callId)
                .update(
                    mapOf(
                        "status" to CallStatus.ENDED.name,
                        "endedBy" to endedBy,
                        "endedAt" to Timestamp.now()
                    )
                ).await()

            Log.d(TAG, "Call ended: $callId by $endedBy")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end call", e)
            Result.failure(e)
        }
    }

    /**
     * Reject call
     */
    override suspend fun rejectCall(callId: String): Result<Unit> {
        return try {
            callsCollection.document(callId)
                .update("status", CallStatus.REJECTED.name)
                .await()
            Log.d(TAG, "Call rejected: $callId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject call", e)
            Result.failure(e)
        }
    }

    /**
     * Check if user is currently in a call
     */
    override suspend fun isUserInCall(userId: String): Boolean {
        return try {
            val activeCalls = callsCollection
                .whereIn("status", listOf(CallStatus.RINGING.name, CallStatus.CONNECTING.name, CallStatus.CONNECTED.name))
                .get()
                .await()

            activeCalls.documents.any { doc ->
                val callOffer = doc.toObject(CallOffer::class.java)
                callOffer?.callerId == userId || callOffer?.receiverId == userId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check user call status", e)
            false
        }
    }

    /**
     * Get call details
     */
    override suspend fun getCallDetails(callId: String): Result<CallOffer?> {
        return try {
            val doc = callsCollection.document(callId).get().await()
            val callOffer = doc.toObject(CallOffer::class.java)
            Result.success(callOffer)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get call details", e)
            Result.failure(e)
        }
    }

}