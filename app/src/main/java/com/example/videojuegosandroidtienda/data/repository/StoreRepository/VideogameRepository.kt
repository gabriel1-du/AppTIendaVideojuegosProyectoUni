package com.example.videojuegosandroidtienda.data.repository.StoreRepository


import com.example.videojuegosandroidtienda.data.api.StoreService
import com.example.videojuegosandroidtienda.data.entities.Genre
import com.example.videojuegosandroidtienda.data.entities.Platform
import com.example.videojuegosandroidtienda.data.entities.UploadResponse
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.entities.VideogamePost2
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider
import okhttp3.MultipartBody
import retrofit2.HttpException


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



}