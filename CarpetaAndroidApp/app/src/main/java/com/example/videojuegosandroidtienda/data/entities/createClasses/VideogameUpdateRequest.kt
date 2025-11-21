package com.example.videojuegosandroidtienda.data.entities.createClasses

data class VideogameUpdateRequest(
    val title: String?,
    val price: Int?,
    val description: String?,
    val genre_id: String?,
    val platform_id: String?
)