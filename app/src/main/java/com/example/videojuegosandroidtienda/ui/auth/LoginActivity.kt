package com.example.videojuegosandroidtienda.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.repository.StoreRepository
import kotlinx.coroutines.launch
import android.widget.Toast
import retrofit2.HttpException
import android.util.Log
import com.example.videojuegosandroidtienda.MainActivity

class LoginActivity : AppCompatActivity() {
    private val repository = StoreRepository()

    // Maneja inicio de sesión y navegación según autenticación
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val linkSignup = findViewById<TextView>(R.id.linkSignup)

        // Prefill del email si viene desde registro
        intent.getStringExtra("prefill_email")?.let { prefill ->
            inputEmail.setText(prefill)
        }

        buttonLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this@LoginActivity, "Completa email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Toast.makeText(this@LoginActivity, "La contraseña debe ser mas de 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!password.any { it.isLetter() }) {
                Toast.makeText(this@LoginActivity, "La contraseña debe conetenr un caracter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    repository.login(email, password)
                    // Redirigir a MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    val baseMsg = "Error al iniciar sesión"
                    if (e is HttpException) {
                        when (e.code()) {
                            403 -> {
                                Toast.makeText(this@LoginActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("LoginActivity", "HTTP ${e.code()} ${errBody ?: ""}")
                                Toast.makeText(this@LoginActivity, "$baseMsg (HTTP ${e.code()})", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("LoginActivity", e.localizedMessage ?: baseMsg)
                        Toast.makeText(this@LoginActivity, baseMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        linkSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}