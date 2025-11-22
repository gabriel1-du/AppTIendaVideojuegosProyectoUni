package com.example.videojuegosandroidtienda.ui.adminUi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminVideogameAdapter
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.ui.upload.AddVideogameActivity
import retrofit2.HttpException
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class VideogameAdminDashboardFragment : Fragment() {
    private val repository = VideogameRepository()
    private var all: List<Videogame> = emptyList()
    private lateinit var adapter: AdminVideogameAdapter
    private lateinit var suggestionsAdapter: AdminVideogameAdapter

    private var platformId: String? = null
    private var genreId: String? = null
    private lateinit var searchBar: SearchBar
    private lateinit var searchView: SearchView
    private lateinit var spinnerPlatform: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var spinnerGenre: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private var platformNamesMap: Map<String, String> = emptyMap()
    private var genreNamesMap: Map<String, String> = emptyMap()
    private var lastDataLoadAt: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_videogame_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBar = view.findViewById(R.id.searchBarVideogame)
        searchView = view.findViewById(R.id.searchViewVideogame)
        spinnerPlatform = view.findViewById(R.id.spinnerPlatformAdmin)
        spinnerGenre = view.findViewById(R.id.spinnerGenreAdmin)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerVideogamesAdmin)
        val buttonAdd = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonAddVideogame)

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

        val suggestionsRecycler = searchView.findViewById<RecyclerView>(R.id.searchSuggestionsRecycler)
        suggestionsAdapter = AdminVideogameAdapter(emptyList()) { vg ->
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
        suggestionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        suggestionsRecycler.adapter = suggestionsAdapter

        buttonAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddVideogameActivity::class.java))
        }

        // No cargar aún para evitar ráfaga de requests al crear múltiples fragments.
        // La carga se realizará en onResume cuando el fragment esté visible.

        searchView.setupWithSearchBar(searchBar)
        searchView.editText.hint = getString(R.string.search_videogame_query_hint_short)
        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                render()
                updateSuggestions()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        searchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                suggestionsAdapter.submit(emptyList(), platformNamesMap, genreNamesMap)
            }
        }
    }

    private fun render() {
        val q = searchView.editText.text?.toString()?.trim()
        val list = repository.filterVideogames(all, q, platformId, genreId)
        adapter.submit(list, platformNamesMap, genreNamesMap)
    }

    private fun updateSuggestions() {
        val q = searchView.editText.text?.toString()?.trim()
        val list = repository.filterVideogames(all, q, platformId, genreId)
        if (q.isNullOrBlank()) {
            suggestionsAdapter.submit(emptyList(), platformNamesMap, genreNamesMap)
        } else {
            suggestionsAdapter.submit(list, platformNamesMap, genreNamesMap)
        }
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                val platforms = repository.getPlatforms()
                val genres = repository.getGenres()
                all = repository.getVideogames()

                platformNamesMap = platforms.associate { it.id to it.name }
                genreNamesMap = genres.associate { it.id to it.name }

                val platformItems = listOf("Todas") + platforms.map { it.name }
                val platformAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, platformItems)
                spinnerPlatform.setAdapter(platformAdapter)
                spinnerPlatform.setOnItemClickListener { _, _, position, _ ->
                    platformId = if (position == 0) null else platforms[position - 1].id
                    render()
                }

                val genreItems = listOf("Todas") + genres.map { it.name }
                val genreAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, genreItems)
                spinnerGenre.setAdapter(genreAdapter)
                spinnerGenre.setOnItemClickListener { _, _, position, _ ->
                    genreId = if (position == 0) null else genres[position - 1].id
                    render()
                }

                lastDataLoadAt = System.currentTimeMillis()
                render()
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 429) {
                    showCustomErrorToast(requireContext(), "Límite de API alcanzado. Espera ~20s e intenta de nuevo")
                    // Reintento único tras 20s si sigue visible
                    lifecycleScope.launch {
                        delay(20_000)
                        if (isResumed) loadInitialData()
                    }
                } else {
                    showCustomErrorToast(requireContext(), "Error al cargar datos")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val now = System.currentTimeMillis()
        if (now - lastDataLoadAt >= 20_000L || all.isEmpty()) {
            loadInitialData()
        }
    }
}
