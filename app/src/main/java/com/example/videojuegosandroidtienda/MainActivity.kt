package com.example.videojuegosandroidtienda

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Genre
import com.example.videojuegosandroidtienda.data.entities.Platform
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.repository.StoreRepository
import com.example.videojuegosandroidtienda.ui.detail.DetailActivity
import com.example.videojuegosandroidtienda.ui.main.VideogameAdapter
import com.example.videojuegosandroidtienda.ui.main.SimpleItemSelectedListener
import com.google.android.material.appbar.MaterialToolbar
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import kotlinx.coroutines.launch
import android.widget.Spinner
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val repository = StoreRepository()
    private val adapter = VideogameAdapter()

    private var allVideogames: List<Videogame> = emptyList()
    private var platforms: List<Platform> = emptyList()
    private var genres: List<Genre> = emptyList()
    private var platformNamesMap: Map<String, String> = emptyMap()
    private var genreNamesMap: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        // Listener de clics del menú de la toolbar
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    true
                }
                R.id.action_cart -> {
                    val t = com.example.videojuegosandroidtienda.data.network.TokenStore.token
                    if (t.isNullOrBlank()) {
                        startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.auth.LoginActivity::class.java))
                    } else {
                        startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.cart.CartActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
        // Ajustar icono de carrito según estado inicial
        updateCartIcon(toolbar)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val spinnerPlatform = findViewById<Spinner>(R.id.spinnerPlatform)
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)
        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerVideogames)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Bottom navigation
        bottomNav.selectedItemId = R.id.nav_search
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> true
                R.id.nav_cart -> {
                    val t = com.example.videojuegosandroidtienda.data.network.TokenStore.token
                    if (t.isNullOrBlank()) {
                        startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.auth.LoginActivity::class.java))
                    } else {
                        startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.cart.CartActivity::class.java))
                    }
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.profile.ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch {
            try {
                platforms = repository.getPlatforms()
                genres = repository.getGenres()
                allVideogames = repository.getVideogames()

                platformNamesMap = platforms.associate { it.id to it.name }
                genreNamesMap = genres.associate { it.id to it.name }

                setupSpinners(spinnerPlatform, spinnerGenre)
                adapter.submit(allVideogames, genreNamesMap, platformNamesMap)
                adapter.setOnItemClickListener { vg ->
                    val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                        putExtra(DetailActivity.EXTRA_ID, vg.id)
                        putExtra(DetailActivity.EXTRA_IMAGE_URL, vg.cover_image?.url)
                        putExtra(DetailActivity.EXTRA_TITLE, vg.title)
                        putExtra(DetailActivity.EXTRA_GENRE_NAME, genreNamesMap[vg.genre_id] ?: "-")
                        putExtra(DetailActivity.EXTRA_PLATFORM_NAME, platformNamesMap[vg.platform_id] ?: "-")
                        putExtra(DetailActivity.EXTRA_PRICE, vg.price)
                        putExtra(DetailActivity.EXTRA_DESCRIPTION, vg.description ?: "")
                    }
                    startActivity(intent)
                }
            } catch (e: Exception) {
                // TODO: manejar error (mostrar mensaje)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applyFilters(searchView.query.toString(), spinnerPlatform, spinnerGenre)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters(newText, spinnerPlatform, spinnerGenre)
                return true
            }
        })

        // Cuando cambian los filtros, aplicamos
        spinnerPlatform.setOnItemSelectedListener(SimpleItemSelectedListener { 
            applyFilters(searchView.query.toString(), spinnerPlatform, spinnerGenre)
        })
        spinnerGenre.setOnItemSelectedListener(SimpleItemSelectedListener { 
            applyFilters(searchView.query.toString(), spinnerPlatform, spinnerGenre)
        })
    }

    override fun onResume() {
        super.onResume()
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        updateCartIcon(toolbar)
    }

    private fun updateCartIcon(toolbar: MaterialToolbar) {
        val t = com.example.videojuegosandroidtienda.data.network.TokenStore.token
        if (!t.isNullOrBlank()) {
            toolbar.navigationIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view)
        } else {
            toolbar.navigationIcon = null
        }
        val menuItem = toolbar.menu.findItem(R.id.action_cart)
        menuItem?.isVisible = !t.isNullOrBlank()
    }

    private fun setupSpinners(spPlatform: Spinner, spGenre: Spinner) {
        val platformNames = listOf("Todas") + platforms.map { it.name }
        val genreNames = listOf("Todos") + genres.map { it.name }

        spPlatform.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, platformNames)
        spGenre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genreNames)
    }

    private fun applyFilters(query: String?, spPlatform: Spinner, spGenre: Spinner) {
        val selectedPlatformName = spPlatform.selectedItem?.toString()
        val selectedGenreName = spGenre.selectedItem?.toString()

        val platformId = platforms.firstOrNull { it.name == selectedPlatformName }?.id
        val genreId = genres.firstOrNull { it.name == selectedGenreName }?.id

        val filtered = repository.filterVideogames(allVideogames, query, platformId, genreId)
        platformNamesMap = platforms.associate { it.id to it.name }
        genreNamesMap = genres.associate { it.id to it.name }
        adapter.submit(filtered, genreNamesMap, platformNamesMap)
    }
}