package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UserEditActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_edit)

        val editId = findViewById<EditText>(R.id.editUserId)
        val editCreated = findViewById<EditText>(R.id.editUserCreated)
        val editName = findViewById<EditText>(R.id.editUserName)
        val editEmail = findViewById<EditText>(R.id.editUserEmail)
        val switchAdmin = findViewById<Switch>(R.id.switchUserAdmin)
        val switchBloqueo = findViewById<Switch>(R.id.switchUserBloqueo)
        val buttonEditarConfirm = findViewById<Button>(R.id.buttonEditarConfirm)

        val userId = intent.getStringExtra("user_id")?.trim().orEmpty()

        lifecycleScope.launch {
            try {
                val user = userRepository.getUser(userId)
                editId.setText(user.id)
                editCreated.setText(user.created_at ?: "")
                editName.setText(user.name)
                editEmail.setText(user.email)
                switchAdmin.isChecked = user.admin
                switchBloqueo.isChecked = user.bloqueo
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    showCustomErrorToast(this@UserEditActivity, "Has alcanzado el límite de la API. Intenta nuevamente en unos segundos.")
                } else {
                    showCustomErrorToast(this@UserEditActivity, "No se pudo cargar el usuario (HTTP ${e.code()})")
                }
            } catch (e: Exception) {
                showCustomErrorToast(this@UserEditActivity, "No se pudo cargar el usuario")
            }
        }

        buttonEditarConfirm.setOnClickListener {
            val name = editName.text?.toString()?.trim().orEmpty()
            val email = editEmail.text?.toString()?.trim().orEmpty()
            val admin = switchAdmin.isChecked
            val bloqueo = switchBloqueo.isChecked
            val created = editCreated.text?.toString()

            // Restricciones alineadas con Signup: campos obligatorios
            if (name.isBlank() || email.isBlank()) {
                showCustomErrorToast(this@UserEditActivity, "Completa nombre y email")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val updated = User(
                        id = userId,
                        created_at = created,
                        name = name,
                        email = email,
                        password = null,
                        admin = admin,
                        bloqueo = bloqueo
                    )
                    userRepository.putUser(userId, updated)
                    showCustomOkToast(this@UserEditActivity, "Usuario editado correctamente")
                    finish()
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@UserEditActivity, "Límite de API alcanzado. Intenta nuevamente en unos segundos.")
                    } else {
                        showCustomErrorToast(this@UserEditActivity, "No se pudo editar (HTTP ${e.code()})")
                    }
                } catch (_: Exception) {
                    showCustomErrorToast(this@UserEditActivity, "No se pudo editar el usuario")
                }
            }
        }
    }
}