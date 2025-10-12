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
import com.example.videojuegosandroidtienda.MainActivity
import retrofit2.HttpException
import android.util.Log
import android.widget.TextView

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
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.custom_toast_error, null)
                val textView = layout.findViewById<TextView>(R.id.toast_text)
                textView.text = "Completa nombre, email y contraseña"
                with(Toast(applicationContext)) {
                    duration = Toast.LENGTH_SHORT
                    view = layout
                    show()
                }
                return@setOnClickListener
            }
            if (password.length < 8) { //error
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.custom_toast_error, null)
                val textView = layout.findViewById<TextView>(R.id.toast_text)
                textView.text = "La contraseña debe tener mas de 8 caracteres"
                with(Toast(applicationContext)) {
                    duration = Toast.LENGTH_SHORT
                    view = layout
                    show()
                }
                return@setOnClickListener
            }
            if (!password.any { it.isLetter() }) {
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.custom_toast_error, null)
                val textView = layout.findViewById<TextView>(R.id.toast_text)
                textView.text = "La contraseña debe contener al menos un carácter"
                with(Toast(applicationContext)) {
                    duration = Toast.LENGTH_SHORT
                    view = layout
                    show()
                }
                return@setOnClickListener
            }
            if (!password.any { !it.isLetterOrDigit() }) {
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.custom_toast_error, null)
                val textView = layout.findViewById<TextView>(R.id.toast_text)
                textView.text = "La contraseña debe contener al menos un carácter especial"
                with(Toast(applicationContext)) {
                    duration = Toast.LENGTH_SHORT
                    view = layout
                    show()
                }
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
                        textView.text = "$baseMsg (HTTP $code)"
                        with(Toast(applicationContext)) {
                            duration = Toast.LENGTH_SHORT
                            view = layout
                            show()
                        }
                    } else {
                        Log.e("SignupActivity", e.localizedMessage ?: baseMsg)
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
    }
}