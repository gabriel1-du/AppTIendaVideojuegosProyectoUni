package com.example.videojuegosandroidtienda.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.cart.CartManager
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.example.videojuegosandroidtienda.data.functions.setupBottomNavigation
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {

    private val cartRepository = CartRepository()
    private val authRepository = AuthRepository()

    // Renderiza el carrito y prepara acciones de compra
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)

        // Si no hay sesión, enviar a login
        val token = TokenStore.token
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        setupBottomNavigation(this, bottomNav, R.id.nav_cart)

        val itemsContainer = findViewById<LinearLayout>(R.id.cartItemsContainer)
        val totalText = findViewById<TextView>(R.id.cartTotal)
        val payButton = findViewById<Button>(R.id.buttonPay)

        // Dibuja tarjetas del carrito y sincroniza cantidades
        fun renderCart() {
            itemsContainer.removeAllViews()
            val items = CartManager.getItems()
            if (items.isEmpty()) {
                val tv = TextView(this@CartActivity).apply {
                    text = "No hay productos en el carrito"
                    setTextColor(resources.getColor(R.color.textSecondary, theme))
                }
                itemsContainer.addView(tv)
            } else {
                items.forEach { (product, qty) ->
                    val view = layoutInflater.inflate(R.layout.view_cart_product, itemsContainer, false)
                    val image = view.findViewById<ImageView>(R.id.imageProduct)
                    val title = view.findViewById<TextView>(R.id.textTitle)
                    val price = view.findViewById<TextView>(R.id.textPrice)
                    val quantity = view.findViewById<TextView>(R.id.textQuantity)
                    val minus = view.findViewById<Button>(R.id.buttonMinus)
                    val plus = view.findViewById<View>(R.id.buttonPlus) // Puede ser Button o ImageButton


                    title.text = product.title
                    price.text = "Precio: ${product.price}"
                    quantity.text = qty.toString()

                    val url = product.imageUrl
                    if (!url.isNullOrBlank()) {
                        val req = ImageRequest.Builder(this@CartActivity)
                            .data(url)
                            .target(image)
                            .build()
                        image.context.imageLoader.enqueue(req)
                    } else {
                        image.setImageResource(android.R.color.darker_gray)
                    }

                    minus.setOnClickListener {
                        CartManager.decrease(product.id)
                        updateTotal(totalText)
                        renderCart()
                    }
                    plus.setOnClickListener {
                        CartManager.increase(product.id)
                        updateTotal(totalText)
                        renderCart()
                    }

                    itemsContainer.addView(view)
                }
            }
            updateTotal(totalText)
        }

        renderCart()

        payButton.setOnClickListener {
            if (CartManager.getItems().isEmpty()) {
                showCustomErrorToast(this, "No hay productos en el carrito")
            } else {
                lifecycleScope.launch {
                    try {
                        // Obtenemos el usuario autenticado desde el backend
                        val user = authRepository.getAuthMe()
                        val userId = user.id

                        val cart = Cart(
                            id = "", // El ID se genera en el backend
                            user_id = userId,
                            total = CartManager.getTotal(),
                            created_at = System.currentTimeMillis()
                        )
                        cartRepository.postCart(cart)

                        CartManager.clear()
                        showCustomOkToast(this@CartActivity, "Se ha realizado exitosamente la compra")

                        startActivity(Intent(this@CartActivity, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        finish()
                    } catch (e: Exception) {
                        showCustomErrorToast(this@CartActivity, "Error al procesar la compra: ${e.message}")
                    }
                }
            }
        }
    }

    // Actualiza el total acumulado del carrito
    private fun updateTotal(totalText: TextView) {
        totalText.text = "Total acumulado: ${CartManager.getTotal()}"
    }
}