package com.example.videojuegosandroidtienda.ui.userUi

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.ui.adapter.UserCartAdapter
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UserCartDashboardProfile : AppCompatActivity() {

    private val cartRepository = CartRepository()
    private val authRepository = AuthRepository()

    private lateinit var adapter: UserCartAdapter
    private var allUserCarts: List<Cart> = emptyList()
    private var approvedFilter: Boolean? = null

    // Views
    private lateinit var searchBar: SearchBar
    private lateinit var searchView: SearchView
    private lateinit var spinnerApproved: MaterialAutoCompleteTextView
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_user_cart_dashboard)

        // Init views
        searchBar = findViewById(R.id.searchBarUserOrders)
        searchView = findViewById(R.id.searchUserOrders)
        spinnerApproved = findViewById(R.id.spinnerApprovedUser)
        recycler = findViewById(R.id.recyclerUserOrders)

        adapter = UserCartAdapter(emptyList()) { cart ->
            val intent = Intent(this@UserCartDashboardProfile, DetailOrderUserProfile::class.java)
            intent.putExtra("cart_id", cart.id)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Configure filter options
        val options = arrayOf("Todos", "Aprobado", "Pendiente")
        spinnerApproved.setAdapter(
            android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        )
        spinnerApproved.setOnItemClickListener { _, _, position, _ ->
            approvedFilter = when (position) {
                0 -> null
                1 -> true
                2 -> false
                else -> null
            }
            applyFilters(null)
        }

        // Optional search by text (e.g., by total or id if needed)
        searchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                searchBar.setText("")
                applyFilters(null)
            }
        }
        searchView.editText.setOnEditorActionListener { v, _, _ ->
            val q = v.text?.toString()
            applyFilters(q)
            false
        }

        // Load data for the logged-in user
        loadUserCarts()
    }

    private fun loadUserCarts() {
        lifecycleScope.launch {
            try {
                val me = authRepository.getAuthMe()
                val all = cartRepository.getCarts()
                allUserCarts = all.filter { it.user_id == me.id }
                applyFilters(null)
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    showCustomErrorToast(this@UserCartDashboardProfile, getString(R.string.api_limit_error_retry))
                } else {
                    showCustomErrorToast(this@UserCartDashboardProfile, getString(R.string.http_error_format, getString(R.string.orders_load_error_base), e.code()))
                }
            } catch (e: Exception) {
                showCustomErrorToast(this@UserCartDashboardProfile, getString(R.string.orders_load_error_base) + (e.message?.let { " ($it)" } ?: ""))
            }
        }
    }

    private fun applyFilters(query: String?) {
        val q = query?.trim()?.lowercase().orEmpty()
        val filtered = allUserCarts.filter { c ->
            val matchesApproval = approvedFilter?.let { c.aprobado == it } ?: true
            val matchesQuery = if (q.isEmpty()) true else (c.id.lowercase().contains(q))
            matchesApproval && matchesQuery
        }
        adapter.submit(filtered)
    }
}