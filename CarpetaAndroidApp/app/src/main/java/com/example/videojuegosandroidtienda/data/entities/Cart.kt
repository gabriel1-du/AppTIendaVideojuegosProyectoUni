package com.example.videojuegosandroidtienda.data.entities

data class Cart(
    val id: String,
    val created_at: Long,
    val total: Double,
    val user_id: String,
    val aprobado: Boolean? = null,
    val videogames_id: List<String>? = null
)