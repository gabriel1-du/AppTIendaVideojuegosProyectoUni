package com.example.videojuegosandroidtienda.ui.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.User

class AdminUserAdapter(
    private var items: List<User>,
    private val onVerMasClick: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {

    fun submit(list: List<User>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.usuario_admin_recycleview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textViewUserId.text = "id: ${item.id}"
        holder.textViewUserCreated.text = "creado: ${item.created_at ?: ""}"
        holder.textViewUserName.text = "nombre: ${item.name}"
        holder.textViewUserEmail.text = "email: ${item.email}"
        holder.textViewUserAdmin.text = "admin: ${if (item.admin) "true" else "false"}"
        holder.textViewUserBloqueo.text = "bloqueado: ${if (item.bloqueo) "true" else "false"}"
        val buttonVerMas = holder.itemView.findViewById<android.widget.Button>(R.id.buttonVerMas)
        buttonVerMas.setOnClickListener { onVerMasClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewUserId: TextView = itemView.findViewById(R.id.TextViewUserId)
        val textViewUserCreated: TextView = itemView.findViewById(R.id.TextViewUserCreated)
        val textViewUserName: TextView = itemView.findViewById(R.id.TextViewUserName)
        val textViewUserEmail: TextView = itemView.findViewById(R.id.TextViewUserEmail)
        val textViewUserAdmin: TextView = itemView.findViewById(R.id.TextViewUserAdmin)
        val textViewUserBloqueo: TextView = itemView.findViewById(R.id.TextViewUserBloqueo)
    }
}