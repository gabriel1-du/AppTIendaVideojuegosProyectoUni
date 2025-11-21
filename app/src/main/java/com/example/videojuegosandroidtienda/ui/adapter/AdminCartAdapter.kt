package com.example.videojuegosandroidtienda.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.google.android.material.button.MaterialButton

class AdminCartAdapter(
    private var items: List<Cart>,
    private val onVerMasClick: (Cart) -> Unit
) : RecyclerView.Adapter<AdminCartAdapter.ViewHolder>() {

    private var userNames: Map<String, String> = emptyMap()

    fun submit(list: List<Cart>) {
        items = list
        notifyDataSetChanged()
    }

    fun setUserNames(map: Map<String, String>) {
        userNames = map
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carrito_compra_admin_recycleview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val name = userNames[item.user_id] ?: item.user_id
        holder.textViewUserName.text = "Usuario: ${name}"
        holder.textViewTotalCart.text = "Total: ${item.total}"
        holder.textViewCheckStatus.text = "Estado: ${if (item.aprobado == true) "Aprobado" else "Pendiente"}"
        val buttonVerMas = holder.itemView.findViewById<MaterialButton>(R.id.buttonVerMas)
        buttonVerMas.setOnClickListener { onVerMasClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewUserName: TextView = itemView.findViewById(R.id.TextViewUserName)
        val textViewTotalCart: TextView = itemView.findViewById(R.id.TextViewTotalCart)
        val textViewCheckStatus: TextView = itemView.findViewById(R.id.TextViewCheckStatus)
    }
}