package com.exa.android.letstalk.core.di

import com.exa.android.letstalk.data.local.room.ScheduledMessageRepositoryImpl
import com.exa.android.letstalk.data.usecase.ScheduledMessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindScheduledMessageRepository(
        impl: ScheduledMessageRepositoryImpl
    ): ScheduledMessageRepository
}
