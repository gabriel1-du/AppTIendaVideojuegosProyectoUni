package com.example.videojuegosandroidtienda

import android.app.Application
import android.content.Context
import com.example.videojuegosandroidtienda.data.repository.AuthRepository

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        // Inicializar token guardado para que todas las Activities lo tengan disponible
        try {
            AuthRepository().loadPersistedToken()
        } catch (_: Exception) { }
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}