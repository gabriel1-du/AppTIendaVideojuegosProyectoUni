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
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminCartAdapter
import kotlinx.coroutines.launch

class ActivityOrdersAdminDashboard : AppCompatActivity() {
    private val cartRepository = CartRepository()
    private val userRepository = UserRepository()
    private lateinit var adapter: AdminCartAdapter
    private var all: List<Cart> = emptyList()
    private var userNames: Map<String, String> = emptyMap()
    private var approvedFilter: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_orders_admin_dashboard)

        val search = findViewById<SearchView>(R.id.searchOrdersByUser)
        val spinnerApproved = findViewById<android.widget.Spinner>(R.id.spinnerApproved)
        val recycler = findViewById<RecyclerView>(R.id.recyclerOrders)

        adapter = AdminCartAdapter(emptyList()) { cart ->
            val intent = android.content.Intent(this, CartDetailActivity::class.java)
            intent.putExtra("cart_id", cart.id)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        lifecycleScope.launch {
            try {
                val carts = cartRepository.getCarts()
                all = carts
                val users = userRepository.listUsers()
                userNames = users.associate { it.id to it.name }

                val items = listOf("Todos", "Aprobados", "Pendientes")
                val sAdapter = ArrayAdapter(this@ActivityOrdersAdminDashboard, android.R.layout.simple_spinner_item, items).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerApproved.adapter = sAdapter
                spinnerApproved.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                        approvedFilter = when (position) {
                            1 -> true
                            2 -> false
                            else -> null
                        }
                        render()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                render()
            } catch (_: Exception) { }
        }

        search.queryHint = "Buscar por usuario"
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { render(); return true }
            override fun onQueryTextChange(newText: String?): Boolean { render(); return true }
        })
    }

    private fun render() {
        val search = findViewById<SearchView>(R.id.searchOrdersByUser)
        val q = search.query?.toString()?.trim()?.lowercase().orEmpty()
        var list = all
        approvedFilter?.let { ap -> list = list.filter { (it.aprobado ?: false) == ap } }
        if (q.isNotEmpty()) {
            list = list.filter { (userNames[it.user_id]?.lowercase() ?: "").contains(q) }
        }
        adapter.submit(list)
    }
}