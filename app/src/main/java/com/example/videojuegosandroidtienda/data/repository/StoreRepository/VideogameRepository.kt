package com.example.videojuegosandroidtienda.data.repository.StoreRepository


import com.example.videojuegosandroidtienda.data.api.StoreService
import com.example.videojuegosandroidtienda.data.entities.Genre
import com.example.videojuegosandroidtienda.data.entities.Platform
import com.example.videojuegosandroidtienda.data.entities.UploadResponse
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.entities.createClasses.CoverImageRef
import com.example.videojuegosandroidtienda.data.entities.createClasses.CreateVideogameRequest
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.util.UUID

class VideogameRepository {

    private val storeService: StoreService =
        RetrofitProvider.createService(ApiConfig.STORE_BASE_URL, StoreService::class.java)

    private val uploadClient = com.example.videojuegosandroidtienda.data.upload.UploadClient()
    suspend fun getVideogames(): List<Videogame> = storeService.listVideogames()
    // Obtiene plataformas disponibles

    suspend fun getPlatforms(): List<Platform> = storeService.listPlatforms()
    // Obtiene géneros disponibles
    suspend fun getGenres(): List<Genre> = storeService.listGenres()

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
            id = UUID.randomUUID().toString(), // Genera UUID único
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
