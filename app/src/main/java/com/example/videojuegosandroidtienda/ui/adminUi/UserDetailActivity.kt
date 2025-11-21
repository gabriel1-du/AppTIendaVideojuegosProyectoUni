package com.example.videojuegosandroidtienda.ui.adminUi

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
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
        val switchBloqueo = findViewById<MaterialSwitch>(R.id.switchUserBloqueo)
        val switchAdmin = findViewById<MaterialSwitch>(R.id.switchUserAdmin)
        val buttonEliminar = findViewById<MaterialButton>(R.id.buttonEliminar)
        val buttonEditar = findViewById<MaterialButton>(R.id.buttonEditar)

        val userId = intent.getStringExtra("user_id")?.trim().orEmpty()

        if (userId.isBlank()) {
            showCustomErrorToast(this, "ID de usuario no válido")
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val user = userRepository.getUser(userId)
                textId.text = getString(R.string.user_id_format, user.id)
                textCreated.text = getString(R.string.user_created_format, user.created_at ?: "")
                textName.text = getString(R.string.user_name_format, user.name)
                textEmail.text = getString(R.string.user_email_format, user.email)
                textAdmin.text = getString(
                    R.string.user_admin_format,
                    getString(if (user.admin) R.string.boolean_true else R.string.boolean_false)
                )
                switchAdmin.isChecked = user.admin
                switchBloqueo.isChecked = user.bloqueo
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    showCustomErrorToast(this@UserDetailActivity, getString(R.string.api_limit_error_retry))
                } else {
                    showCustomErrorToast(
                        this@UserDetailActivity,
                        getString(R.string.http_error_format, getString(R.string.user_load_error), e.code())
                    )
                }
                finish()
            } catch (e: Exception) {
                val msg = e.message?.let { " (${it})" } ?: ""
                showCustomErrorToast(this@UserDetailActivity, getString(R.string.user_load_error) + msg)
                finish()
            }
        }

        buttonEliminar.setOnClickListener {
            lifecycleScope.launch {
                try {
                    userRepository.deleteUser(userId)
                    showCustomOkToast(this@UserDetailActivity, getString(R.string.user_deleted_success))
                    finish()
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@UserDetailActivity, getString(R.string.api_limit_error_retry))
                    } else {
                        showCustomErrorToast(
                            this@UserDetailActivity,
                            getString(R.string.http_error_format, getString(R.string.delete_error_base), e.code())
                        )
                    }
                } catch (e: Exception) {
                    val msg = e.message?.let { " (${it})" } ?: ""
                    showCustomErrorToast(this@UserDetailActivity, getString(R.string.user_delete_error) + msg)
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
