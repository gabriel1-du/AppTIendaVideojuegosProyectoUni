package com.example.videojuegosandroidtienda.data.entities

data class Cart(
    val id: String,
    val created_at: String?,
    val total: Double,
    val user_id: String
)