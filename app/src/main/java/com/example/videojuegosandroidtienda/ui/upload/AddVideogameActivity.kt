package com.example.videojuegosandroidtienda.ui.upload

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Genre
import com.example.videojuegosandroidtienda.data.entities.Platform
import com.example.videojuegosandroidtienda.data.entities.VideogamePost2
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.UUID

class AddVideogameActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AddVideogameActivity"
        private const val MAX_ADDITIONAL_IMAGES = 4
    }

    private val videogameRepository = VideogameRepository()

    private var coverImageUri: Uri? = null
    private val additionalImageUris = mutableListOf<Uri>()

    private var platforms: List<Platform> = emptyList()
    private var genres: List<Genre> = emptyList()

    private lateinit var imagePreview: ImageView
    private lateinit var additionalImagesPreviewContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_videogame)

        val inputTitle = findViewById<EditText>(R.id.inputTitle)
        imagePreview = findViewById<ImageView>(R.id.imagePreview)
        additionalImagesPreviewContainer = findViewById<LinearLayout>(R.id.additionalImagesPreviewContainer)
        val spinnerPlatform = findViewById<Spinner>(R.id.spinnerPlatform)
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)
        val inputPrice = findViewById<EditText>(R.id.inputPrice)
        val inputDescription = findViewById<EditText>(R.id.inputDescription)
        val buttonSelectCover = findViewById<Button>(R.id.buttonSelectImage)
        val buttonSelectAdditional = findViewById<Button>(R.id.buttonSelectAdditionalImages)
        val buttonUpload = findViewById<Button>(R.id.buttonUpload)

        val pickCoverImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            coverImageUri = uri
            if (uri != null) {
                val request = ImageRequest.Builder(this).data(uri).target(imagePreview).build()
                imagePreview.context.imageLoader.enqueue(request)
            } else {
                imagePreview.setImageResource(android.R.color.darker_gray)
            }
        }

        val pickAdditionalImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            additionalImageUris.clear()
            additionalImageUris.addAll(uris.take(MAX_ADDITIONAL_IMAGES))
            updateAdditionalImagesPreview()
        }

        buttonSelectCover.setOnClickListener { pickCoverImage.launch("image/*") }

        buttonSelectAdditional.setOnClickListener { pickAdditionalImages.launch("image/*") }

        loadSpinnersData(spinnerPlatform, spinnerGenre)

        buttonUpload.setOnClickListener {
            if (!validateInputs(inputTitle, inputPrice, spinnerPlatform, spinnerGenre)) return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val title = inputTitle.text.toString().trim()
                    val price = inputPrice.text.toString().toInt()
                    val description = inputDescription.text.toString().trim()
                    val platformId = platforms[spinnerPlatform.selectedItemPosition].id
                    val genreId = genres[spinnerGenre.selectedItemPosition].id

                    val coverImagePart = buildImagePartFromUri(coverImageUri!!, "cover.jpg")
                    val coverImageResponse = videogameRepository.uploadCoverImage(coverImagePart)

                    val additionalImageParts = additionalImageUris.mapIndexed { index, uri ->
                        buildImagePartFromUri(uri, "image_$index.jpg")
                    }
                    val additionalImagesResponse = videogameRepository.uploadImages(additionalImageParts)

                    val videogame = VideogamePost2(
                        id = null, // dejar que el backend genere el ID
                        created_at = null, // dejar que el backend genere el timestamp
                        title = title,
                        price = price,
                        description = description,
                        cover_image = coverImageResponse, // Se pasa el objeto UploadResponse directamente
                        genre_id = genreId,
                        platform_id = platformId,
                        images = additionalImagesResponse // Se pasa la lista de UploadResponse directamente
                    )

                    videogameRepository.createVideogame(videogame)
                    showCustomOkToast(this@AddVideogameActivity, "Videojuego subido exitosamente")
                    finish()

                } catch (e: Exception) {
                    showCustomErrorToast(this@AddVideogameActivity, "Error al subir: ${e.message}")
                }
            }
        }
    }

    private fun loadSpinnersData(spinnerPlatform: Spinner, spinnerGenre: Spinner) {
        lifecycleScope.launch {
            try {
                platforms = videogameRepository.getPlatforms()
                genres = videogameRepository.getGenres()
                spinnerPlatform.adapter = ArrayAdapter(this@AddVideogameActivity, android.R.layout.simple_spinner_dropdown_item, platforms.map { it.name })
                spinnerGenre.adapter = ArrayAdapter(this@AddVideogameActivity, android.R.layout.simple_spinner_dropdown_item, genres.map { it.name })
            } catch (e: Exception) {
                showCustomErrorToast(this@AddVideogameActivity, "Error al cargar listas: ${e.message}")
            }
        }
    }

    private fun validateInputs(inputTitle: EditText, inputPrice: EditText, spinnerPlatform: Spinner, spinnerGenre: Spinner): Boolean {
        if (inputTitle.text.isBlank()) {
            showCustomErrorToast(this, "Ingresa el nombre del videojuego")
            return false
        }
        if (coverImageUri == null) {
            showCustomErrorToast(this, "Selecciona una portada")
            return false
        }
        if (inputPrice.text.toString().toIntOrNull() == null) {
            showCustomErrorToast(this, "Ingresa un precio válido")
            return false
        }
        if (spinnerPlatform.selectedItemPosition !in platforms.indices) {
            showCustomErrorToast(this, "Selecciona una plataforma")
            return false
        }
        if (spinnerGenre.selectedItemPosition !in genres.indices) {
            showCustomErrorToast(this, "Selecciona un género")
            return false
        }
        return true
    }

    private fun updateAdditionalImagesPreview() {
        additionalImagesPreviewContainer.removeAllViews()
        for (uri in additionalImageUris) {
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(250, 250).apply {
                    marginEnd = 16
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            val request = ImageRequest.Builder(this).data(uri).target(imageView).build()
            imageLoader.enqueue(request)
            additionalImagesPreviewContainer.addView(imageView)
        }
    }

    private fun buildImagePartFromUri(uri: Uri, defaultFileName: String): MultipartBody.Part {
        val fileName = queryDisplayName(uri) ?: defaultFileName
        val mediaType = contentResolver.getType(uri)?.toMediaType() ?: "image/jpeg".toMediaType()
        val requestBody = object : RequestBody() {
            override fun contentType() = mediaType
            override fun writeTo(sink: okio.BufferedSink) {
                contentResolver.openInputStream(uri)?.use { it.copyTo(sink.outputStream()) }
            }
        }
        return MultipartBody.Part.createFormData("content", fileName, requestBody)
    }

    private fun queryDisplayName(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }
}
