package com.example.videojuegosandroidtienda.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Videogame

class AdminVideogameAdapter(
    private var items: List<Videogame>,
    private val onVerMasClick: (Videogame) -> Unit
) : RecyclerView.Adapter<AdminVideogameAdapter.ViewHolder>() {

    fun submit(list: List<Videogame>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.videogame_admin_recycleview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textViewId.text = "id: ${item.id}"
        holder.textViewTitle.text = "title: ${item.title}"
        holder.textViewPrice.text = "price: ${item.price.toInt()}"
        holder.textViewPlatformId.text = "platform_id: ${item.platform_id}"
        holder.textViewGenreId.text = "genre_id: ${item.genre_id}"
        val buttonVerMas = holder.itemView.findViewById<android.widget.Button>(R.id.buttonVerMas)
        buttonVerMas.setOnClickListener { onVerMasClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewId: TextView = itemView.findViewById(R.id.TextViewVideogameId)
        val textViewTitle: TextView = itemView.findViewById(R.id.TextViewVideogameTitle)
        val textViewPrice: TextView = itemView.findViewById(R.id.TextViewVideogamePrice)
        val textViewPlatformId: TextView = itemView.findViewById(R.id.TextViewVideogamePlatformId)
        val textViewGenreId: TextView = itemView.findViewById(R.id.TextViewVideogameGenreId)
    }
}