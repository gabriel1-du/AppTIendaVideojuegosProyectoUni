package com.example.videojuegosandroidtienda.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.example.videojuegosandroidtienda.data.repository.StoreRepository
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {
    private val repository = StoreRepository()

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

        lifecycleScope.launch {
            try {
                val carts = repository.getCarts()
                itemsContainer.removeAllViews()
                var accumulatedTotal = 0.0
                if (carts.isEmpty()) {
                    val tv = TextView(this@CartActivity).apply {
                        text = "No hay items en el carrito"
                        setTextColor(resources.getColor(R.color.textSecondary, theme))
                    }
                    itemsContainer.addView(tv)
                } else {
                    carts.forEach { cart ->
                        accumulatedTotal += cart.total
                        val itemView = layoutInflater.inflate(R.layout.view_cart_item_price, itemsContainer, false)
                        itemView.findViewById<TextView>(R.id.textCartItemTitle).text = "Carrito #${cart.id}"
                        itemView.findViewById<TextView>(R.id.textCartItemPrice).text = "Precio total: ${cart.total}"
                        itemsContainer.addView(itemView)
                    }
                }
                totalText.text = "Total acumulado: ${accumulatedTotal}"
            } catch (e: Exception) {
                Toast.makeText(this@CartActivity, "Error al cargar carrito", Toast.LENGTH_SHORT).show()
            }
        }

        payButton.setOnClickListener {
            Toast.makeText(this@CartActivity, "compra exitosamente realizada", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@CartActivity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }
    }

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
}