package com.example.videojuegosandroidtienda.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.google.android.material.button.MaterialButton
import androidx.core.content.ContextCompat

class UserCartAdapter(
    private var items: List<Cart>,
    private val onVerMasClick: (Cart) -> Unit
) : RecyclerView.Adapter<UserCartAdapter.ViewHolder>() {

    fun submit(list: List<Cart>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_user_recycleview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.total.text = holder.itemView.context.getString(R.string.cart_total_format, item.total)
        val statusText = if (item.aprobado == true) {
            holder.itemView.context.getString(R.string.status_approved)
        } else {
            holder.itemView.context.getString(R.string.status_pending)
        }
        holder.status.text = holder.itemView.context.getString(R.string.cart_status_format, statusText)
        val statusColorRes = if (item.aprobado == true) R.color.statusApproved else R.color.statusPending
        holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, statusColorRes))

        holder.verMas.setOnClickListener { onVerMasClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val total: TextView = itemView.findViewById(R.id.TextViewTotalCart)
        val status: TextView = itemView.findViewById(R.id.TextViewCheckStatus)
        val verMas: MaterialButton = itemView.findViewById(R.id.buttonVerMas)
    }
}