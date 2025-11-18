package com.example.videojuegosandroidtienda

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.data.entities.User
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.databinding.ActivityMainBinding
import com.example.videojuegosandroidtienda.ui.auth.LoginActivity
import com.example.videojuegosandroidtienda.ui.fragments.CartFragment
import com.example.videojuegosandroidtienda.ui.fragments.HomeFragment
import com.example.videojuegosandroidtienda.ui.fragments.ProfileFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private var currentUser: User? = null

    private val homeFragment = HomeFragment()
    private val cartFragment = CartFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Set HomeFragment as the default
        setCurrentFragment(homeFragment)

        // Bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> setCurrentFragment(homeFragment)
                R.id.nav_cart -> setCurrentFragment(cartFragment)
                R.id.nav_profile -> setCurrentFragment(profileFragment)
            }
            true
        }

        // Listener de clics del menú de la toolbar
        binding.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    true
                }
                R.id.action_upload_videogame -> {
                    startActivity(Intent(this, com.example.videojuegosandroidtienda.ui.upload.AddVideogameActivity::class.java))
                    true
                }
                R.id.action_cart -> {
                    setCurrentFragment(cartFragment)
                    // Make sure the bottom nav is updated
                    binding.bottomNav.selectedItemId = R.id.nav_cart
                    true
                }
                R.id.action_refresh -> {
                    showCustomOkToast(this, "Refrescando...")
                    // The fragment should handle its own refresh
                    val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (current is HomeFragment) {
                        current.loadInitialData()
                    }
                    true
                }
                else -> false
            }
        }
        // Cargar token persistido y ajustar iconos según estado inicial
        authRepository.loadPersistedToken()
        updateUiBasedOnAuthState()
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUiBasedOnAuthState()

        // If there is a session, check if the user is blocked (full GET) and log out automatically
        lifecycleScope.launch {
            try {
                val token = com.example.videojuegosandroidtienda.data.network.TokenStore.token
                if (!token.isNullOrBlank()) {
                    val me = authRepository.getAuthMe()
                    val fetched = userRepository.getUser(me.id)
                    if (fetched.bloqueo) {
                        authRepository.logout()
                        showCustomErrorToast(this@MainActivity, "Usuario bloqueado")
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                        return@launch
                    }
                }
            } catch (_: Exception) {
                // Ignore transient errors
            }
        }
    }

    private fun updateUiBasedOnAuthState() {
        val token = com.example.videojuegosandroidtienda.data.network.TokenStore.token
        val isLoggedIn = !token.isNullOrBlank()

        binding.toolbar.menu.findItem(R.id.action_login)?.isVisible = !isLoggedIn
        binding.toolbar.menu.findItem(R.id.action_cart)?.isVisible = isLoggedIn

        // Update visibility of "upload videogame" based on user role
        lifecycleScope.launch {
            try {
                val uploadItem = binding.toolbar.menu.findItem(R.id.action_upload_videogame)
                if (!isLoggedIn) {
                    uploadItem?.isVisible = false
                    currentUser = null
                } else {
                    // Get authenticated user and then their data by ID
                    val authUser = authRepository.getAuthMe()
                    val fetchedUser = userRepository.getUser(authUser.id)
                    currentUser = fetchedUser
                    uploadItem?.isVisible = fetchedUser.admin
                }
            } catch (_: Exception) {
                binding.toolbar.menu.findItem(R.id.action_upload_videogame)?.isVisible = false
            }
        }
    }
}
