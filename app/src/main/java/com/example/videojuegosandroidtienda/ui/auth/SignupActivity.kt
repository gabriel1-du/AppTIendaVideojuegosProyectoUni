package com.example.videojuegosandroidtienda.ui.auth

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import kotlinx.coroutines.launch
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import com.example.videojuegosandroidtienda.MainActivity
import retrofit2.HttpException
import android.util.Log
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable

class SignupActivity : AppCompatActivity() {
    private val repository = AuthRepository()
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
                showCustomErrorToast(this@SignupActivity, getString(R.string.signup_fields_required))
                return@setOnClickListener
            }
            if (password.length < 8) { //error
                showCustomErrorToast(this@SignupActivity, getString(R.string.password_length_error))
                return@setOnClickListener
            }
            if (!password.any { it.isLetter() }) {
                showCustomErrorToast(this@SignupActivity, getString(R.string.password_letter_error))
                return@setOnClickListener
            }
            if (!password.any { !it.isLetterOrDigit() }) {
                showCustomErrorToast(this@SignupActivity, getString(R.string.password_special_char_error))
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    repository.register(name, email, password)
                    // Iniciar sesión automáticamente y redirigir a MainActivity
                    repository.login(email, password)

                    // 1. Crear y configurar el Dialog
                    val dialog = Dialog(this@SignupActivity)
                    dialog.setContentView(R.layout.custom_toast_ok) // Reutilizamos tu layout
                    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable()) // Para esquinas redondeadas si las tienes

                    val textView = dialog.findViewById<TextView>(R.id.toast_text)
                    textView.text = getString(R.string.signup_succes) // Asegúrate que se llame 'signup_success' en strings.xml si lo cambiaste

                    dialog.setCancelable(false) // El usuario no puede cerrarlo
                    dialog.show()

                    // 2. Usar un Handler para cerrar el diálogo y navegar después de un tiempo
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                        // Navegar a MainActivity
                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Cierra SignupActivity
                    }, 2000)
                } catch (e: Exception) {
                    val baseMsg = "Error al crear cuenta (Comunicar con soporte)"
                    if (e is HttpException) {
                        val code = e.code()
                        val errBody = e.response()?.errorBody()?.string()
                        Log.e("SignupActivity", "HTTP $code ${errBody ?: ""}")
                        val message = if (code == 429) {
                            getString(R.string.api_limit_error_retry)
                        } else {
                            getString(R.string.http_error_format, baseMsg, code)
                        }
                        showCustomErrorToast(this@SignupActivity, message)
                    } else {
                        Log.e("SignupActivity", e.localizedMessage ?: baseMsg)
                        showCustomErrorToast(this@SignupActivity, baseMsg)
                    }
                }
            }
        }
    }
}