package com.example.videojuegosandroidtienda.data.entities.createClasses

//Clase para enviar una entidad a la bda
data class cartPost(
    var id: String?,
    var created_at: Long,
    var total: Double,
    var user_id: String?
)
