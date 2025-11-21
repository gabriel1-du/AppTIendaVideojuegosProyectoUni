package com.example.videojuegosandroidtienda.data.entities

data class Videogame(
    val id: String,
    val created_at: String?,
    val title: String,
    val price: Double,
    val description: String?,
    val genre_id: String,
    val platform_id: String,
    val cover_image: FileInfo?
)