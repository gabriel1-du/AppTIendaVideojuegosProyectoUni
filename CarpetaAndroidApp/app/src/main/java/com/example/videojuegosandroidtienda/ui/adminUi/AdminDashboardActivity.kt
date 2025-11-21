package com.example.videojuegosandroidtienda.ui.adminUi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.videojuegosandroidtienda.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardActivity : AppCompatActivity() {

    private val userAdminFragment = UserAdminDashboardFragment()
    private val videogameAdminFragment = VideogameAdminDashboardFragment()
    private val ordersAdminFragment = OrdersDashboardFragment()
    private var activeFragment: Fragment = userAdminFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container_admin, ordersAdminFragment, "3").hide(ordersAdminFragment)
            add(R.id.fragment_container_admin, videogameAdminFragment, "2").hide(videogameAdminFragment)
            add(R.id.fragment_container_admin, userAdminFragment, "1")
        }.commit()

        // Set default selection
        bottomNav.selectedItemId = R.id.nav_users

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_users -> {
                    supportFragmentManager.beginTransaction().hide(activeFragment).show(userAdminFragment).commit()
                    activeFragment = userAdminFragment
                    true
                }
                R.id.nav_videogames -> {
                    supportFragmentManager.beginTransaction().hide(activeFragment).show(videogameAdminFragment).commit()
                    activeFragment = videogameAdminFragment
                    true
                }
                R.id.nav_orders -> {
                    supportFragmentManager.beginTransaction().hide(activeFragment).show(ordersAdminFragment).commit()
                    activeFragment = ordersAdminFragment
                    true
                }
                else -> false
            }
        }
    }
}
