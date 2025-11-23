package com.example.videojuegosandroidtienda.ui.adminUi

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.ui.adminUi.UserCreateActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminUserAdapter
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import retrofit2.HttpException
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class UserAdminDashboardFragment : Fragment() {
    private val repository = UserRepository()
    private var allUsers: List<User> = emptyList()
    private lateinit var adapter: AdminUserAdapter
    private lateinit var searchBar: SearchBar
    private lateinit var searchView: SearchView
    private lateinit var suggestionsAdapter: AdminUserAdapter
    private lateinit var switchOrder: android.widget.Switch
    private var lastDataLoadAt: Long = 0
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadInitialData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBar = view.findViewById(R.id.searchBarUsers)
        searchView = view.findViewById(R.id.searchViewUsers)
        switchOrder = view.findViewById(R.id.switchOrder)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerUsers)
        val buttonCrearUsuario = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonCrearUsuario)

        adapter = AdminUserAdapter(emptyList()) { user ->
            val intent = Intent(requireContext(), UserDetailActivity::class.java)
            intent.putExtra("user_id", user.id)
            resultLauncher.launch(intent)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val suggestionsRecycler = searchView.findViewById<RecyclerView>(R.id.searchSuggestionsRecycler)
        suggestionsAdapter = AdminUserAdapter(emptyList()) { user ->
            val intent = Intent(requireContext(), UserDetailActivity::class.java)
            intent.putExtra("user_id", user.id)
            resultLauncher.launch(intent)
        }
        suggestionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        suggestionsRecycler.adapter = suggestionsAdapter

        buttonCrearUsuario.setOnClickListener {
            val intent = Intent(requireContext(), UserCreateActivity::class.java)
            resultLauncher.launch(intent)
        }

        // Evitar carga inmediata para reducir ráfagas de red al crear múltiples fragments.

        // Cargaremos en onResume cuando esté visible.
        searchView.setupWithSearchBar(searchBar)
        searchView.editText.hint = getString(R.string.search_user_query_hint)
        searchView.editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                render()
                updateSuggestions()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        searchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                suggestionsAdapter.submit(emptyList())
            }
        }

        switchOrder.setOnCheckedChangeListener { _, _ -> render() }
    }


    private fun render() {
        val q = searchView.editText.text?.toString()?.trim()?.lowercase().orEmpty()
        var list = if (q.isEmpty()) allUsers else allUsers.filter { it.name.lowercase().contains(q) }
        list = if (switchOrder.isChecked) {
            list.sortedByDescending { it.created_at?.toLongOrNull() ?: Long.MIN_VALUE }
        } else {
            list.sortedBy { it.created_at?.toLongOrNull() ?: Long.MAX_VALUE }
        }
        adapter.submit(list)
    }


    private fun updateSuggestions() {
        val q = searchView.editText.text?.toString()?.trim()?.lowercase().orEmpty()
        var list = if (q.isEmpty()) emptyList() else allUsers.filter { it.name.lowercase().contains(q) }
        list = if (switchOrder.isChecked) {
            list.sortedByDescending { it.created_at?.toLongOrNull() ?: Long.MIN_VALUE }
        } else {
            list.sortedBy { it.created_at?.toLongOrNull() ?: Long.MAX_VALUE }
        }
        suggestionsAdapter.submit(list)
    }


    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                allUsers = repository.listUsers()
                lastDataLoadAt = System.currentTimeMillis()
                render()
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 429) {
                    showCustomErrorToast(requireContext(), "Límite de API alcanzado. Espera ~20s e intenta de nuevo")
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
        if (now - lastDataLoadAt >= 60_000L || allUsers.isEmpty()) {
            loadInitialData()
        }
    }
}
