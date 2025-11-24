package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videojuegosandroidtienda.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.ui.adapter.VideogameAdapter
import com.google.android.material.button.MaterialButton
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

        val buttonApruebo = findViewById<MaterialButton>(R.id.buttonApruebo)
        val buttonRechazo = findViewById<MaterialButton>(R.id.buttonRechazo)
        val buttonEliminar = findViewById<MaterialButton>(R.id.buttonEliminarCarrito)

        // Obtener cart_id del intent y cargar datos
        val cartId = intent.getStringExtra("cart_id")?.trim()
        if (cartId.isNullOrBlank()) {
            showCustomErrorToast(this, getString(R.string.cart_id_missing_error))
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val cart = cartRepository.getCartById(cartId)

                // Mostrar datos del carrito con etiquetas descriptivas
                textViewIdCarrito.text = getString(R.string.cart_id_format, cart.id)
                val formattedDate = try {
                    DateFormat.getDateTimeInstance().format(Date(cart.created_at))
                } catch (_: Exception) { cart.created_at.toString() }
                textViewFechaCarrito.text = getString(R.string.cart_created_format, formattedDate)
                textViewTotalCarrito.text = getString(R.string.cart_total_format, cart.total)
                // Mostrar nombre del usuario en vez de ID
                try {
                    val user = UserRepository().getUser(cart.user_id)
                    textViewUserId.text = getString(R.string.cart_user_name_format, user.name)
                } catch (_: Exception) {
                    textViewUserId.text = getString(R.string.cart_user_id_format, cart.user_id)
                }
                textViewEstado.text = getString(
                    R.string.cart_status_format,
                    getString(if (cart.aprobado) R.string.status_approved else R.string.status_pending)
                )

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
                    showCustomErrorToast(this@CartDetailActivity, getString(R.string.api_limit_error_retry))
                } else {
                    showCustomErrorToast(
                        this@CartDetailActivity,
                        getString(R.string.http_error_format, getString(R.string.cart_load_error), e.code())
                    )
                }
            } catch (e: Exception) {
                val msg = e.message?.let { " (${it})" } ?: ""
                showCustomErrorToast(this@CartDetailActivity, getString(R.string.cart_load_error) + msg)
            }
        }

        buttonApruebo.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val updated = cartRepository.updateCartApproval(cartId, true)
                    textViewEstado.text = getString(
                        R.string.cart_status_format,
                        getString(if (updated.aprobado) R.string.status_approved else R.string.status_pending)
                    )
                    showCustomOkToast(this@CartDetailActivity, getString(R.string.cart_approved_success))
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@CartDetailActivity, getString(R.string.api_limit_error_retry))
                    } else {
                        showCustomErrorToast(
                            this@CartDetailActivity,
                            getString(R.string.http_error_format, getString(R.string.approve_error_base), e.code())
                        )
                    }
                } catch (e: Exception) {
                    val msg = e.message?.let { " (${it})" } ?: ""
                    showCustomErrorToast(this@CartDetailActivity, getString(R.string.approve_error_base) + msg)
                }
            }
        }

        buttonRechazo.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val updated = cartRepository.updateCartApproval(cartId, false)
                    textViewEstado.text = getString(
                        R.string.cart_status_format,
                        getString(if (updated.aprobado) R.string.status_approved else R.string.status_pending)
                    )
                    showCustomOkToast(this@CartDetailActivity, getString(R.string.cart_marked_pending_success))
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@CartDetailActivity, getString(R.string.api_limit_error_retry))
                    } else {
                        showCustomErrorToast(
                            this@CartDetailActivity,
                            getString(R.string.http_error_format, getString(R.string.reject_error_base), e.code())
                        )
                    }
                } catch (e: Exception) {
                    val msg = e.message?.let { " (${it})" } ?: ""
                    showCustomErrorToast(this@CartDetailActivity, getString(R.string.reject_error_base) + msg)
                }
            }
        }

        buttonEliminar.setOnClickListener {
            lifecycleScope.launch {
                try {
                    cartRepository.deleteCart(cartId)
                    showCustomOkToast(this@CartDetailActivity, "Carrito eliminado")
                    finish()
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@CartDetailActivity, getString(R.string.api_limit_error_retry))
                    } else {
                        showCustomErrorToast(
                            this@CartDetailActivity,
                            getString(R.string.http_error_format, getString(R.string.delete_error_base), e.code())
                        )
                    }
                } catch (e: Exception) {
                    val msg = e.message?.let { " (${it})" } ?: ""
                    showCustomErrorToast(this@CartDetailActivity, getString(R.string.delete_error_base) + msg)
                }
            }
        }
    }
}
