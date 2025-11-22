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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminCartAdapter
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import retrofit2.HttpException
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class OrdersDashboardFragment : Fragment() {
    private val cartRepository = CartRepository()
    private val userRepository = UserRepository()
    private lateinit var adapter: AdminCartAdapter
    private lateinit var suggestionsAdapter: AdminCartAdapter
    private var all: List<Cart> = emptyList()
    private var userNames: Map<String, String> = emptyMap()
    private var approvedFilter: Boolean? = null

    // Views
    private lateinit var searchBar: SearchBar
    private lateinit var search: SearchView
    private lateinit var spinnerApproved: MaterialAutoCompleteTextView
    private lateinit var recycler: RecyclerView
    private var lastDataLoadAt: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBar = view.findViewById(R.id.searchBarOrders)
        search = view.findViewById(R.id.searchOrdersByUser)
        spinnerApproved = view.findViewById(R.id.spinnerApproved)
        recycler = view.findViewById(R.id.recyclerOrders)

        adapter = AdminCartAdapter(emptyList()) { cart ->
            val intent = Intent(requireContext(), CartDetailActivity::class.java)
            intent.putExtra("cart_id", cart.id)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Evitar carga inmediata para no disparar múltiples requests al iniciar la actividad.
        // Se cargará en onResume cuando el fragment esté visible.

        search.setupWithSearchBar(searchBar)
        search.editText.hint = getString(R.string.search_orders_query_hint)
        // Recycler de sugerencias dentro del overlay del SearchView
        val suggestionsRecycler = search.findViewById<RecyclerView>(R.id.searchSuggestionsRecycler)
        suggestionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        suggestionsAdapter = AdminCartAdapter(emptyList()) { cart ->
            val intent = Intent(requireContext(), CartDetailActivity::class.java)
            intent.putExtra("cart_id", cart.id)
            startActivity(intent)
        }
        suggestionsRecycler.adapter = suggestionsAdapter

        search.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                render()
                updateSuggestions()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        search.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                suggestionsAdapter.submit(emptyList())
            }
        }
    }

    private fun render() {
        val q = search.editText.text?.toString()?.trim()?.lowercase().orEmpty()
        val list = computeList(q)
        adapter.submit(list)
    }

    private fun updateSuggestions() {
        val q = search.editText.text?.toString()?.trim()?.lowercase().orEmpty()
        val list = computeList(q)
        if (q.isEmpty()) {
            suggestionsAdapter.submit(emptyList())
        } else {
            suggestionsAdapter.submit(list)
        }
    }

    private fun computeList(q: String): List<Cart> {
        var list = all
        approvedFilter?.let { ap -> list = list.filter { (it.aprobado ?: false) == ap } }
        if (q.isNotEmpty()) {
            list = list.filter {
                (userNames[it.user_id]?.lowercase() ?: "").contains(q) ||
                        (it.id?.lowercase() ?: "").contains(q)
            }
        }
        return list
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                val carts = cartRepository.getCarts()
                all = carts
                val users = userRepository.listUsers()
                userNames = users.associate { it.id to it.name }
                adapter.setUserNames(userNames)
                suggestionsAdapter.setUserNames(userNames)

                val items = listOf("Todos", "Aprobados", "Pendientes")
                val sAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
                spinnerApproved.setAdapter(sAdapter)
                spinnerApproved.setOnItemClickListener { _, _, position, _ ->
                    approvedFilter = when (position) {
                        1 -> true
                        2 -> false
                        else -> null
                    }
                    render()
                }

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
        if (now - lastDataLoadAt >= 20_000L || all.isEmpty()) {
            loadInitialData()
        }
    }
}
