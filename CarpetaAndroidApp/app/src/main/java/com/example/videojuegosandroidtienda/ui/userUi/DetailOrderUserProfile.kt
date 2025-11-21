package com.example.videojuegosandroidtienda.ui.userUi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.ui.adapter.VideogameAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.DateFormat
import java.util.Date

class DetailOrderUserProfile : AppCompatActivity() {
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
        setContentView(R.layout.activity_order_user_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewIdCarrito = findViewById(R.id.textViewIdCarrito)
        textViewFechaCarrito = findViewById(R.id.textViewFechaCarrito)
        textViewTotalCarrito = findViewById(R.id.textViewTotalCArrito)
        textViewUserId = findViewById(R.id.textViewUserId)
        textViewEstado = findViewById(R.id.textViewEstado)
        recyclerViewVideogames = findViewById(R.id.recyclerViewUserCartItems)
        videogameAdapter = VideogameAdapter()
        recyclerViewVideogames.adapter = videogameAdapter
        recyclerViewVideogames.layoutManager = LinearLayoutManager(this)

        val buttonDelete = findViewById<MaterialButton>(R.id.buttonDeleteCart)

        val cartId = intent.getStringExtra("cart_id")?.trim()
        if (cartId.isNullOrBlank()) {
            showCustomErrorToast(this, getString(R.string.cart_id_missing_error))
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val cart = cartRepository.getCartById(cartId)
                textViewIdCarrito.text = getString(R.string.cart_id_format, cart.id)
                val formattedDate = try {
                    DateFormat.getDateTimeInstance().format(Date(cart.created_at))
                } catch (_: Exception) { cart.created_at.toString() }
                textViewFechaCarrito.text = getString(R.string.cart_created_format, formattedDate)
                textViewTotalCarrito.text = getString(R.string.cart_total_format, cart.total)
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

                val platforms = videogameRepository.getPlatforms()
                val genres = videogameRepository.getGenres()
                val platformNames = platforms.associate { it.id to it.name }
                val genreNames = genres.associate { it.id to it.name }

                val allVideogames = videogameRepository.getVideogames()
                val ids = cart.videogames_id ?: emptyList()
                val selected = allVideogames.filter { vg -> ids.contains(vg.id) }
                videogameAdapter.submit(selected, genreNames, platformNames)
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    showCustomErrorToast(this@DetailOrderUserProfile, getString(R.string.api_limit_error_retry))
                } else {
                    showCustomErrorToast(
                        this@DetailOrderUserProfile,
                        getString(R.string.http_error_format, getString(R.string.cart_load_error), e.code())
                    )
                }
            } catch (e: Exception) {
                val msg = e.message?.let { " (${it})" } ?: ""
                showCustomErrorToast(this@DetailOrderUserProfile, getString(R.string.cart_load_error) + msg)
            }
        }

        buttonDelete.setOnClickListener {
            lifecycleScope.launch {
                try {
                    cartRepository.deleteCart(cartId)
                    showCustomOkToast(this@DetailOrderUserProfile, getString(R.string.cart_deleted_success))
                    finish()
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showCustomErrorToast(this@DetailOrderUserProfile, getString(R.string.api_limit_error_retry))
                    } else {
                        showCustomErrorToast(
                            this@DetailOrderUserProfile,
                            getString(R.string.http_error_format, getString(R.string.delete_error_base), e.code())
                        )
                    }
                } catch (e: Exception) {
                    val msg = e.message?.let { " (${it})" } ?: ""
                    showCustomErrorToast(this@DetailOrderUserProfile, getString(R.string.delete_error_base) + msg)
                }
            }
        }
    }
}