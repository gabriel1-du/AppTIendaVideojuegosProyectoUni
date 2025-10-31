package com.example.videojuegosandroidtienda.data.api

import com.example.videojuegosandroidtienda.data.entities.*
import com.example.videojuegosandroidtienda.data.entities.createClasses.cartPost
import com.example.videojuegosandroidtienda.data.entities.createClasses.VideogameUpdateRequest
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.PATCH

interface StoreService {
    @GET("videogame")
    suspend fun listVideogames(): List<Videogame>

    @GET("videogame/{id}")
    suspend fun getVideogame(@Path("id") id: String): com.example.videojuegosandroidtienda.data.entities.VideogamePost2

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
        @Body req: VideogamePost2
    ): Videogame

    // Fallback absoluto si hubiese problemas con la base URL
    @POST("https://x8ki-letl-twmt.n7.xano.io/api:k6eLeFyi/videogame")
    suspend fun createVideogameAbsolute(
        @Body req: VideogamePost2
    ): Videogame

    @PATCH("videogame/{id}")
    suspend fun updateVideogame(
        @Path("id") id: String,
        @Body req: VideogameUpdateRequest
    ): Videogame

    @DELETE("videogame/{id}")
    suspend fun deleteVideogame(
        @Path("id") id: String
    ): Any?


}