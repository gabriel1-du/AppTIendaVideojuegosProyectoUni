package com.example.videojuegosandroidtienda.data.entities

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthTokenResponse(
    val token: String
)