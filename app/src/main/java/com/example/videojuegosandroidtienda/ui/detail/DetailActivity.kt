package com.example.videojuegosandroidtienda.ui.detail

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.R
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.videojuegosandroidtienda.data.entities.CartProduct
import com.example.videojuegosandroidtienda.data.cart.CartManager
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.ui.Adapter.ImageUrlAdapter
import retrofit2.HttpException

class DetailActivity : AppCompatActivity() {
    // Muestra detalle del videojuego y permite agregar al carrito
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        val image = findViewById<ImageView>(R.id.detailImage)
        val imagesRecycler = findViewById<RecyclerView>(R.id.recyclerDetailImages)
        val title = findViewById<TextView>(R.id.detailTitle)
        val genre = findViewById<TextView>(R.id.detailGenre)
        val platform = findViewById<TextView>(R.id.detailPlatform)
        val price = findViewById<TextView>(R.id.detailPrice)
        val description = findViewById<TextView>(R.id.detailDescription)
        val buttonAdd = findViewById<Button>(R.id.buttonAddToCart)

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val id = intent.getStringExtra(EXTRA_ID).orEmpty()
        val titleText = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val genreText = intent.getStringExtra(EXTRA_GENRE_NAME).orEmpty()
        val platformText = intent.getStringExtra(EXTRA_PLATFORM_NAME).orEmpty()
        val priceValue = intent.getDoubleExtra(EXTRA_PRICE, 0.0)
        val descText = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()
        val extraImages: ArrayList<String>? = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS)

        if (!imageUrl.isNullOrBlank()) {
            val request = ImageRequest.Builder(this)
                .data(imageUrl)
                .target(image)
                .build()
            image.context.imageLoader.enqueue(request)
        } else {
            image.setImageResource(android.R.color.darker_gray)
        }

        title.text = titleText
        genre.text = "Género: $genreText"
        platform.text = "Plataforma: $platformText"
        price.text = "Precio: $priceValue"
        description.text = "Descripción: $descText"

        // Configurar galería horizontal si hay imágenes adicionales disponibles
        val urls = extraImages?.filter { !it.isNullOrBlank() } ?: emptyList()
        if (urls.isNotEmpty()) {
            imagesRecycler.visibility = android.view.View.VISIBLE
            imagesRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val imgAdapter = ImageUrlAdapter()
            imagesRecycler.adapter = imgAdapter
            imgAdapter.submit(urls)
        } else {
            imagesRecycler.visibility = android.view.View.GONE
            // Intentar cargar imágenes desde API por id si no se pasaron en el intent
            if (id.isNotBlank()) {
                val repo = VideogameRepository()
                lifecycleScope.launch {
                    try {
                        val vg = repo.getVideogameById(id)
                        val fetchedUrls = vg.images.mapNotNull { it.url }.filter { it.isNotBlank() }
                        if (fetchedUrls.isNotEmpty()) {
                            imagesRecycler.visibility = android.view.View.VISIBLE
                            imagesRecycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                            val imgAdapter = ImageUrlAdapter()
                            imagesRecycler.adapter = imgAdapter
                            imgAdapter.submit(fetchedUrls)
                        }
                    } catch (e: HttpException) {
                        if (e.code() == 429) {
                            showCustomErrorToast(this@DetailActivity, "Has alcanzado el límite de la API. Intenta nuevamente en unos segundos.")
                        }
                    } catch (_: Exception) {
                        // Silenciar otros errores para no afectar la UX del detalle
                    }
                }
            }
        }

        buttonAdd.setOnClickListener {
            val product = CartProduct(
                id = id,
                title = titleText,
                price = priceValue,
                imageUrl = imageUrl
            )
            CartManager.add(product, 1)
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.custom_toast_error, null)

            val textView = layout.findViewById<TextView>(R.id.toast_text)
            textView.text = "Este producto se agregó al carrito"

            with (Toast(applicationContext)) {
                duration = Toast.LENGTH_SHORT
                view = layout
                show()
            }
        }
    }

    // Claves de extras para intercambio de datos con detalle
    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_IMAGE_URLS = "extra_image_urls" // Lista de imágenes adicionales
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_GENRE_NAME = "extra_genre_name"
        const val EXTRA_PLATFORM_NAME = "extra_platform_name"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_DESCRIPTION = "extra_description"
    }
}