package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videojuegosandroidtienda.R
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.ui.Adapter.VideogameAdapter
import java.text.DateFormat
import java.util.Date

class CartDetailActivity : AppCompatActivity() {
    private lateinit var textViewIdCarrito: TextView
    private lateinit var textViewFechaCarrito: TextView
    private lateinit var textViewTotalCarrito: TextView
    private lateinit var textViewUserId: TextView
    private lateinit var textViewEstado: TextView
    private lateinit var recyclerViewVideogames: RecyclerView

    private val cartRepository = CartRepository()
    private val videogameRepository = VideogameRepository()
    private lateinit var videogameAdapter: VideogameAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Inicializar variables de vista
        textViewIdCarrito = findViewById(R.id.textViewIdCarrito)
        textViewFechaCarrito = findViewById(R.id.textViewFechaCarrito)
        textViewTotalCarrito = findViewById(R.id.textViewTotalCArrito)
        textViewUserId = findViewById(R.id.textViewUserId)
        textViewEstado = findViewById(R.id.textViewEstado)
        recyclerViewVideogames = findViewById(R.id.recyclerView2)
        videogameAdapter = VideogameAdapter()
        recyclerViewVideogames.adapter = videogameAdapter
        recyclerViewVideogames.layoutManager = LinearLayoutManager(this)

        val buttonApruebo = findViewById<Button>(R.id.buttonApruebo)
        val buttonRechazo = findViewById<Button>(R.id.buttonRechazo)

        // Obtener cart_id del intent y cargar datos
        val cartId = intent.getStringExtra("cart_id")?.trim()
        if (cartId.isNullOrBlank()) {
            showCustomErrorToast(this, "No se pudo obtener el ID del carrito")
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val cart = cartRepository.getCartById(cartId)

                // Mostrar datos del carrito con etiquetas descriptivas
                textViewIdCarrito.text = "Id del carrito: ${cart.id}"
                val formattedDate = try {
                    DateFormat.getDateTimeInstance().format(Date(cart.created_at))
                } catch (_: Exception) { cart.created_at.toString() }
                textViewFechaCarrito.text = "Fecha de creación: ${formattedDate}"
                textViewTotalCarrito.text = "Total del carrito: ${cart.total}"
                textViewUserId.text = "Id del usuario: ${cart.user_id}"
                textViewEstado.text = "Estado: ${if (cart.aprobado) "Aprobado" else "Pendiente"}"

                // Cargar listas auxiliares para nombres
                val platforms = videogameRepository.getPlatforms()
                val genres = videogameRepository.getGenres()
                val platformNames = platforms.associate { it.id to it.name }
                val genreNames = genres.associate { it.id to it.name }

                // Cargar videojuegos y filtrar por IDs del carrito
                val allVideogames = videogameRepository.getVideogames()
                val ids = cart.videogames_id ?: emptyList()
                val selected = allVideogames.filter { vg -> ids.contains(vg.id) }
                videogameAdapter.submit(selected, genreNames, platformNames)
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    showCustomErrorToast(this@CartDetailActivity, "Límite de API alcanzado. Espera ~20s e intenta de nuevo")
                } else {
                    showCustomErrorToast(this@CartDetailActivity, "Error HTTP ${e.code()}")
                }
            } catch (e: Exception) {
                showCustomErrorToast(this@CartDetailActivity, "Error al cargar carrito: ${e.message}")
            }
        }

        buttonApruebo.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val updated = cartRepository.updateCartApproval(cartId, true)
                    textViewEstado.text = "Estado: ${if (updated.aprobado) "Aprobado" else "Pendiente"}"
                    showCustomOkToast(this@CartDetailActivity, "Carrito aprobado correctamente")
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@CartDetailActivity, "Límite de API alcanzado. Espera ~20s e intenta de nuevo")
                    } else {
                        showCustomErrorToast(this@CartDetailActivity, "Error HTTP ${e.code()}")
                    }
                } catch (e: Exception) {
                    showCustomErrorToast(this@CartDetailActivity, "Error al aprobar: ${e.message}")
                }
            }
        }

        buttonRechazo.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val updated = cartRepository.updateCartApproval(cartId, false)
                    textViewEstado.text = "Estado: ${if (updated.aprobado) "Aprobado" else "Pendiente"}"
                    showCustomOkToast(this@CartDetailActivity, "Carrito marcado como pendiente/rechazado")
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@CartDetailActivity, "Límite de API alcanzado. Espera ~20s e intenta de nuevo")
                    } else {
                        showCustomErrorToast(this@CartDetailActivity, "Error HTTP ${e.code()}")
                    }
                } catch (e: Exception) {
                    showCustomErrorToast(this@CartDetailActivity, "Error al rechazar: ${e.message}")
                }
            }
        }
    }
}