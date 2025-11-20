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
    private var all: List<Cart> = emptyList()
    private var userNames: Map<String, String> = emptyMap()
    private var approvedFilter: Boolean? = null

    // Views
    private lateinit var search: SearchView
    private lateinit var spinnerApproved: android.widget.Spinner
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

        search.queryHint = "Buscar por usuario"
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { render(); return true }
            override fun onQueryTextChange(newText: String?): Boolean { render(); return true }
        })
    }

    private fun render() {
        val q = search.query?.toString()?.trim()?.lowercase().orEmpty()
        var list = all
        approvedFilter?.let { ap -> list = list.filter { (it.aprobado ?: false) == ap } }
        if (q.isNotEmpty()) {
            list = list.filter { (userNames[it.user_id]?.lowercase() ?: "").contains(q) }
        }
        adapter.submit(list)
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                val carts = cartRepository.getCarts()
                all = carts
                val users = userRepository.listUsers()
                userNames = users.associate { it.id to it.name }
                adapter.setUserNames(userNames)

                val items = listOf("Todos", "Aprobados", "Pendientes")
                val sAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerApproved.adapter = sAdapter
                spinnerApproved.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        approvedFilter = when (position) {
                            1 -> true
                            2 -> false
                            else -> null
                        }
                        render()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
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
