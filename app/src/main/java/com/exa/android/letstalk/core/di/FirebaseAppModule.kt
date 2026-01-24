package com.exa.android.letstalk.core.di

import com.exa.android.letstalk.core.utils.Constants.CLOUDINARY_BASE_URL
import com.exa.android.letstalk.data.api.CloudinaryApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideRealTimeFirebase() = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun providesCloudinaryApi(): CloudinaryApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(CLOUDINARY_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(CloudinaryApi::class.java)
    }
}