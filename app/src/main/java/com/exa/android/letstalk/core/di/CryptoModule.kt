package com.exa.android.letstalk.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.exa.android.letstalk.data.local.room.ScheduleMessageDatabase
import com.exa.android.letstalk.data.local.room.crypto.CryptoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Annotation to differentiate encrypted preferences from regular preferences.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SignalEncryptedPrefs

/**
 * Hilt module providing Signal Protocol cryptography dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    /**
     * Provides EncryptedSharedPreferences for secure storage of Signal Protocol private keys.
     * Uses Android Keystore for key protection.
     */
    @Provides
    @Singleton
    @SignalEncryptedPrefs
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "signal_secure_preferences",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to create EncryptedSharedPreferences", e)
        }
    }

    /**
     * Provides CryptoDao for Signal Protocol database operations.
     * Used by SignalProtocolStoreImpl for storing sessions and pre-keys.
     */
    @Provides
    @Singleton
    fun provideCryptoDao(database: ScheduleMessageDatabase): CryptoDao {
        return database.cryptoDao()
    }
}
