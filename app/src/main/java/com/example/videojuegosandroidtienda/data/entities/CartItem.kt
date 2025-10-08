package com.example.videojuegosandroidtienda.data.entities

data class CartItem(
    val id: String,
    val created_at: String?,
    val quantity: Int,
    val cart_id: String,
    val videogame_id: String
)