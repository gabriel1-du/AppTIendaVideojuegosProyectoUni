package com.example.videojuegosandroidtienda.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Genre
import com.example.videojuegosandroidtienda.data.entities.Platform
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.VideogameRepository
import com.example.videojuegosandroidtienda.databinding.FragmentHomeBinding
import com.example.videojuegosandroidtienda.ui.adapter.SimpleItemSelectedListener
import com.example.videojuegosandroidtienda.ui.adapter.VideogameAdapter
import com.example.videojuegosandroidtienda.ui.detail.DetailActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val videogameRepository = VideogameRepository()
    private val adapter = VideogameAdapter()

    private var allVideogames: List<Videogame> = emptyList()
    private var platforms: List<Platform> = emptyList()
    private var genres: List<Genre> = emptyList()
    private var platformNamesMap: Map<String, String> = emptyMap()
    private var genreNamesMap: Map<String, String> = emptyMap()
    private var lastDataLoadAt: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerVideogames.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerVideogames.adapter = adapter

        adapter.setOnItemClickListener { vg ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_ID, vg.id)
                putExtra(DetailActivity.EXTRA_IMAGE_URL, vg.cover_image?.url)
                putExtra(DetailActivity.EXTRA_TITLE, vg.title)
                putExtra(DetailActivity.EXTRA_GENRE_NAME, genreNamesMap[vg.genre_id] ?: "-")
                putExtra(DetailActivity.EXTRA_PLATFORM_NAME, platformNamesMap[vg.platform_id] ?: "-")
                putExtra(DetailActivity.EXTRA_GENRE_ID, vg.genre_id)
                putExtra(DetailActivity.EXTRA_PLATFORM_ID, vg.platform_id)
                putExtra(DetailActivity.EXTRA_PRICE, vg.price)
                putExtra(DetailActivity.EXTRA_DESCRIPTION, vg.description ?: "")
            }
            startActivity(intent)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applyFilters(binding.searchView.query.toString(), binding.spinnerPlatform, binding.spinnerGenre)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters(newText, binding.spinnerPlatform, binding.spinnerGenre)
                return true
            }
        })

        binding.spinnerPlatform.onItemSelectedListener = SimpleItemSelectedListener {
            applyFilters(binding.searchView.query.toString(), binding.spinnerPlatform, binding.spinnerGenre)
        }
        binding.spinnerGenre.onItemSelectedListener = SimpleItemSelectedListener {
            applyFilters(binding.searchView.query.toString(), binding.spinnerPlatform, binding.spinnerGenre)
        }

        loadInitialData()
    }

    fun loadInitialData() {
        lifecycleScope.launch {
            try {
                platforms = videogameRepository.getPlatforms()
                genres = videogameRepository.getGenres()
                allVideogames = videogameRepository.getVideogames()

                platformNamesMap = platforms.associate { it.id to it.name }
                genreNamesMap = genres.associate { it.id to it.name }

                setupSpinners(binding.spinnerPlatform, binding.spinnerGenre)

                // Apply initial filters after data is loaded
                applyFilters(null, binding.spinnerPlatform, binding.spinnerGenre)

            } catch (e: Exception) {
                if (e is HttpException && e.code() == 429) {
                    showCustomErrorToast(requireContext(), "Límite de API alcanzado. Espera ~20s e intenta de nuevo")
                } else {
                    showCustomErrorToast(requireContext(), "Error al cargar datos")
                }
            }
        }
    }

    private fun setupSpinners(spPlatform: Spinner, spGenre: Spinner) {
        val platformNames = listOf("Todas") + platforms.map { it.name }
        val genreNames = listOf("Todos") + genres.map { it.name }

        spPlatform.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, platformNames)
        spGenre.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genreNames)
    }

    private fun applyFilters(query: String?, spPlatform: Spinner?, spGenre: Spinner?) {
        val selectedPlatformName = spPlatform?.selectedItem?.toString()
        val selectedGenreName = spGenre?.selectedItem?.toString()

        val platformId = platforms.firstOrNull { it.name == selectedPlatformName }?.id
        val genreId = genres.firstOrNull { it.name == selectedGenreName }?.id

        val filtered = videogameRepository.filterVideogames(allVideogames, query, platformId, genreId)
        platformNamesMap = platforms.associate { it.id to it.name }
        genreNamesMap = genres.associate { it.id to it.name }
        adapter.submit(filtered, genreNamesMap, platformNamesMap)

        if (filtered.isEmpty()) {
            if (query != null || (platformId != null || genreId != null)) {
                showCustomErrorToast(requireContext(), "No se encontraron resultados")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val now = System.currentTimeMillis()
        if (now - lastDataLoadAt >= 20_000L || allVideogames.isEmpty()) {
            lastDataLoadAt = now
            loadInitialData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}