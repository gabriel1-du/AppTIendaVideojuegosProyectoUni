package com.example.videojuegosandroidtienda.data.repository

import com.example.videojuegosandroidtienda.data.api.AuthService
import com.example.videojuegosandroidtienda.data.api.StoreService
import com.example.videojuegosandroidtienda.data.entities.*
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider
import com.example.videojuegosandroidtienda.data.network.TokenStore
import retrofit2.HttpException

class StoreRepository {
    private val storeService: StoreService =
        RetrofitProvider.createService(ApiConfig.STORE_BASE_URL, StoreService::class.java)
    private val authService: AuthService =
        RetrofitProvider.createService(ApiConfig.AUTH_BASE_URL, AuthService::class.java)

    // Obtiene lista de videojuegos desde el servicio de tienda
    suspend fun getVideogames(): List<Videogame> = storeService.listVideogames()
    // Obtiene plataformas disponibles
    suspend fun getPlatforms(): List<Platform> = storeService.listPlatforms()
    // Obtiene géneros disponibles
    suspend fun getGenres(): List<Genre> = storeService.listGenres()
    // Lista carritos remotos (ejemplo/demo)
    suspend fun getCarts(): List<Cart> = storeService.listCarts()
    // Obtiene detalle de ítem de carrito por id
    suspend fun getCartItem(id: String): CartItem = storeService.getCartItem(id)
    // Obtiene datos del usuario autenticado
    suspend fun getAuthMe(): User = authService.getAuthMe()

    // Inicia sesión y devuelve token
    suspend fun login(email: String, password: String): String {
        val res = authService.login(LoginRequest(email, password))
        TokenStore.token = res.token
        return res.token
    }

    // Registra usuario y devuelve token
    suspend fun register(name: String, email: String, password: String): String {
        // Usar explícitamente /auth/signup según endpoints proporcionados
        val res = authService.signup(SignupRequest(name, email, password))
        TokenStore.token = res.token
        return res.token
    }

    // Filtra videojuegos por texto, plataforma y género
    fun filterVideogames(
        all: List<Videogame>,
        query: String?,
        platformId: String?,
        genreId: String?
    ): List<Videogame> {
        val q = query?.trim()?.lowercase().orEmpty()
        return all.filter { vg ->
            val matchesQuery = if (q.isEmpty()) true else vg.title.lowercase().contains(q)
            val matchesPlatform = platformId?.let { vg.platform_id == it } ?: true
            val matchesGenre = genreId?.let { vg.genre_id == it } ?: true
            matchesQuery && matchesPlatform && matchesGenre
        }
    }
}