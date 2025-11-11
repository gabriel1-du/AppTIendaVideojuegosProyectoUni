package com.example.videojuegosandroidtienda.ui.adminUi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.videojuegosandroidtienda.R

class AdminCenterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_center)

        val buttonUsuarios = findViewById<Button>(R.id.buttonAdminUsuarios)
        val buttonPedidos = findViewById<Button>(R.id.buttonAdminPedidos)
        val buttonVideogames = findViewById<Button>(R.id.buttonAdminVideogames)

        buttonUsuarios.setOnClickListener {
            startActivity(Intent(this, ActivitiyUserAdminDashboard::class.java))
        }
        buttonPedidos.setOnClickListener {
            startActivity(Intent(this, ActivityOrdersAdminDashboard::class.java))
        }
        buttonVideogames.setOnClickListener {
            startActivity(Intent(this, ActivityVideogameAdminDashboard::class.java))
        }
    }
}