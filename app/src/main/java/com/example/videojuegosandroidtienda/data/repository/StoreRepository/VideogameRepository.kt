package com.example.videojuegosandroidtienda.data.repository.StoreRepository


import com.example.videojuegosandroidtienda.data.api.StoreService
import com.example.videojuegosandroidtienda.data.entities.Genre
import com.example.videojuegosandroidtienda.data.entities.Platform
import com.example.videojuegosandroidtienda.data.entities.UploadResponse
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.entities.VideogamePost2
import com.example.videojuegosandroidtienda.data.entities.createClasses.VideogameUpdateRequest
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider
import okhttp3.MultipartBody
import retrofit2.HttpException


class VideogameRepository {

    companion object {
        private const val CACHE_WINDOW_MS = 20_000L

        private var cachedVideogames: List<Videogame>? = null
        private var cachedPlatforms: List<Platform>? = null
        private var cachedGenres: List<Genre>? = null

        private var lastVideogamesAt: Long = 0L
        private var lastPlatformsAt: Long = 0L
        private var lastGenresAt: Long = 0L
    }

    private val storeService: StoreService =
        RetrofitProvider.createService(ApiConfig.STORE_BASE_URL, StoreService::class.java)

    private val uploadClient = com.example.videojuegosandroidtienda.data.upload.UploadClient()
    suspend fun getVideogames(): List<Videogame> {
        val now = System.currentTimeMillis()
        val cached = cachedVideogames
        if (cached != null && cached.isNotEmpty() && (now - lastVideogamesAt) < CACHE_WINDOW_MS) {
            return cached
        }
        val fresh = storeService.listVideogames()
        cachedVideogames = fresh
        lastVideogamesAt = now
        return fresh
    }
    // Obtiene plataformas disponibles

    suspend fun getPlatforms(): List<Platform> {
        val now = System.currentTimeMillis()
        val cached = cachedPlatforms
        if (cached != null && cached.isNotEmpty() && (now - lastPlatformsAt) < CACHE_WINDOW_MS) {
            return cached
        }
        val fresh = storeService.listPlatforms()
        cachedPlatforms = fresh
        lastPlatformsAt = now
        return fresh
    }
    // Obtiene géneros disponibles
    suspend fun getGenres(): List<Genre> {
        val now = System.currentTimeMillis()
        val cached = cachedGenres
        if (cached != null && cached.isNotEmpty() && (now - lastGenresAt) < CACHE_WINDOW_MS) {
            return cached
        }
        val fresh = storeService.listGenres()
        cachedGenres = fresh
        lastGenresAt = now
        return fresh
    }

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

    suspend fun uploadImages(imageParts: List<MultipartBody.Part>): List<UploadResponse> {
        return imageParts.map { uploadClient.uploadFile(it) }
    }


    // Crea videojuego enviando JSON con cover_image completo
    suspend fun createVideogame(
        videogame: VideogamePost2
    ): Videogame {
        // LOG: Imprimir JSON, URL y headers
        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        android.util.Log.d("VideogameUpload", "JSON enviado: " + gson.toJson(videogame))
        android.util.Log.d("VideogameUpload", "URL: " + ApiConfig.STORE_BASE_URL + "videogame")
        android.util.Log.d("VideogameUpload", "Token: " + (com.example.videojuegosandroidtienda.data.network.TokenStore.token ?: "(sin token)"))
        try {
            return storeService.createVideogame(videogame)
        } catch (e: HttpException) {
            if (e.code() == 404) {
                // Fallback absoluto al endpoint
                return storeService.createVideogameAbsolute(videogame)
            }
            throw e
        }
    }

    // Obtiene un videojuego por id (incluye lista de imágenes adicionales)
    suspend fun getVideogameById(id: String): VideogamePost2 {
        return storeService.getVideogame(id)
    }

    suspend fun updateVideogame(
        id: String,
        title: String?,
        price: Int?,
        description: String?,
        genreId: String?,
        platformId: String?
    ): Videogame {
        val req = VideogameUpdateRequest(
            title = title,
            price = price,
            description = description,
            genre_id = genreId,
            platform_id = platformId
        )
        return storeService.updateVideogame(id, req)
    }

    suspend fun deleteVideogame(id: String) {
        storeService.deleteVideogame(id)
    }



}