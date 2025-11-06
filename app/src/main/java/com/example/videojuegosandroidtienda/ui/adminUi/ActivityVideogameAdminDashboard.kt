package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminVideogameAdapter
import kotlinx.coroutines.launch

class ActivityVideogameAdminDashboard : AppCompatActivity() {
    private val repository = VideogameRepository()
    private var all: List<Videogame> = emptyList()
    private lateinit var adapter: AdminVideogameAdapter

    private var platformId: String? = null
    private var genreId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_videogame_admin_dashboard)

        val search = findViewById<SearchView>(R.id.searchViewVideogame)
        val spinnerPlatform = findViewById<android.widget.Spinner>(R.id.spinnerPlatformAdmin)
        val spinnerGenre = findViewById<android.widget.Spinner>(R.id.spinnerGenreAdmin)
        val recycler = findViewById<RecyclerView>(R.id.recyclerVideogamesAdmin)

        adapter = AdminVideogameAdapter(emptyList()) { vg ->
            val intent = android.content.Intent(this, com.example.videojuegosandroidtienda.ui.detail.DetailActivity::class.java)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_ID, vg.id)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_IMAGE_URL, vg.cover_image?.url)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_TITLE, vg.title)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_GENRE_ID, vg.genre_id)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_PLATFORM_ID, vg.platform_id)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_PRICE, vg.price)
            intent.putExtra(com.example.videojuegosandroidtienda.ui.detail.DetailActivity.EXTRA_DESCRIPTION, vg.description ?: "")
            intent.putExtra("extra_admin_mode", true)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        lifecycleScope.launch {
            try {
                all = repository.getVideogames()
                val platforms = repository.getPlatforms()
                val genres = repository.getGenres()

                val platformItems = listOf("Todas") + platforms.map { it.name }
                val genreItems = listOf("Todos") + genres.map { it.name }

                val platformAdapter = ArrayAdapter(this@ActivityVideogameAdminDashboard, android.R.layout.simple_spinner_item, platformItems).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerPlatform.adapter = platformAdapter

                val genreAdapter = ArrayAdapter(this@ActivityVideogameAdminDashboard, android.R.layout.simple_spinner_item, genreItems).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerGenre.adapter = genreAdapter

                spinnerPlatform.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                        platformId = if (position == 0) null else platforms[position - 1].id
                        render()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) { }
                }
                spinnerGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                        genreId = if (position == 0) null else genres[position - 1].id
                        render()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) { }
                }

                render()
            } catch (_: Exception) { }
        }

        search.queryHint = "Buscar por nombre"
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { render(); return true }
            override fun onQueryTextChange(newText: String?): Boolean { render(); return true }
        })
    }

    private fun render() {
        val search = findViewById<SearchView>(R.id.searchViewVideogame)
        val q = search.query?.toString()?.trim()
        val list = repository.filterVideogames(all, q, platformId, genreId)
        adapter.submit(list)
    }
}