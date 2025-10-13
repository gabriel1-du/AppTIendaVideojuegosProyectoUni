package com.example.videojuegosandroidtienda.data.api

import com.example.videojuegosandroidtienda.data.entities.*
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import com.example.videojuegosandroidtienda.data.entities.createClasses.CreateVideogameRequest
import com.example.videojuegosandroidtienda.data.entities.createClasses.cartPost

interface StoreService {
    @GET("videogame")
    suspend fun listVideogames(): List<Videogame>

    @GET("platform")
    suspend fun listPlatforms(): List<Platform>

    @GET("genre")
    suspend fun listGenres(): List<Genre>

    @GET("cart")
    suspend fun listCarts(): List<Cart>

    @POST("cart")
    suspend fun createCart(@Body req: cartPost): Cart

    @GET("cart_item/{id}")
    suspend fun getCartItem(@Path("id") id: String): CartItem

    @POST("videogame")
    suspend fun createVideogame(
        @Body req: CreateVideogameRequest
    ): Videogame

    // Fallback absoluto si hubiese problemas con la base URL
    @POST("https://x8ki-letl-twmt.n7.xano.io/api:k6eLeFyi/videogame")
    suspend fun createVideogameAbsolute(
        @Body req: CreateVideogameRequest
    ): Videogame


}