package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.ui.Adapter.AdminCartAdapter
import com.example.videojuegosandroidtienda.ui.Adapter.AdminUserAdapter
import kotlinx.coroutines.launch

class VistaAdminDashboard : AppCompatActivity() {
    private val cartRepository = CartRepository()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vista_admin_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerView)
        val cartAdapter = AdminCartAdapter(emptyList()) { cart ->
            val intent = android.content.Intent(this, com.example.videojuegosandroidtienda.ui.adminUi.CartDetailActivity::class.java)
            intent.putExtra("cart_id", cart.id)
            startActivity(intent)
        }
        val userAdapter = AdminUserAdapter(emptyList()) { user ->
            val intent = android.content.Intent(this, com.example.videojuegosandroidtienda.ui.adminUi.UserDetailActivity::class.java)
            intent.putExtra("user_id", user.id)
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = cartAdapter

        val header = findViewById<android.widget.TextView>(R.id.textDashboardHeader)
        val buttonUsuarios = findViewById<android.widget.Button>(R.id.buttonUsuariosDashboard)
        val buttonCompras = findViewById<android.widget.Button>(R.id.buttonCompras)

        buttonUsuarios.setOnClickListener {
            header.text = "Lista de usuarios"
            recycler.adapter = userAdapter
            lifecycleScope.launch {
                try {
                    val users = userRepository.listUsers()
                    userAdapter.submit(users)
                } catch (_: Exception) {
                    // Silenciar errores en la vista admin por ahora
                }
            }
        }

        buttonCompras.setOnClickListener {
            header.text = "Lista de carritos"
            recycler.adapter = cartAdapter
            lifecycleScope.launch {
                try {
                    val carts = cartRepository.getCarts()
                    cartAdapter.submit(carts)
                } catch (_: Exception) {
                    // Silenciar errores en la vista admin por ahora
                }
            }
        }

        lifecycleScope.launch {
            try {
                val carts = cartRepository.getCarts()
                cartAdapter.submit(carts)
            } catch (_: Exception) {
                // Silenciar errores en la vista admin por ahora
            }
        }
    }
}