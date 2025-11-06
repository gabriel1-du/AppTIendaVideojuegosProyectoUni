package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminUserAdapter
import kotlinx.coroutines.launch

class ActivitiyUserAdminDashboard : AppCompatActivity() {
    private val repository = UserRepository()
    private var allUsers: List<User> = emptyList()
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_admin_dashboard)


        val search = findViewById<SearchView>(R.id.searchUsers)
        val switchOrder = findViewById<android.widget.Switch>(R.id.switchOrder)
        val recycler = findViewById<RecyclerView>(R.id.recyclerUsers)

        adapter = AdminUserAdapter(emptyList()) { user ->
            val intent = android.content.Intent(this, UserDetailActivity::class.java)
            intent.putExtra("user_id", user.id)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        lifecycleScope.launch {
            try {
                allUsers = repository.listUsers()
                render()
            } catch (_: Exception) {
            }
        }

        search.queryHint = "Buscar usuario por nombre"
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                render()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                render()
                return true
            }
        })

        switchOrder.setOnCheckedChangeListener { _, _ -> render() }
    }

    private fun render() {
        val search = findViewById<SearchView>(R.id.searchUsers)
        val switchOrder = findViewById<android.widget.Switch>(R.id.switchOrder)
        val q = search.query?.toString()?.trim()?.lowercase().orEmpty()
        var list = if (q.isEmpty()) allUsers else allUsers.filter { it.name.lowercase().contains(q) }
        list = if (switchOrder.isChecked) {
            // checked: más nuevo → más viejo
            list.sortedByDescending { it.created_at?.toLongOrNull() ?: Long.MIN_VALUE }
        } else {
            // unchecked: más viejo → más nuevo
            list.sortedBy { it.created_at?.toLongOrNull() ?: Long.MAX_VALUE }
        }
        adapter.submit(list)
    }
}