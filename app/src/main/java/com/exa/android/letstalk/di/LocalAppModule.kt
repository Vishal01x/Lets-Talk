package com.exa.android.letstalk.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.exa.android.letstalk.data.local.pref.UserPreferences
import com.exa.android.letstalk.data.local.room.ScheduleMessageDatabase
import com.exa.android.letstalk.data.local.room.ScheduledMessageEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }


    @Provides
    @Singleton
    fun provideUserPreferences(dataStore: DataStore<Preferences>): UserPreferences {
        return UserPreferences(dataStore)
    }


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScheduleMessageDatabase {
        return Room.databaseBuilder(
            context,
            ScheduleMessageDatabase::class.java,
            "scheduled_messages_db"
        ).build()
    }
}
