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
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import kotlinx.coroutines.launch
import android.widget.Toast
import retrofit2.HttpException
import android.util.Log
import com.example.videojuegosandroidtienda.MainActivity

class LoginActivity : AppCompatActivity() {
    private val repository = AuthRepository()

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
            if (email.isBlank() || password.isBlank()) { //error
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.custom_toast_error, null)
                val textView = layout.findViewById<TextView>(R.id.toast_text)
                textView.text = "Completa email y contraseña"
                with(Toast(applicationContext)) {
                    duration = Toast.LENGTH_SHORT
                    view = layout
                    show()
                }
                return@setOnClickListener
            }
            lifecycleScope.launch { //ok
                try {
                    repository.login(email, password)
                    val inflater = layoutInflater
                    val layout = inflater.inflate(R.layout.custom_toast_ok, null)
                    val textView = layout.findViewById<TextView>(R.id.toast_text)
                    textView.text = "Login exitoso"
                    with(Toast(applicationContext)) {
                        duration = Toast.LENGTH_SHORT
                        view = layout
                        show()
                    }
                    // Redirigir a MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    val baseMsg = "Error al iniciar sesión"
                    if (e is HttpException) {
                        when (e.code()) {
                            403 -> { //error
                                val inflater = layoutInflater
                                val layout = inflater.inflate(R.layout.custom_toast_error, null)
                                val textView = layout.findViewById<TextView>(R.id.toast_text)
                                textView.text = "Correo o contraseña incorrectos"
                                with(Toast(applicationContext)) {
                                    duration = Toast.LENGTH_SHORT
                                    view = layout
                                    show()
                                }
                            }
                            429 -> {
                                val inflater = layoutInflater
                                val layout = inflater.inflate(R.layout.custom_toast_error, null)
                                val textView = layout.findViewById<TextView>(R.id.toast_text)
                                textView.text = "Límite de API alcanzado. Espera ~20s e intenta de nuevo"
                                with(Toast(applicationContext)) {
                                    duration = Toast.LENGTH_SHORT
                                    view = layout
                                    show()
                                }
                            }
                            else -> {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("LoginActivity", "HTTP ${e.code()} ${errBody ?: ""}")
                                val inflater = layoutInflater
                                val layout = inflater.inflate(R.layout.custom_toast_error, null)
                                val textView = layout.findViewById<TextView>(R.id.toast_text)
                                textView.text = "$baseMsg (HTTP ${e.code()})"
                                with(Toast(applicationContext)) {
                                    duration = Toast.LENGTH_SHORT
                                    view = layout
                                    show()
                                }
                            }
                        }
                    } else {
                        Log.e("LoginActivity", e.localizedMessage ?: baseMsg)
                        val inflater = layoutInflater
                        val layout = inflater.inflate(R.layout.custom_toast_error, null)
                        val textView = layout.findViewById<TextView>(R.id.toast_text)
                        textView.text = baseMsg
                        with(Toast(applicationContext)) {
                            duration = Toast.LENGTH_SHORT
                            view = layout
                            show()
                        }
                    }
                }
            }
        }

        linkSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}