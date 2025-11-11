package com.example.videojuegosandroidtienda.data.repository.StoreRepository

import com.example.videojuegosandroidtienda.data.api.UserService
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider

class UserRepository {

    private val service: UserService =
        RetrofitProvider.createService(ApiConfig.STORE_BASE_URL, UserService::class.java)

    suspend fun listUsers(): List<User> = service.listUsers()

    suspend fun getUser(id: String): User = service.getUser(id)

    suspend fun createUser(req: User): User = service.createUser(req)

    suspend fun patchUser(id: String, fields: Map<String, Any?>): User = service.patchUser(id, fields)

    suspend fun putUser(id: String, req: User): User = service.putUser(id, req)

    suspend fun deleteUser(id: String) = service.deleteUser(id)
}