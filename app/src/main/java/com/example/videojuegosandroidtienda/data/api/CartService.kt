package com.example.videojuegosandroidtienda.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface CartService {
    @GET("cart/{cart_id}")
    suspend fun getCartById(@Path("cart_id") cartId: String): CartResponse

    @PATCH("cart/{cart_id}")
    suspend fun patchCart(
        @Path("cart_id") cartId: String,
        @Body req: CartUpdateRequest
    ): CartResponse

    @DELETE("cart/{cart_id}")
    suspend fun deleteCart(@Path("cart_id") cartId: String): Unit
}



data class CartResponse(
    val id: String,
    val created_at: Long,
    val total: Int,
    val user_id: String,
    val aprobado: Boolean,
    val videogames_id: List<String>?
)

data class CartUpdateRequest(
    val id: String,
    val created_at: Long,
    val total: Int,
    val user_id: String,
    val aprobado: Boolean,
    val videogames_id: List<String>?
)
