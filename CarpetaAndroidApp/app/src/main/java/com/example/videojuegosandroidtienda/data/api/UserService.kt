package com.example.videojuegosandroidtienda.data.api

import com.example.videojuegosandroidtienda.data.entities.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @GET("user")
    suspend fun listUsers(): List<User>

    @GET("user/{user_id}")
    suspend fun getUser(@Path("user_id") id: String): User

    @POST("user")
    suspend fun createUser(@Body req: User): User

    @PATCH("user/{user_id}")
    suspend fun patchUser(@Path("user_id") id: String, @Body fields: Map<String, Any?>): User

    @PUT("user/{user_id}")
    suspend fun putUser(@Path("user_id") id: String, @Body req: User): User

    @DELETE("user/{user_id}")
    suspend fun deleteUser(@Path("user_id") id: String): Unit
}