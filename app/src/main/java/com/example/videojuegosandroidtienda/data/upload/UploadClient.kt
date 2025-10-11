package com.example.videojuegosandroidtienda.data.upload

import com.example.videojuegosandroidtienda.data.upload.UploadService
import com.example.videojuegosandroidtienda.data.upload.UploadFilesService
import com.example.videojuegosandroidtienda.data.entities.UploadResponse
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.min

class UploadClient(
    baseUrl: String = ApiConfig.STORE_BASE_URL
) {
    private val uploadService: UploadService =
        RetrofitProvider.createService(baseUrl, UploadService::class.java)
    private val uploadFilesService: UploadFilesService =
        RetrofitProvider.createService(baseUrl, UploadFilesService::class.java)
    private val uploadFileService: UploadFileService =
        RetrofitProvider.createService(baseUrl, UploadFileService::class.java)

    suspend fun uploadFile(file: MultipartBody.Part): UploadResponse {
        // Preferimos /upload primero (lo más sencillo de configurar en Xano)
        var attempt = 0
        val maxAttempts = 3
        var lastError: Throwable? = null
        val delays = listOf(250L, 750L, 1500L)
        while (attempt < maxAttempts) {
            try {
                return uploadService.uploadFile(file)
            } catch (t: Throwable) {
                lastError = t
                val shouldRetry = when (t) {
                    is IOException -> true
                    is HttpException -> t.code() >= 500
                    else -> false
                }
                if (!shouldRetry) break
                val sleepMs = delays[min(attempt, delays.lastIndex)]
                try { Thread.sleep(sleepMs) } catch (_: InterruptedException) {}
                attempt++
            }
        }

        // Fallback 404: URL absoluta a /file/upload
        try {
            return uploadFileService.uploadFileAbsolute(file)
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
        }

        // Compatibilidad adicional: /files/upload y /file/upload
        try {
            return uploadFilesService.uploadFile(file)
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
        }
        return uploadFileService.uploadFile(file)
    }
}