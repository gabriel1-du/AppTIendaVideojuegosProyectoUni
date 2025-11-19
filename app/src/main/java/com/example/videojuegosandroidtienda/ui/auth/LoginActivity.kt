package com.example.videojuegosandroidtienda.ui.auth

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import kotlinx.coroutines.launch
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import retrofit2.HttpException
import android.util.Log
import com.example.videojuegosandroidtienda.MainActivity
import androidx.core.graphics.drawable.toDrawable

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
                val dialog = Dialog(this@LoginActivity) // Crea un nuevo Dialog
                dialog.setContentView(R.layout.custom_toast_error) //Uso de loayout error

                //Esquinas redondas para el toast
                dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

                val textView = dialog.findViewById<TextView>(R.id.toast_text)
                textView.text = getString(R.string.login_fill_credentials)//Seteo de texto con credenciales

                dialog.show()
                return@setOnClickListener
            }
            lifecycleScope.launch { //ok
                try {
                    repository.login(email, password)
                    // Verificar si el usuario está bloqueado usando GET completo del usuario
                    val authUser = repository.getAuthMe()
                    val fullUser = UserRepository().getUser(authUser.id)
                    if (fullUser.bloqueo) {
                        // Cerrar sesión inmediatamente y mostrar error
                        repository.logout()
                        showCustomErrorToast(this@LoginActivity, getString(R.string.user_blocked))
                        return@launch
                    }

                    showCustomOkToast(this@LoginActivity, getString(R.string.login_success))
                    // Redirigir a MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    val baseMsg = getString(R.string.login_error_base)
                    if (e is HttpException) {
                        when (e.code()) {
                            403 -> { //error
                                showCustomErrorToast(this@LoginActivity, getString(R.string.login_invalid_credentials))
                            }
                            429 -> {
                                showCustomErrorToast(this@LoginActivity, getString(R.string.api_limit_error_retry))
                            }
                            else -> {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("LoginActivity", "HTTP ${e.code()} ${errBody ?: ""}")
                                showCustomErrorToast(this@LoginActivity, getString(R.string.http_error_format, baseMsg, e.code()))
                            }
                        }
                    } else {
                        Log.e("LoginActivity", e.localizedMessage ?: baseMsg)
                        showCustomErrorToast(this@LoginActivity, baseMsg)
                    }
                }
            }
        }

        linkSignup.setOnClickListener {
            startActivity(Intent(this, SignUpFragment::class.java))
        }
    }
}