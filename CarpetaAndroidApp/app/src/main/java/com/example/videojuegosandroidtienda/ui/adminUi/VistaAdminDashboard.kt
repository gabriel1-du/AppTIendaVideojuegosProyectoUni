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
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminCartAdapter
import com.example.videojuegosandroidtienda.ui.adapter.AdminUserAdapter
import com.example.videojuegosandroidtienda.ui.adapter.AdminVideogameAdapter
import kotlinx.coroutines.launch

class VistaAdminDashboard : AppCompatActivity() {
    private val cartRepository = CartRepository()
    private val userRepository = UserRepository()
    private val videogameRepository = VideogameRepository()

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
        val buttonVideogames = findViewById<android.widget.Button>(R.id.buttonVideogames)
        val buttonCrearUsuario = findViewById<android.widget.Button>(R.id.buttonCrearUsuario)
        val buttonCrearVideogame = findViewById<android.widget.Button>(R.id.buttonCrearVideogame)

        buttonUsuarios.setOnClickListener {
            header.text = getString(R.string.users_list)
            recycler.adapter = userAdapter
            buttonCrearUsuario.visibility = android.view.View.VISIBLE
            buttonCrearVideogame.visibility = android.view.View.GONE
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
            header.text = getString(R.string.carts_list)
            recycler.adapter = cartAdapter
            buttonCrearUsuario.visibility = android.view.View.GONE
            buttonCrearVideogame.visibility = android.view.View.GONE
            lifecycleScope.launch {
                try {
                    val carts = cartRepository.getCarts()
                    cartAdapter.submit(carts)
                } catch (_: Exception) {
                    // Silenciar errores en la vista admin por ahora
                }
            }
        }

        val vgAdapter = AdminVideogameAdapter(emptyList()) { vg ->
            val intent = android.content.Intent(this, com.example.videojuegosandroidtienda.ui.detail.DetailActivity::class.java)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_ID, vg.id)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_IMAGE_URL, vg.cover_image?.url)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_TITLE, vg.title)
            // Pasar IDs crudos para que la pantalla de edición los reciba correctamente
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_GENRE_ID, vg.genre_id)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_PLATFORM_ID, vg.platform_id)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_PRICE, vg.price)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_DESCRIPTION, vg.description ?: "")
            intent.putExtra("extra_admin_mode", true)
            startActivity(intent)
        }

        buttonVideogames.setOnClickListener {
            header.text = getString(R.string.videogames_list)
            recycler.adapter = vgAdapter
            buttonCrearUsuario.visibility = android.view.View.GONE
            buttonCrearVideogame.visibility = android.view.View.VISIBLE
            lifecycleScope.launch {
                try {
                    val platforms = videogameRepository.getPlatforms()
                    val genres = videogameRepository.getGenres()
                    val platformNamesMap = platforms.associate { it.id to it.name }
                    val genreNamesMap = genres.associate { it.id to it.name }
                    val vgs = videogameRepository.getVideogames()
                    vgAdapter.submit(vgs, genreNamesMap, platformNamesMap)
                } catch (_: Exception) {
                    // Silenciar errores en la vista admin por ahora
                }
            }
        }

        lifecycleScope.launch {
            try {
                val carts = cartRepository.getCarts()
                cartAdapter.submit(carts)
                buttonCrearUsuario.visibility = android.view.View.GONE
                buttonCrearVideogame.visibility = android.view.View.GONE
            } catch (_: Exception) {
                // Silenciar errores en la vista admin por ahora
            }
        }

        buttonCrearUsuario.setOnClickListener {
            val intent = android.content.Intent(this@VistaAdminDashboard, com.example.videojuegosandroidtienda.ui.adminUi.UserCreateActivity::class.java)
            startActivity(intent)
        }

        buttonCrearVideogame.setOnClickListener {
            val intent = android.content.Intent(this@VistaAdminDashboard, com.example.videojuegosandroidtienda.ui.upload.AddVideogameActivity::class.java)
            startActivity(intent)
        }
    }
}
