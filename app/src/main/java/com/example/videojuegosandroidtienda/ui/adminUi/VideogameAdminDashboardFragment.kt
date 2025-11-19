package com.example.videojuegosandroidtienda.ui.adminUi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminVideogameAdapter
import kotlinx.coroutines.launch

class VideogameAdminDashboardFragment : Fragment() {
    private val repository = VideogameRepository()
    private var all: List<Videogame> = emptyList()
    private lateinit var adapter: AdminVideogameAdapter

    private var platformId: String? = null
    private var genreId: String? = null
    private lateinit var searchView: SearchView
    private lateinit var spinnerPlatform: android.widget.Spinner
    private lateinit var spinnerGenre: android.widget.Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_videogame_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.searchViewVideogame)
        spinnerPlatform = view.findViewById(R.id.spinnerPlatformAdmin)
        spinnerGenre = view.findViewById(R.id.spinnerGenreAdmin)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerVideogamesAdmin)

        adapter = AdminVideogameAdapter(emptyList()) { vg ->
            val intent = Intent(requireContext(), com.example.videojuegosandroidtienda.ui.detail.DetailActivity::class.java)
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
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        lifecycleScope.launch {
            try {
                all = repository.getVideogames()
                val platforms = repository.getPlatforms()
                val genres = repository.getGenres()

                val platformItems = listOf("Todas") + platforms.map { it.name }
                val genreItems = listOf("Todos") + genres.map { it.name }

                val platformAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, platformItems).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerPlatform.adapter = platformAdapter

                val genreAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genreItems).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerGenre.adapter = genreAdapter

                spinnerPlatform.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        platformId = if (position == 0) null else platforms[position - 1].id
                        render()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) { }
                }
                spinnerGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        genreId = if (position == 0) null else genres[position - 1].id
                        render()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) { }
                }

                render()
            } catch (_: Exception) { }
        }

        searchView.queryHint = "Buscar por nombre"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { render(); return true }
            override fun onQueryTextChange(newText: String?): Boolean { render(); return true }
        })
    }

    private fun render() {
        val q = searchView.query?.toString()?.trim()
        val list = repository.filterVideogames(all, q, platformId, genreId)
        adapter.submit(list)
    }
}
