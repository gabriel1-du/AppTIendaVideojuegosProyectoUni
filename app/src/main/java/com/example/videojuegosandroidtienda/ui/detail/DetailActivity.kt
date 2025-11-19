package com.example.videojuegosandroidtienda.ui.detail

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.imageLoader
import android.util.Log
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.R
import android.widget.Button
import android.content.Intent
import com.example.videojuegosandroidtienda.ui.adminUi.VideogameEditActivity
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.videojuegosandroidtienda.data.entities.CartProduct
import com.example.videojuegosandroidtienda.data.cart.CartManager
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.ui.adapter.ImageUrlAdapter
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
        val buttonEdit = findViewById<Button>(R.id.buttonEditVideogame)
        val buttonDelete = findViewById<Button>(R.id.buttonDeleteVideogame)

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val id = intent.getStringExtra(EXTRA_ID).orEmpty().trim()
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
            // Hacer la portada principal clicable para verla en pantalla completa
            image.setOnClickListener {
                val fullIntent = Intent(this@DetailActivity, FullImageActivity::class.java)
                fullIntent.putExtra(FullImageActivity.EXTRA_IMAGE_URL, imageUrl)
                startActivity(fullIntent)
            }
        } else {
            image.setImageResource(android.R.color.darker_gray)
            image.setOnClickListener(null)
        }

        title.text = titleText
        genre.text = getString(R.string.detail_genre_format, genreText)
        platform.text = getString(R.string.detail_platform_format, platformText)
        price.text = getString(R.string.detail_price_format, priceValue)
        description.text = getString(R.string.detail_description_format, descText)

        // Configurar galería horizontal si hay imágenes adicionales disponibles
        val urls = extraImages?.filter { !it.isNullOrBlank() } ?: emptyList()
        if (urls.isNotEmpty()) {
            imagesRecycler.visibility = android.view.View.VISIBLE
            imagesRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val imgAdapter = ImageUrlAdapter(onImageClick = { clickedUrl ->
                val fullIntent = Intent(this@DetailActivity, FullImageActivity::class.java)
                fullIntent.putExtra(FullImageActivity.EXTRA_IMAGE_URL, clickedUrl)
                startActivity(fullIntent)
            })
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
                            val imgAdapter = ImageUrlAdapter(onImageClick = { clickedUrl ->
                                val fullIntent = Intent(this@DetailActivity, FullImageActivity::class.java)
                                fullIntent.putExtra(FullImageActivity.EXTRA_IMAGE_URL, clickedUrl)
                                startActivity(fullIntent)
                            })
                            imagesRecycler.adapter = imgAdapter
                            imgAdapter.submit(fetchedUrls)
                        }
                    } catch (e: HttpException) {
                        if (e.code() == 429) {
                            showCustomErrorToast(this@DetailActivity, getString(R.string.api_limit_error_retry))
                            lifecycleScope.launch {
                                delay(20000)
                                try {
                                    val vg2 = repo.getVideogameById(id)
                                    val urls2 = vg2.images.mapNotNull { it.url }.filter { it.isNotBlank() }
                                    if (urls2.isNotEmpty()) {
                                        imagesRecycler.visibility = android.view.View.VISIBLE
                                        imagesRecycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                                        val imgAdapter2 = ImageUrlAdapter(onImageClick = { clickedUrl ->
                                            val fullIntent = Intent(this@DetailActivity, FullImageActivity::class.java)
                                            fullIntent.putExtra(FullImageActivity.EXTRA_IMAGE_URL, clickedUrl)
                                            startActivity(fullIntent)
                                        })
                                        imagesRecycler.adapter = imgAdapter2
                                        imgAdapter2.submit(urls2)
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Silenciar otros errores para no afectar la UX del detalle
                    }
                }
            }
        }

        val isAdminMode = intent.getBooleanExtra("extra_admin_mode", false)
        if (isAdminMode) {
            buttonAdd.visibility = android.view.View.GONE
            buttonEdit.visibility = android.view.View.VISIBLE
            buttonDelete.visibility = android.view.View.VISIBLE

            buttonEdit.setOnClickListener {
                val editIntent = Intent(this@DetailActivity, VideogameEditActivity::class.java)
                editIntent.putExtra("videogame_id", id)
                editIntent.putExtra("title", titleText)
                editIntent.putExtra("price", priceValue.toInt())
                editIntent.putExtra("description", descText)
                // Preferir IDs crudos si se pasaron en el intent
                editIntent.putExtra("genre_id", intent.getStringExtra(EXTRA_GENRE_ID) ?: "")
                editIntent.putExtra("platform_id", intent.getStringExtra(EXTRA_PLATFORM_ID) ?: "")
                startActivity(editIntent)
            }

            buttonDelete.setOnClickListener {
                val repo = VideogameRepository()
                val deleteId = id.trim()
                if (deleteId.isBlank()) {
                    showCustomErrorToast(this@DetailActivity, "ID de videojuego inválido")
                    return@setOnClickListener
                }
                Log.d("VideogameDelete", "Deleting id=" + deleteId + ", URL=" + ApiConfig.STORE_BASE_URL + "videogame/" + deleteId)
                lifecycleScope.launch {
                    try {
                        repo.deleteVideogame(deleteId)
                        showCustomOkToast(this@DetailActivity, getString(R.string.videogame_deleted))
                        finish()
                    } catch (e: HttpException) {
                        if (e.code() == 429) {
                            showCustomErrorToast(this@DetailActivity, "Límite de API, intenta en unos segundos")
                        } else {
                            showCustomErrorToast(this@DetailActivity, "Error al eliminar: ${e.message()}")
                        }
                    } catch (e: Exception) {
                        showCustomErrorToast(this@DetailActivity, "Error al eliminar: ${e.message}")
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
            showCustomOkToast(this@DetailActivity, getString(R.string.product_added_to_cart))
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
        // IDs crudos para permitir edición sin ambigüedad
        const val EXTRA_GENRE_ID = "extra_genre_id"
        const val EXTRA_PLATFORM_ID = "extra_platform_id"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_DESCRIPTION = "extra_description"
    }
}