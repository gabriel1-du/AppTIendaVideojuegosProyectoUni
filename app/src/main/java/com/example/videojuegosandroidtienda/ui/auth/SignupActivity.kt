package com.example.videojuegosandroidtienda.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.repository.StoreRepository
import kotlinx.coroutines.launch
import android.widget.Toast
import android.content.Intent
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import retrofit2.HttpException
import android.util.Log

class SignupActivity : AppCompatActivity() {
    private val repository = StoreRepository()

    // Registra usuario y redirige según resultado
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        val inputName = findViewById<EditText>(R.id.inputName)
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val buttonSignup = findViewById<Button>(R.id.buttonSignup)

        buttonSignup.setOnClickListener {
            val name = inputName.text.toString()
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this@SignupActivity, "Completa nombre, email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Toast.makeText(this@SignupActivity, "La contraseña debe ser mas de 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!password.any { it.isLetter() }) {
                Toast.makeText(this@SignupActivity, "La contraseñai debe conetenr un caracter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    repository.register(name, email, password)
                    // No iniciar sesión automáticamente tras crear cuenta
                    TokenStore.token = null
                    Toast.makeText(this@SignupActivity, "Cuenta creada. Inicia sesión.", Toast.LENGTH_SHORT).show()
                    // Ir a pantalla de login y prellenar el email
                    val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                    intent.putExtra("prefill_email", email)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    val baseMsg = "Error al crear cuenta"
                    if (e is HttpException) {
                        val code = e.code()
                        val errBody = e.response()?.errorBody()?.string()
                        Log.e("SignupActivity", "HTTP $code ${errBody ?: ""}")
                        Toast.makeText(this@SignupActivity, "$baseMsg (HTTP $code)", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("SignupActivity", e.localizedMessage ?: baseMsg)
                        Toast.makeText(this@SignupActivity, baseMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}