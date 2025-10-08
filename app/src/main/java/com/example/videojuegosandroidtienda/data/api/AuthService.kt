package com.example.videojuegosandroidtienda.data.api

import com.example.videojuegosandroidtienda.data.entities.User
import retrofit2.http.GET

interface AuthService {
    @GET("auth/me")
    suspend fun getAuthMe(): User
}