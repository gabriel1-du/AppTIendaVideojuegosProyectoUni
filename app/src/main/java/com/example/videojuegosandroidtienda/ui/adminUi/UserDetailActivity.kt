package com.example.videojuegosandroidtienda.ui.adminUi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UserDetailActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_detail)

        val textId = findViewById<TextView>(R.id.textUserId)
        val textCreated = findViewById<TextView>(R.id.textUserCreated)
        val textName = findViewById<TextView>(R.id.textUserName)
        val textEmail = findViewById<TextView>(R.id.textUserEmail)
        val textAdmin = findViewById<TextView>(R.id.textUserAdmin)
        val switchBloqueo = findViewById<android.widget.Switch>(R.id.switchUserBloqueo)
        val buttonActualizarBloqueo = findViewById<Button>(R.id.buttonActualizarBloqueo)
        val buttonEliminar = findViewById<Button>(R.id.buttonEliminar)
        val buttonEditar = findViewById<Button>(R.id.buttonEditar)

        val userId = intent.getStringExtra("user_id")?.trim().orEmpty()

        lifecycleScope.launch {
            try {
                val user = userRepository.getUser(userId)
                textId.text = "id: ${user.id}"
                textCreated.text = "creado: ${user.created_at ?: ""}"
                textName.text = "nombre: ${user.name}"
                textEmail.text = "email: ${user.email}"
                textAdmin.text = "admin: ${if (user.admin) "true" else "false"}"
                switchBloqueo.isChecked = user.bloqueo
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    showCustomErrorToast(this@UserDetailActivity, "Has alcanzado el límite de la API. Intenta nuevamente en unos segundos.")
                } else {
                    showCustomErrorToast(this@UserDetailActivity, "No se pudo cargar el usuario (HTTP ${e.code()})")
                }
            } catch (e: Exception) {
                showCustomErrorToast(this@UserDetailActivity, "No se pudo cargar el usuario")
            }
        }

        buttonActualizarBloqueo.setOnClickListener {
            lifecycleScope.launch {
                try {
                    userRepository.patchUser(userId, mapOf("bloqueo" to switchBloqueo.isChecked))
                    showCustomOkToast(this@UserDetailActivity, "Estado de bloqueo actualizado")
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@UserDetailActivity, "Límite de API alcanzado. Intenta nuevamente en unos segundos.")
                    } else {
                        showCustomErrorToast(this@UserDetailActivity, "No se pudo actualizar (HTTP ${e.code()})")
                    }
                } catch (_: Exception) {
                    showCustomErrorToast(this@UserDetailActivity, "No se pudo actualizar el bloqueo")
                }
            }
        }

        buttonEliminar.setOnClickListener {
            lifecycleScope.launch {
                try {
                    userRepository.deleteUser(userId)
                    showCustomOkToast(this@UserDetailActivity, "Se ha eliminado exitosamente el usuario")
                    finish()
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@UserDetailActivity, "Límite de API alcanzado. Intenta nuevamente en unos segundos.")
                    } else {
                        showCustomErrorToast(this@UserDetailActivity, "No se pudo eliminar (HTTP ${e.code()})")
                    }
                } catch (_: Exception) {
                    showCustomErrorToast(this@UserDetailActivity, "No se pudo eliminar el usuario")
                }
            }
        }

        buttonEditar.setOnClickListener {
            val intent = Intent(this@UserDetailActivity, UserEditActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }
    }
}