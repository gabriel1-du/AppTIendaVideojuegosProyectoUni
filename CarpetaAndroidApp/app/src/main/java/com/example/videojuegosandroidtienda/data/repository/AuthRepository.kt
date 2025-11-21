package com.example.videojuegosandroidtienda.data.repository

import android.content.Context
import com.example.videojuegosandroidtienda.App
import retrofit2.HttpException
import com.example.videojuegosandroidtienda.data.api.AuthService
import com.example.videojuegosandroidtienda.data.entities.LoginRequest
import com.example.videojuegosandroidtienda.data.entities.SignupRequest
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider
import com.example.videojuegosandroidtienda.data.network.TokenStore

class AuthRepository {

    private val authService: AuthService =
        RetrofitProvider.createService(ApiConfig.AUTH_BASE_URL, AuthService::class.java)

    private val prefs by lazy {
        App.appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    suspend fun getAuthMe(): User = authService.getAuthMe()

    // Inicia sesión y devuelve token
    suspend fun login(email: String, password: String): String {
        val res = authService.login(LoginRequest(email, password))
        persistToken(res.token)
        return res.token
    }

    // Registra usuario y devuelve token
    suspend fun register(name: String, email: String, password: String): String {
        // Usar explícitamente /auth/signup según endpoints proporcionados
        val res = authService.signup(SignupRequest(name, email, password))
        persistToken(res.token)
        return res.token
    }

    // Guarda el token de forma persistente y en memoria
    private fun persistToken(token: String) {
        TokenStore.token = token
        prefs.edit().putString("auth_token", token).apply()
    }

    // Carga el token guardado (si existe) y lo coloca en TokenStore
    fun loadPersistedToken(): String? {
        val saved = prefs.getString("auth_token", null)
        TokenStore.token = saved
        return saved
    }

    // Borra el token tanto de memoria como del almacenamiento persistente
    fun logout() {
        TokenStore.token = null
        prefs.edit().remove("auth_token").apply()
    }

    // Verifica si la sesión es válida (token presente y /auth/me responde)
    suspend fun hasValidSession(): Boolean {
        val t = TokenStore.token ?: loadPersistedToken()
        if (t.isNullOrBlank()) return false
        return try {
            // Si no lanza excepción, la sesión es válida
            getAuthMe()
            true
        } catch (e: Exception) {
            // Solo cerrar sesión si el backend indica no autorizado
            if (e is HttpException && (e.code() == 401 || e.code() == 403)) {
                logout()
                false
            } else {
                // Errores de red/otros: mantener el token y considerar la sesión válida
                true
            }
        }
    }

}