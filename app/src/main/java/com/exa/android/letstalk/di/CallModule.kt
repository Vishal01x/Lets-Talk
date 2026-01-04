package com.exa.android.letstalk.di

import android.content.Context
import com.exa.android.letstalk.data.domain.call.CallSignalingRepository
import com.exa.android.letstalk.data.domain.call.CallWebRTCManager
import com.exa.android.letstalk.data.usecase.call.*
import com.exa.android.letstalk.utils.CallRingtoneManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

/**
 * Hilt module providing call-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object CallModule {
    
    @Provides
    @Singleton
    fun provideCallWebRTCManager(
        @ApplicationContext context: Context
    ): CallWebRTCManager {
        return CallWebRTCManager(context)
    }
    
    @Provides
    @Singleton
    fun provideCallSignalingRepository(
        firestore: FirebaseFirestore
    ): CallSignalingRepository {
        return CallSignalingRepository(firestore)
    }
    
    @Provides
    @Singleton
    fun provideCallRingtoneManager(
        @ApplicationContext context: Context
    ): CallRingtoneManager {
        return CallRingtoneManager(context)
    }
    
    @Provides
    @Singleton
    fun provideInitiateCallUseCase(
        webRTCManager: CallWebRTCManager,
        signalingRepository: CallSignalingRepository
    ): InitiateCallUseCase {
        return InitiateCallUseCase(webRTCManager, signalingRepository)
    }
    
    @Provides
    @Singleton
    fun provideAnswerCallUseCase(
        webRTCManager: CallWebRTCManager,
        signalingRepository: CallSignalingRepository
    ): AnswerCallUseCase {
        return AnswerCallUseCase(webRTCManager, signalingRepository)
    }
    
    @Provides
    @Singleton
    fun provideEndCallUseCase(
        webRTCManager: CallWebRTCManager,
        signalingRepository: CallSignalingRepository
    ): EndCallUseCase {
        return EndCallUseCase(webRTCManager, signalingRepository)
    }
    
    @Provides
    @Singleton
    fun provideRejectCallUseCase(
        signalingRepository: CallSignalingRepository
    ): RejectCallUseCase {
        return RejectCallUseCase(signalingRepository)
    }
    
    @Provides
    @Singleton
    fun provideObserveIncomingCallsUseCase(
        signalingRepository: CallSignalingRepository
    ): ObserveIncomingCallsUseCase {
        return ObserveIncomingCallsUseCase(signalingRepository)
    }
}
