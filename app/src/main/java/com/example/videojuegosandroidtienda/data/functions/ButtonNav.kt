package com.example.videojuegosandroidtienda.data.functions

import android.app.Activity
import android.content.Intent
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.ui.cart.CartActivity
import com.example.videojuegosandroidtienda.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun setupBottomNavigation(
    activity: Activity,
    bottomNav: BottomNavigationView,
    selectedItemId: Int
) {
    bottomNav.selectedItemId = selectedItemId

    bottomNav.setOnItemSelectedListener { item ->
        // Evita recargar la actividad si el usuario presiona el ítem ya activo
        if (item.itemId == selectedItemId) {
            return@setOnItemSelectedListener true
        }

        val intent = when (item.itemId) {
            R.id.nav_search -> Intent(activity, MainActivity::class.java)
            R.id.nav_cart -> Intent(activity, CartActivity::class.java)
            R.id.nav_profile -> Intent(activity, ProfileActivity::class.java)
            else -> null
        }

        intent?.let {
            activity.startActivity(it)
            activity.finish()
        }
        
        // Devuelve true para indicar que el evento ha sido manejado
        intent != null
    }
}
