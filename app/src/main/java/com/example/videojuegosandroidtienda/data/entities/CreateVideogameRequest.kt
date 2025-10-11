package com.example.videojuegosandroidtienda.data.entities

data class ImageMeta(
    val width: Int? = null,
    val height: Int? = null
)

data class CoverImageRef(
    val path: String,
    val name: String? = null,
    val mime: String? = null,
    val access: String? = null,
    val type: String? = null,
    val size: Int? = null,
    val url: String? = null,
    val meta: ImageMeta? = null
)

data class CreateVideogameRequest(
    val id: String?, // UUID generado en cliente o nulo para que lo genere el backend
    val created_at: Long?, // timestamp en milisegundos o nulo para que lo genere el backend
    val title: String,
    val price: Int,
    val description: String?,
    val cover_image: CoverImageRef,
    val genre_id: String,
    val platform_id: String
)