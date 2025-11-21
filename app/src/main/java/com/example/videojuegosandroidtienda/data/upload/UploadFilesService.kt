package com.example.videojuegosandroidtienda.data.upload

import com.example.videojuegosandroidtienda.data.entities.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Fallback para APIs que exponen subida en /files/upload
interface UploadFilesService {
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadResponse
}