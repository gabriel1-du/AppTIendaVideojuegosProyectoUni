package com.example.videojuegosandroidtienda.data.repository

import com.example.videojuegosandroidtienda.data.api.AuthService
import com.example.videojuegosandroidtienda.data.api.StoreService
// Upload services are encapsulated via UploadClient
import com.example.videojuegosandroidtienda.data.entities.*
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider
import com.example.videojuegosandroidtienda.data.network.TokenStore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import com.example.videojuegosandroidtienda.data.entities.CreateVideogameRequest
import com.example.videojuegosandroidtienda.data.entities.CoverImageRef

class StoreRepository {
    private val storeService: StoreService =
        RetrofitProvider.createService(ApiConfig.STORE_BASE_URL, StoreService::class.java)
    private val authService: AuthService =
        RetrofitProvider.createService(ApiConfig.AUTH_BASE_URL, AuthService::class.java)
    private val uploadClient = com.example.videojuegosandroidtienda.data.upload.UploadClient()

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

    // Sube archivo y devuelve objeto completo para usar en cover_image
    suspend fun uploadCoverImage(imagePart: MultipartBody.Part): UploadResponse {
        return uploadClient.uploadFile(imagePart)
    }


    // Crea videojuego enviando JSON con cover_image completo
    suspend fun createVideogameJson(
        title: String,
        platformId: String,
        genreId: String,
        price: Int,
        description: String?,
        cover: UploadResponse,
        overrideName: String? = null,
        overrideMime: String? = null
    ): Videogame {
        val req = CreateVideogameRequest(
            id = java.util.UUID.randomUUID().toString(), // Genera UUID único
            created_at = System.currentTimeMillis(), // Timestamp actual en ms
            title = title,
            price = price,
            description = description ?: "",
            cover_image = CoverImageRef(
                path = cover.path,
                name = cover.name ?: overrideName,
                mime = cover.mime ?: overrideMime,
                access = cover.access,
                type = cover.type,
                size = cover.size,
                url = cover.url,
                meta = cover.meta // Asegura que meta se incluya si existe
            ),
            genre_id = genreId,
            platform_id = platformId
        )
        // LOG: Imprimir JSON, URL y headers
        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        android.util.Log.d("VideogameUpload", "JSON enviado: " + gson.toJson(req))
        android.util.Log.d("VideogameUpload", "URL: " + ApiConfig.STORE_BASE_URL + "videogame")
        android.util.Log.d("VideogameUpload", "Token: " + (com.example.videojuegosandroidtienda.data.network.TokenStore.token ?: "(sin token)"))
        try {
            return storeService.createVideogame(req)
        } catch (e: HttpException) {
            if (e.code() == 404) {
                // Fallback absoluto al endpoint
                return storeService.createVideogameAbsolute(req)
            }
            throw e
        }
    }

    
}