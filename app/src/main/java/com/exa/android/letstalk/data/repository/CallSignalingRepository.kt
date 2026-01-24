package com.exa.android.letstalk.data.repository

import com.exa.android.letstalk.domain.CallAnswer
import com.exa.android.letstalk.domain.CallOffer
import com.exa.android.letstalk.domain.CallStatus
import com.exa.android.letstalk.domain.IceCandidate
import kotlinx.coroutines.flow.Flow

/**
 * Interface for WebRTC signaling repository
 */
interface CallSignalingRepository {
    suspend fun createCallOffer(callOffer: CallOffer): Result<String>
    suspend fun sendCallAnswer(callId: String, answer: CallAnswer): Result<Unit>
    suspend fun addIceCandidate(callId: String, userId: String, candidate: IceCandidate): Result<Unit>
    fun observeIncomingCalls(userId: String): Flow<CallOffer>
    fun observeCallAnswer(callId: String): Flow<CallAnswer?>
    fun observeIceCandidates(callId: String, remotePeerId: String): Flow<IceCandidate>
    suspend fun updateCallStatus(callId: String, status: CallStatus): Result<Unit>
    suspend fun endCall(callId: String, endedBy: String): Result<Unit>
    suspend fun rejectCall(callId: String): Result<Unit>
    suspend fun isUserInCall(userId: String): Boolean
    suspend fun getCallDetails(callId: String): Result<CallOffer?>

}