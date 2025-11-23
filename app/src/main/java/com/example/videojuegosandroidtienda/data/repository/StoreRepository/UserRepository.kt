package com.example.videojuegosandroidtienda.data.repository.StoreRepository

import com.example.videojuegosandroidtienda.data.api.UserService
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider

class UserRepository {

    companion object {
        private const val CACHE_WINDOW_MS = 20_000L

        private var cachedUsers: List<User>? = null
        private var lastUsersAt: Long = 0L

        private val cachedUserById: MutableMap<String, Pair<User, Long>> = mutableMapOf()
    }

    private val service: UserService =
        RetrofitProvider.createService(ApiConfig.STORE_BASE_URL, UserService::class.java)

    suspend fun listUsers(): List<User> {
        val now = System.currentTimeMillis()
        val cached = cachedUsers
        if (cached != null && cached.isNotEmpty() && (now - lastUsersAt) < CACHE_WINDOW_MS) {
            return cached
        }
        val fresh = service.listUsers()
        cachedUsers = fresh
        lastUsersAt = now
        return fresh
    }

    suspend fun getUser(id: String): User {
        val now = System.currentTimeMillis()
        val hit = cachedUserById[id]
        if (hit != null && (now - hit.second) < CACHE_WINDOW_MS) {
            return hit.first
        }
        val fresh = service.getUser(id)
        cachedUserById[id] = fresh to now
        return fresh
    }

    suspend fun createUser(req: User): User = service.createUser(req)

    suspend fun patchUser(id: String, fields: Map<String, Any?>): User {
        val user = service.patchUser(id, fields)
        cachedUsers = null
        lastUsersAt = 0L
        cachedUserById.remove(id)
        return user
    }

    suspend fun putUser(id: String, req: User): User {
        val user = service.putUser(id, req)
        cachedUsers = null
        lastUsersAt = 0L
        cachedUserById[id] = user to System.currentTimeMillis()
        return user
    }

    suspend fun deleteUser(id: String) {
        service.deleteUser(id)
        cachedUsers = null
        lastUsersAt = 0L
        cachedUserById.remove(id)
    }
}
