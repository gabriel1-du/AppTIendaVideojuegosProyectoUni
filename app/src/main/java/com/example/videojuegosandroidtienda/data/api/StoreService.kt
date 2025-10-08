package com.example.videojuegosandroidtienda.data.api

import com.example.videojuegosandroidtienda.data.entities.*
import retrofit2.http.GET
import retrofit2.http.Path

interface StoreService {
    @GET("videogame")
    suspend fun listVideogames(): List<Videogame>

    @GET("platform")
    suspend fun listPlatforms(): List<Platform>

    @GET("genre")
    suspend fun listGenres(): List<Genre>

    @GET("cart")
    suspend fun listCarts(): List<Cart>

    @GET("cart_item/{id}")
    suspend fun getCartItem(@Path("id") id: String): CartItem
}