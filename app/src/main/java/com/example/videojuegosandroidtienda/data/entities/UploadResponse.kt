package com.example.videojuegosandroidtienda.data.entities

data class UploadResponse(
    val path: String,
    val mime: String?,
    val name: String?,
    val access: String? = null,
    val type: String? = null,
    val size: Int? = null,
    val url: String? = null,
    val meta: ImageMeta? = null
)