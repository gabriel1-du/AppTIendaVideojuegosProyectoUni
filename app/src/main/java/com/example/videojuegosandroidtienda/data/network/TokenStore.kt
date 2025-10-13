package com.example.videojuegosandroidtienda.data.network

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT

object TokenStore {
    @Volatile
    var token: String? = null

    fun getUserId(): String? {
        return try {
            token?.let {
                val decodedJWT: DecodedJWT = JWT.decode(it)
                // El ID de usuario a menudo se encuentra en la claim 'sub' (subject).
                // Ajusta esto si tu token usa una claim diferente para el ID de usuario.
                decodedJWT.subject
            }
        } catch (e: Exception) {
            // Manejar el error, por ejemplo, registrarlo o devolver nulo si falla la decodificación
            null
        }
    }
}