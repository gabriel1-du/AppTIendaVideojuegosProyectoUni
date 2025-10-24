package com.example.videojuegosandroidtienda.ui.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Cart

class AdminCartAdapter(
    private var items: List<Cart>
) : RecyclerView.Adapter<AdminCartAdapter.ViewHolder>() {

    fun submit(list: List<Cart>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carrito_compra_admin_recycleview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textViewIdUser.text = item.id
        holder.textViewTotalCart.text = item.total.toString()
        holder.textViewCheckStatus.text = (item.aprobado ?: false).toString()
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewIdUser: TextView = itemView.findViewById(R.id.TextViewIdUser)
        val textViewTotalCart: TextView = itemView.findViewById(R.id.TextViewTotalCart)
        val textViewCheckStatus: TextView = itemView.findViewById(R.id.TextViewCheckStatus)
    }
}