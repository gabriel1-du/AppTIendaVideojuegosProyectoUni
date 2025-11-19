package com.example.videojuegosandroidtienda.ui.adminUi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.ui.adapter.AdminUserAdapter
import kotlinx.coroutines.launch

class UserAdminDashboardFragment : Fragment() {
    private val repository = UserRepository()
    private var allUsers: List<User> = emptyList()
    private lateinit var adapter: AdminUserAdapter
    private lateinit var searchView: SearchView
    private lateinit var switchOrder: android.widget.Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.searchUsers)
        switchOrder = view.findViewById(R.id.switchOrder)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerUsers)

        adapter = AdminUserAdapter(emptyList()) { user ->
            val intent = Intent(requireContext(), UserDetailActivity::class.java)
            intent.putExtra("user_id", user.id)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        lifecycleScope.launch {
            try {
                allUsers = repository.listUsers()
                render()
            } catch (_: Exception) {
            }
        }

        searchView.queryHint = "Buscar usuario por nombre"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        val q = searchView.query?.toString()?.trim()?.lowercase().orEmpty()
        var list = if (q.isEmpty()) allUsers else allUsers.filter { it.name.lowercase().contains(q) }
        list = if (switchOrder.isChecked) {
            list.sortedByDescending { it.created_at?.toLongOrNull() ?: Long.MIN_VALUE }
        } else {
            list.sortedBy { it.created_at?.toLongOrNull() ?: Long.MAX_VALUE }
        }
        adapter.submit(list)
    }
}
