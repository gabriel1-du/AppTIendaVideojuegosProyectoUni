package com.example.videojuegosandroidtienda.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.example.videojuegosandroidtienda.data.cart.CartManager
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import coil.imageLoader
import coil.request.ImageRequest

class CartActivity : AppCompatActivity() {

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

        setupBottomNav()

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
            Toast.makeText(this@CartActivity, "compra exitosamente realizada", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@CartActivity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }
    }

    // Configura navegación inferior y selección en Carrito
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_cart
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cart -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.profile.ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // Actualiza el total acumulado del carrito
    private fun updateTotal(totalText: TextView) {
        totalText.text = "Total acumulado: ${CartManager.getTotal()}"
    }
}