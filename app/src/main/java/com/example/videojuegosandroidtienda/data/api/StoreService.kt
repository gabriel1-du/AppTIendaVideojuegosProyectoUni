package com.example.videojuegosandroidtienda.data.api

import com.example.videojuegosandroidtienda.data.entities.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @Multipart
    @POST("videogame")
    suspend fun createVideogame(
        @Part("title") title: RequestBody,
        @Part("platform_id") platformId: RequestBody,
        @Part("genre_id") genreId: RequestBody,
        @Part("price") price: RequestBody,
        @Part("description") description: RequestBody?,
        @Part cover_image: MultipartBody.Part
    ): Videogame
}