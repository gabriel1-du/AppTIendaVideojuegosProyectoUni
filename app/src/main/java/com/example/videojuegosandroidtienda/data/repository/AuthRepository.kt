package com.example.videojuegosandroidtienda.data.repository

import com.example.videojuegosandroidtienda.data.api.AuthService
import com.example.videojuegosandroidtienda.data.entities.LoginRequest
import com.example.videojuegosandroidtienda.data.entities.SignupRequest
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider
import com.example.videojuegosandroidtienda.data.network.TokenStore

class AuthRepository {

    private val authService: AuthService =
        RetrofitProvider.createService(ApiConfig.AUTH_BASE_URL, AuthService::class.java)

    suspend fun getAuthMe(): User = authService.getAuthMe()

    // Inicia sesión y devuelve token
    suspend fun login(email: String, password: String): String {
        val res = authService.login(LoginRequest(email, password))
        TokenStore.token = res.token
        return res.token
    }

    // Registra usuario y devuelve token
    suspend fun register(name: String, email: String, password: String): String {
        // Usar explícitamente /auth/signup según endpoints proporcionados
        val res = authService.signup(SignupRequest(name, email, password))
        TokenStore.token = res.token
        return res.token
    }

}