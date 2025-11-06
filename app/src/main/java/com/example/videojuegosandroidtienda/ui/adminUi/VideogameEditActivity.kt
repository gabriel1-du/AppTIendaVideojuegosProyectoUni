package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VideogameEditActivity : AppCompatActivity() {
    private val repo = VideogameRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_videogame_edit)

        val videogameId = intent.getStringExtra("videogame_id").orEmpty()
        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editPrice = findViewById<EditText>(R.id.editPrice)
        val editDescription = findViewById<EditText>(R.id.editDescription)
        val editGenreId = findViewById<EditText>(R.id.editGenreId)
        val editPlatformId = findViewById<EditText>(R.id.editPlatformId)
        val buttonGuardar = findViewById<Button>(R.id.buttonGuardar)

        // Prefill con extras si vienen
        editTitle.setText(intent.getStringExtra("title") ?: "")
        val priceExtra = intent.getIntExtra("price", 0)
        if (priceExtra != 0) editPrice.setText(priceExtra.toString())
        editDescription.setText(intent.getStringExtra("description") ?: "")
        editGenreId.setText(intent.getStringExtra("genre_id") ?: "")
        editPlatformId.setText(intent.getStringExtra("platform_id") ?: "")

        buttonGuardar.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val priceText = editPrice.text.toString().trim()
            val description = editDescription.text.toString().trim().ifEmpty { null }
            val genreId = editGenreId.text.toString().trim().ifEmpty { null }
            val platformId = editPlatformId.text.toString().trim().ifEmpty { null }

            if (title.isEmpty()) {
                showCustomErrorToast(this, "El título es obligatorio")
                return@setOnClickListener
            }
            if (priceText.isEmpty()) {
                showCustomErrorToast(this, "El precio es obligatorio")
                return@setOnClickListener
            }
            val price = try { priceText.toInt() } catch (_: Exception) {
                showCustomErrorToast(this, "El precio debe ser un número entero")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    repo.updateVideogame(
                        id = videogameId,
                        title = title,
                        price = price,
                        description = description,
                        genreId = genreId,
                        platformId = platformId
                    )
                    showCustomOkToast(this@VideogameEditActivity, "Videojuego actualizado")
                    finish()
                } catch (e: HttpException) {
                    showCustomErrorToast(this@VideogameEditActivity, "Error al actualizar: ${e.message()}")
                } catch (e: Exception) {
                    showCustomErrorToast(this@VideogameEditActivity, "Error al actualizar: ${e.message}")
                }
            }
        }
    }
}