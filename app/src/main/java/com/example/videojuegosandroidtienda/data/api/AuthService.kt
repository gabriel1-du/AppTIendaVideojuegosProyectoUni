package com.example.videojuegosandroidtienda.data.api

import com.example.videojuegosandroidtienda.data.entities.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @GET("auth/me")
    suspend fun getAuthMe(): User

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthTokenResponse

    @POST("auth/register")
    suspend fun register(@Body req: SignupRequest): AuthTokenResponse

    // Fallback si el backend usa "signup" en lugar de "register"
    @POST("auth/signup")
    suspend fun signup(@Body req: SignupRequest): AuthTokenResponse
}