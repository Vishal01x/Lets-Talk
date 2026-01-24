package com.exa.android.letstalk.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Cloudinary response data
data class CloudinaryUploadResponse(
    val secure_url: String
)

// Cloudinary Retrofit API
interface CloudinaryApi {
    @Multipart
    @POST("v1_1/dgqxusedq/raw/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryUploadResponse
}
