package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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
    private var platforms: List<com.example.videojuegosandroidtienda.data.entities.Platform> = emptyList()
    private var genres: List<com.example.videojuegosandroidtienda.data.entities.Genre> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_videogame_edit)

        val videogameId = intent.getStringExtra("videogame_id").orEmpty()
        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editPrice = findViewById<EditText>(R.id.editPrice)
        val editDescription = findViewById<EditText>(R.id.editDescription)
        val spinnerGenre = findViewById<MaterialAutoCompleteTextView>(R.id.spinnerGenreEdit)
        val spinnerPlatform = findViewById<MaterialAutoCompleteTextView>(R.id.spinnerPlatformEdit)
        val buttonGuardar = findViewById<Button>(R.id.buttonGuardar)

        // Prefill con extras si vienen
        editTitle.setText(intent.getStringExtra("title") ?: "")
        val priceExtra = intent.getIntExtra("price", 0)
        if (priceExtra != 0) editPrice.setText(priceExtra.toString())
        editDescription.setText(intent.getStringExtra("description") ?: "")
        val preselectGenreId = intent.getStringExtra("genre_id")
        val preselectPlatformId = intent.getStringExtra("platform_id")

        lifecycleScope.launch {
            try {
                genres = repo.getGenres()
                platforms = repo.getPlatforms()

                val genreNames = genres.map { it.name }
                val platformNames = platforms.map { it.name }

                val genreAdapter = ArrayAdapter(this@VideogameEditActivity, android.R.layout.simple_dropdown_item_1line, genreNames)
                val platformAdapter = ArrayAdapter(this@VideogameEditActivity, android.R.layout.simple_dropdown_item_1line, platformNames)

                spinnerGenre.setAdapter(genreAdapter)
                spinnerPlatform.setAdapter(platformAdapter)

                // Preselección basada en IDs recibidos en el intent
                preselectGenreId?.let { id ->
                    val name = genres.firstOrNull { it.id == id }?.name
                    if (name != null) spinnerGenre.setText(name, false)
                }
                preselectPlatformId?.let { id ->
                    val name = platforms.firstOrNull { it.id == id }?.name
                    if (name != null) spinnerPlatform.setText(name, false)
                }
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    showCustomErrorToast(this@VideogameEditActivity, "Límite de API, intenta en unos segundos")
                } else {
                    showCustomErrorToast(this@VideogameEditActivity, "Error al cargar listas: ${e.message()}")
                }
            } catch (e: Exception) {
                showCustomErrorToast(this@VideogameEditActivity, "Error al cargar listas: ${e.message}")
            }
        }

        buttonGuardar.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val priceText = editPrice.text.toString().trim()
            val description = editDescription.text.toString().trim().ifEmpty { null }
            val selectedGenreName = spinnerGenre.text?.toString()?.trim().orEmpty()
            val selectedPlatformName = spinnerPlatform.text?.toString()?.trim().orEmpty()
            val genreId = genres.firstOrNull { it.name == selectedGenreName }?.id
            val platformId = platforms.firstOrNull { it.name == selectedPlatformName }?.id

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