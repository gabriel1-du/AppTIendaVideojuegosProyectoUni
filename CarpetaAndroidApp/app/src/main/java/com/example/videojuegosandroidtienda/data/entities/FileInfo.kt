package com.example.videojuegosandroidtienda.data.entities

data class FileInfo(
    val access: String?,
    val path: String?,
    val name: String?,
    val type: String?,
    val size: Long?,
    val mime: String?,
    val meta: Map<String, Any>?,
    val url: String?
)