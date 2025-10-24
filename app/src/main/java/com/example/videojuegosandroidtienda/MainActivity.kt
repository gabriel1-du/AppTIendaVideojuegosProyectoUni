package com.example.videojuegosandroidtienda

import android.content.Intent
import android.os.Bundle
import android.service.autofill.UserData
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.HttpException
import com.example.videojuegosandroidtienda.data.entities.Genre
import com.example.videojuegosandroidtienda.data.entities.Platform
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import com.example.videojuegosandroidtienda.ui.detail.DetailActivity
import com.example.videojuegosandroidtienda.ui.Adapter_CLickListener.SimpleItemSelectedListener
import com.example.videojuegosandroidtienda.ui.Adapter_CLickListener.VideogameAdapter
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val AuthRepository = AuthRepository() //Repo auth

    private val VideogameRepository = VideogameRepository() //Repo videogame

    private val adapter = VideogameAdapter() //Adaptador para las tajetas

    private var allVideogames: List<Videogame> = emptyList()
    private var platforms: List<Platform> = emptyList()
    private var genres: List<Genre> = emptyList()
    private var platformNamesMap: Map<String, String> = emptyMap()
    private var genreNamesMap: Map<String, String> = emptyMap()
    private var lastDataLoadAt: Long = 0

    // Repositorio y usuario autenticado
    private val userRepository = UserRepository()
    private var currentUser: User? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
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
                R.id.action_upload_videogame -> {
                    startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.upload.AddVideogameActivity::class.java))
                    true
                }
                R.id.action_cart -> {
                    val t = com.example.videojuegosandroidtienda.data.network.TokenStore.token
                    if (t.isNullOrBlank()) {
                        startActivity(Intent( this, com.example.videojuegosandroidtienda.ui.auth.LoginActivity::class.java))
                    } else {
                        startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.cart.CartActivity::class.java))
                    }
                    true
                }
                R.id.action_refresh -> {
                    showCustomOkToast(this, "Refrescando...")
                    loadInitialData()
                    true
                }
                else -> false
            }
        }
        // Cargar token persistido y ajustar iconos según estado inicial
        AuthRepository.loadPersistedToken()
        updateCartIcon(toolbar)
        val t0 = com.example.videojuegosandroidtienda.data.network.TokenStore.token
        toolbar.menu.findItem(R.id.action_login)?.isVisible = t0.isNullOrBlank()
        // Actualizar visibilidad de opción de subir videojuego según rol
        updateAdminUploadVisibility(toolbar)

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
        val searchView = findViewById<SearchView>(R.id.searchView)
        val spinnerPlatform = findViewById<Spinner>(R.id.spinnerPlatform)
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)
        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerVideogames)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Bottom navigation
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

    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                platforms = VideogameRepository.getPlatforms()
                genres = VideogameRepository.getGenres()
                allVideogames = VideogameRepository.getVideogames()

                platformNamesMap = platforms.associate { it.id to it.name }
                genreNamesMap = genres.associate { it.id to it.name }

                val spinnerPlatform = findViewById<Spinner>(R.id.spinnerPlatform)
                val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)
                setupSpinners(spinnerPlatform, spinnerGenre)
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 429) {
                    showCustomErrorToast(this@MainActivity, "Límite de API alcanzado. Espera ~20s e intenta de nuevo")
                } else {
                    showCustomErrorToast(this@MainActivity, "Error al cargar datos")
                }
            }
        }
    }

    // Refresca el icono del carrito al reanudar la actividad
    override fun onResume() {
        super.onResume()
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        updateCartIcon(toolbar)
        val t = com.example.videojuegosandroidtienda.data.network.TokenStore.token
        toolbar.menu.findItem(R.id.action_login)?.isVisible = t.isNullOrBlank()
        // Actualizar visibilidad de opción de subir videojuego según rol
        updateAdminUploadVisibility(toolbar)
        val now = System.currentTimeMillis()
        if (now - lastDataLoadAt >= 20_000L || allVideogames.isEmpty()) {
            lastDataLoadAt = now
            loadInitialData()
        }
        
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_search
    }

    // Muestra/oculta acción de carrito según sesión
    private fun updateCartIcon(toolbar: MaterialToolbar) {
        val t = com.example.videojuegosandroidtienda.data.network.TokenStore.token
        // No establecer icono de navegación (evita el "ojo" sobre la barra inferior)
        toolbar.navigationIcon = null
        val menuItem = toolbar.menu.findItem(R.id.action_cart)
        menuItem?.isVisible = !t.isNullOrBlank()
    }

    // Actualiza visibilidad de "subir videojuego" según si el usuario es admin
    private fun updateAdminUploadVisibility(toolbar: MaterialToolbar) {
        lifecycleScope.launch {
            try {
                val token = com.example.videojuegosandroidtienda.data.network.TokenStore.token
                val uploadItem = toolbar.menu.findItem(R.id.action_upload_videogame)
                if (token.isNullOrBlank()) {
                    uploadItem?.isVisible = false
                    currentUser = null
                } else {
                    // Obtener usuario autenticado y luego sus datos por ID
                    val authUser = AuthRepository.getAuthMe()
                    val fetchedUser = userRepository.getUser(authUser.id)
                    currentUser = fetchedUser
                    uploadItem?.isVisible = fetchedUser.admin
                }
            } catch (_: Exception) {
                toolbar.menu.findItem(R.id.action_upload_videogame)?.isVisible = false
            }
        }
    }

    // Inicializa los spinners de plataforma y género
    private fun setupSpinners(spPlatform: Spinner, spGenre: Spinner) {
        val platformNames = listOf("Todas") + platforms.map { it.name }
        val genreNames = listOf("Todos") + genres.map { it.name }

        spPlatform.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, platformNames)
        spGenre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genreNames)
    }

    // Aplica filtros y actualiza la lista de videojuegos
    private fun applyFilters(query: String?, spPlatform: Spinner, spGenre: Spinner) {
        val selectedPlatformName = spPlatform.selectedItem?.toString()
        val selectedGenreName = spGenre.selectedItem?.toString()

        val platformId = platforms.firstOrNull { it.name == selectedPlatformName }?.id
        val genreId = genres.firstOrNull { it.name == selectedGenreName }?.id

        val filtered = VideogameRepository.filterVideogames(allVideogames, query, platformId, genreId)
        platformNamesMap = platforms.associate { it.id to it.name }
        genreNamesMap = genres.associate { it.id to it.name }
        adapter.submit(filtered, genreNamesMap, platformNamesMap)

        if (filtered.isEmpty()) { //Si no se encuentra nada con aquellas busquedas
            showCustomErrorToast(this, "No se encontraron resultados")
        }
    }
}
