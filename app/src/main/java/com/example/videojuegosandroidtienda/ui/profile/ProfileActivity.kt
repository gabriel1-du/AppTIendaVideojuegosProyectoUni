package com.example.videojuegosandroidtienda.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import retrofit2.HttpException
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.functions.setupBottomNavigation
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.data.viewmodel.UserViewModel
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private val repository = AuthRepository()
    private val userViewModel: UserViewModel by viewModels()

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
        val buttonDashboard = findViewById<Button>(R.id.buttonIraDashboard)
        buttonDashboard.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, com.example.videojuegosandroidtienda.ui.adminUi.AdminCenterActivity::class.java))
        }

        lifecycleScope.launch {
            try {
                val authUser = repository.getAuthMe()
                val fetchedUser = userViewModel.getUser(authUser.id)
                if (fetchedUser.bloqueo) {
                    repository.logout()
                    showCustomErrorToast(this@ProfileActivity, getString(R.string.user_blocked))
                    startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }
                textName.text = getString(R.string.profile_name_format, fetchedUser.name)
                textEmail.text = getString(R.string.profile_email_format, fetchedUser.email)
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 429) {
                    showCustomErrorToast(this@ProfileActivity, getString(R.string.api_limit_error_retry))
                } else {
                    showCustomErrorToast(this@ProfileActivity, getString(R.string.profile_load_error))
                }
            }
        }

        buttonLogout.setOnClickListener {
            repository.logout()
            showCustomOkToast(this@ProfileActivity, getString(R.string.logout_success))
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }
    }
}