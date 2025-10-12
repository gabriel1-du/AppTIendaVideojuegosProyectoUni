package com.example.videojuegosandroidtienda.ui.upload



import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import android.content.Intent
import com.example.videojuegosandroidtienda.data.network.TokenStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.content.ClipboardManager
import android.content.ClipData
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast

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

        val inputTitle = findViewById<EditText>(R.id.inputTitle) //Ingreso del titulo
        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
        val spinnerPlatform = findViewById<Spinner>(R.id.spinnerPlatform) //Ingreso de plataforma
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre) //Ingreso de genero
        val inputPrice = findViewById<EditText>(R.id.inputPrice) //ingreso del precio
        val inputDescription = findViewById<EditText>(R.id.inputDescription) //ingreso de la descripcion
        val buttonSelect = findViewById<Button>(R.id.buttonSelectImage) //seleccion de imafen
        val buttonUpload = findViewById<Button>(R.id.buttonUpload) //Boton para subir (Submit)

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
            pickImage.launch("image/*") //buscar imageens en el dispositivo
        }

        lifecycleScope.launch {
            try {
                platforms = repository.getPlatforms()
                genres = repository.getGenres()
                val platformNames = platforms.map { it.name }
                val genreNames = genres.map { it.name }
                spinnerPlatform.adapter = ArrayAdapter( //traer desde la bda
                    this@AddVideogameActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    platformNames
                )
                spinnerGenre.adapter = ArrayAdapter( //traer desde la bda
                    this@AddVideogameActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    genreNames
                )
            } catch (e: Exception) {
                showCustomErrorToast(this@AddVideogameActivity, "Error al cargar listas: ${e.message}, comunicar con soporte")
            }
        }

        buttonUpload.setOnClickListener {
            val token = TokenStore.token
            if (token.isNullOrBlank()) {
                showCustomErrorToast(this,"Debes iniciar sesión para subir un videojuego")
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
                showCustomErrorToast(this, "Por favor, ingresa el nombre del videojuego")
                return@setOnClickListener
            }
            if (uri == null) {
                showCustomErrorToast(this,"Selecciona una portada para el videojuego")
                return@setOnClickListener
            }
            if (platformPos !in platforms.indices) {
                showCustomErrorToast(this,"Selecciona una plataforma")
                return@setOnClickListener
            }
            if (genrePos !in genres.indices) {
                showCustomErrorToast(this,"Selecciona un género")
                return@setOnClickListener
            }
            if (priceText.isBlank()) {
                showCustomErrorToast(this,"Ingresa el precio")
                return@setOnClickListener
            }
            val priceInt = priceText.toIntOrNull()
            if (priceInt == null || priceInt <= 0) {
                showCustomErrorToast(this,"Precio inválido (usa enteros)")
                return@setOnClickListener
            }

            lifecycleScope.launch { //carga
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
                    showCustomOkToast(this@AddVideogameActivity, "Videojuego subido exitosamente")
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
                    showCustomErrorToast(this@AddVideogameActivity,"Error al subir: $msg")
                }
            }
        }
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
