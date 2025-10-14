package com.example.videojuegosandroidtienda.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import retrofit2.HttpException
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.functions.setupBottomNavigation
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private val repository = AuthRepository()

    // Muestra nombre y email del usuario y permite cerrar sesión
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Cargar token persistido, si existe
        repository.loadPersistedToken()

        // Si no hay sesión, enviar a login
        val token = TokenStore.token
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        setupBottomNavigation(this, bottomNav, R.id.nav_profile)

        val textName = findViewById<TextView>(R.id.textName)
        val textEmail = findViewById<TextView>(R.id.textEmail)
        val buttonLogout = findViewById<Button>(R.id.buttonLogout)

        lifecycleScope.launch {
            try {
                val user = repository.getAuthMe()
                textName.text = "Nombre: ${user.name}"
                textEmail.text = "Email: ${user.email}"
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 429) {
                    Toast.makeText(this@ProfileActivity, "Límite de API alcanzado. Espera ~20s e intenta de nuevo", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Error al cargar usuario", Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonLogout.setOnClickListener {
            repository.logout()
            Toast.makeText(this@ProfileActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }
    }
}