package com.example.videojuegosandroidtienda.data.entities

// Se elimina la clase ImagePath que era la fuente de los errores.

data class VideogamePost2(
    val id: String,
    val created_at: String,
    val title: String,
    val price: Int,
    val description: String,
    val cover_image: UploadResponse, // Se usa UploadResponse directamente
    val genre_id: String,
    val platform_id: String,
    val images: List<UploadResponse> // Se usa una lista de UploadResponse
)
