package com.example.videojuegosandroidtienda.ui.upload


import okio.buffer
import okio.source
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.repository.StoreRepository
import com.example.videojuegosandroidtienda.data.entities.Platform
import com.example.videojuegosandroidtienda.data.entities.Genre
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.Intent
import com.example.videojuegosandroidtienda.data.network.TokenStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.content.ClipboardManager
import android.content.ClipData

class AddVideogameActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AddVideogameActivity"
    }
    private val repository = StoreRepository()
    private var selectedImageUri: Uri? = null
    private var platforms: List<Platform> = emptyList()
    private var genres: List<Genre> = emptyList()
    // Pantalla de subida: nombre y portada (multipart)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_videogame)

        val inputTitle = findViewById<EditText>(R.id.inputTitle)
        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
        val spinnerPlatform = findViewById<Spinner>(R.id.spinnerPlatform)
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)
        val inputPrice = findViewById<EditText>(R.id.inputPrice)
        val inputDescription = findViewById<EditText>(R.id.inputDescription)
        val buttonSelect = findViewById<Button>(R.id.buttonSelectImage)
        val buttonUpload = findViewById<Button>(R.id.buttonUpload)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedImageUri = uri
            if (uri != null) {
                val request = ImageRequest.Builder(this)
                    .data(uri)
                    .target(imagePreview)
                    .build()
                imagePreview.context.imageLoader.enqueue(request)
            } else {
                imagePreview.setImageResource(android.R.color.darker_gray)
            }
        }

        buttonSelect.setOnClickListener {
            pickImage.launch("image/*")
        }

        lifecycleScope.launch {
            try {
                platforms = repository.getPlatforms()
                genres = repository.getGenres()
                val platformNames = platforms.map { it.name }
                val genreNames = genres.map { it.name }
                spinnerPlatform.adapter = ArrayAdapter(
                    this@AddVideogameActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    platformNames
                )
                spinnerGenre.adapter = ArrayAdapter(
                    this@AddVideogameActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    genreNames
                )
            } catch (e: Exception) {
                Toast.makeText(this@AddVideogameActivity, "Error al cargar listas: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        buttonUpload.setOnClickListener {
            val token = TokenStore.token
            if (token.isNullOrBlank()) {
                Toast.makeText(this, "Debes iniciar sesión para subir", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.auth.LoginActivity::class.java))
                return@setOnClickListener
            }
            val title = inputTitle.text?.toString()?.trim().orEmpty()
            val uri = selectedImageUri
            val platformPos = spinnerPlatform.selectedItemPosition
            val genrePos = spinnerGenre.selectedItemPosition
            val priceText = inputPrice.text?.toString()?.trim().orEmpty()
            val description = inputDescription.text?.toString()?.trim()

            if (title.isBlank()) {
                Toast.makeText(this, "Por favor, ingresa el nombre del videojuego", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (uri == null) {
                Toast.makeText(this, "Selecciona una portada para el videojuego", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (platformPos !in platforms.indices) {
                Toast.makeText(this, "Selecciona una plataforma", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (genrePos !in genres.indices) {
                Toast.makeText(this, "Selecciona un género", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (priceText.isBlank()) {
                Toast.makeText(this, "Ingresa el precio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val priceInt = priceText.toIntOrNull()
            if (priceInt == null || priceInt <= 0) {
                Toast.makeText(this, "Precio inválido (usa enteros)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val imagePart = buildImagePartFromUri(uri)
                    val platformId = platforms[platformPos].id
                    val genreId = genres[genrePos].id
                    // Log de parámetros antes de enviar
                    val mime = contentResolver.getType(uri)
                    val fileName = queryDisplayName(uri) ?: "(desconocido)"
                    Log.d(TAG, "Subiendo videojuego -> title=$title, platform_id=$platformId, genre_id=$genreId, price=$priceInt, descriptionLen=${description?.length ?: 0}, cover_image(name=$fileName, mime=$mime)")
                    val cover = repository.uploadCoverImage(imagePart)
                    Log.d(TAG, "Upload OK, cover_image.path=${cover.path}, name=${cover.name}, mime=${cover.mime}")
                    repository.createVideogameJson(
                        title = title,
                        platformId = platformId,
                        genreId = genreId,
                        price = priceInt,
                        description = description,
                        cover = cover,
                        overrideName = fileName,
                        overrideMime = mime
                    )
                    Log.i(TAG, "Subida exitosa del videojuego: title=$title")
                    Toast.makeText(this@AddVideogameActivity, "Videojuego subido correctamente", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: Exception) {
                    val msg = when (e) {
                        is retrofit2.HttpException -> {
                            val code = e.code()
                            val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                            Log.e(TAG, "Error HTTP al subir (code=$code): ${body ?: e.message()}", e)
                            "HTTP $code: ${body ?: e.message()}"
                        }
                        else -> {
                            Log.e(TAG, "Error al subir (no HTTP): ${e.message}", e)
                            e.message ?: "Error desconocido"
                        }
                    }
                    showErrorDialog("Error al subir: $msg")
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Error al subir")
            .setMessage(message)
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Copiar") { _, _ ->
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Error", message))
                Toast.makeText(this, "Error copiado al portapapeles", Toast.LENGTH_SHORT).show()
            }
            .create()
        dialog.show()
    }

    private fun buildImagePartFromUri(uri: Uri): MultipartBody.Part {
        val contentResolver = applicationContext.contentResolver
        val fileName = queryDisplayName(uri) ?: "cover.jpg"
        val detected = contentResolver.getType(uri)
        val mediaType = (detected ?: "application/octet-stream").toMediaType()
        val requestBody: RequestBody = object : RequestBody() {
            override fun contentType() = mediaType
            override fun writeTo(sink: okio.BufferedSink) {
                val input = contentResolver.openInputStream(uri) ?: throw IllegalStateException("No se pudo abrir la imagen")
                input.use { stream ->
                    val out = sink.outputStream()
                    stream.copyTo(out)
                    out.flush()
                }
            }
        }
        Log.d(TAG, "Construido Multipart file: name=$fileName, mime=${mediaType}")
        return MultipartBody.Part.createFormData("content", fileName, requestBody)
    }

    private fun queryDisplayName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                return it.getString(nameIndex)
            }
        }
        return null
    }
}

