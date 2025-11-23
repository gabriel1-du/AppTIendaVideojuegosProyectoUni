package com.example.videojuegosandroidtienda.data.network

import okhttp3.OkHttpClient
import okhttp3.Cache
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.google.gson.GsonBuilder
import com.example.videojuegosandroidtienda.data.entities.AuthTokenResponse
import com.example.videojuegosandroidtienda.App
import com.chuckerteam.chucker.api.ChuckerInterceptor
import java.util.concurrent.TimeUnit
import java.io.File

object RetrofitProvider {
    @Volatile
    private var lastRequestAt: Long = 0L
    private const val MIN_INTERVAL_MS = 600L

    private fun okHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val cache = Cache(File(App.appContext.cacheDir, "http_cache"), 10L * 1024L * 1024L)
        val defaultCacheHeaderInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            // Solo ajustar cache para GET
            return@Interceptor if (request.method.equals("GET", ignoreCase = true)) {
                val hasCacheHeader = response.header("Cache-Control") != null
                if (hasCacheHeader) response else response.newBuilder()
                    .header("Cache-Control", "public, max-age=15")
                    .build()
            } else response
        }
        val rateLimitAndRetryInterceptor = Interceptor { chain ->
            var backoffMs = 800L
            val maxRetries = 2
            for (attempt in 0..maxRetries) {
                val now = System.currentTimeMillis()
                val since = now - lastRequestAt
                if (since in 1 until MIN_INTERVAL_MS) {
                    try { Thread.sleep(MIN_INTERVAL_MS - since) } catch (_: InterruptedException) {}
                }
                val request = chain.request()
                val response = chain.proceed(request)
                lastRequestAt = System.currentTimeMillis()
                if (response.code != 429) {
                    return@Interceptor response
                }
                response.close()
                if (attempt == maxRetries) {
                    return@Interceptor chain.proceed(request)
                }
                val retryAfterHeader = response.header("Retry-After")
                val retryAfterMs = retryAfterHeader?.toLongOrNull()?.let { it * 1000 } ?: backoffMs
                val jitter = (Math.random() * 300).toLong()
                try { Thread.sleep(retryAfterMs + jitter) } catch (_: InterruptedException) {}
                backoffMs = (backoffMs * 1.5).toLong().coerceAtMost(4000L)
            }
            chain.proceed(chain.request())
        }
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val t = TokenStore.token
            val req = if (!t.isNullOrBlank()) {
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $t")
                    .build()
            } else original
            chain.proceed(req)
        }
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(rateLimitAndRetryInterceptor)
            .addInterceptor(ChuckerInterceptor.Builder(App.appContext).build())
            .addNetworkInterceptor(defaultCacheHeaderInterceptor)
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun create(baseUrl: String): Retrofit {
        val gson = GsonBuilder()
            .registerTypeAdapter(AuthTokenResponse::class.java, AuthTokenResponseDeserializer())
            .create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun <T> createService(baseUrl: String, serviceClass: Class<T>): T {
        return create(baseUrl).create(serviceClass)
    }
}
