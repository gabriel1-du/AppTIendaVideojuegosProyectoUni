package com.example.videojuegosandroidtienda.ui.auth

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
import com.example.videojuegosandroidtienda.MainActivity
import retrofit2.HttpException
import android.util.Log
import android.widget.TextView
import android.widget.Toast

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
                    val inflater = layoutInflater
                    val layout = inflater.inflate(R.layout.custom_toast_ok, null)
                    val textView = layout.findViewById<TextView>(R.id.toast_text)
                    textView.text = "Registro exitoso"
                    with(Toast(applicationContext)) {
                        duration = Toast.LENGTH_SHORT
                        view = layout
                        show()
                    }
                    val intent = Intent(this@SignupActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    val baseMsg = "Error al crear cuenta (Comunicar con soporte)"
                    if (e is HttpException) {
                        val code = e.code()
                        val errBody = e.response()?.errorBody()?.string()
                        Log.e("SignupActivity", "HTTP $code ${errBody ?: ""}")
                        val inflater = layoutInflater
                        val layout = inflater.inflate(R.layout.custom_toast_error, null)
                        val textView = layout.findViewById<TextView>(R.id.toast_text)
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