package com.example.videojuegosandroidtienda.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.google.gson.GsonBuilder
import com.example.videojuegosandroidtienda.data.entities.AuthTokenResponse

object RetrofitProvider {
    private fun okHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
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
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
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