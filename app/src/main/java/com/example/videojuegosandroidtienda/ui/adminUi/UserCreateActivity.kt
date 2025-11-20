package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UserCreateActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_create)

        val editName = findViewById<EditText>(R.id.editName)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val switchAdmin = findViewById<MaterialSwitch>(R.id.switchAdmin)
        val buttonCrear = findViewById<MaterialButton>(R.id.buttonCrearUsuarioConfirm)

        buttonCrear.setOnClickListener {
            val name = editName.text?.toString()?.trim().orEmpty()
            val email = editEmail.text?.toString()?.trim().orEmpty()
            val password = editPassword.text?.toString()?.trim().orEmpty()
            val admin = switchAdmin.isChecked

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                showCustomErrorToast(this@UserCreateActivity, "Completa nombre, email y contraseña")
                return@setOnClickListener
            }
            if (password.length < 8) {
                showCustomErrorToast(this@UserCreateActivity, "La contraseña debe tener al menos 8 caracteres")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val req = User(
                        id = "", // backend genera
                        created_at = null,
                        name = name,
                        email = email,
                        password = password,
                        admin = admin,
                        bloqueo = false
                    )
                    userRepository.createUser(req)
                    showCustomOkToast(this@UserCreateActivity, "Usuario creado exitosamente")
                    finish()
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@UserCreateActivity, "Límite de API alcanzado. Intenta nuevamente en unos segundos.")
                    } else {
                        showCustomErrorToast(this@UserCreateActivity, "No se pudo crear (HTTP ${e.code()})")
                    }
                } catch (_: Exception) {
                    showCustomErrorToast(this@UserCreateActivity, "No se pudo crear el usuario")
                }
            }
        }
    }
}