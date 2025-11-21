package com.example.videojuegosandroidtienda.data.upload

import com.example.videojuegosandroidtienda.data.entities.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadFileService {
    @Multipart
    @POST("file/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadResponse

    // Fallback absoluto a la URL completa de Xano
    @Multipart
    @POST("https://x8ki-letl-twmt.n7.xano.io/api:k6eLeFyi/file/upload")
    suspend fun uploadFileAbsolute(
        @Part file: MultipartBody.Part
    ): UploadResponse
}

