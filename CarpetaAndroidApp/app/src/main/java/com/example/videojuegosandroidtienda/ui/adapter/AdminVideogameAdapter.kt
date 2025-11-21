package com.example.videojuegosandroidtienda.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Videogame
import com.google.android.material.button.MaterialButton

class AdminVideogameAdapter(
    private var items: List<Videogame>,
    private val onVerMasClick: (Videogame) -> Unit
) : RecyclerView.Adapter<AdminVideogameAdapter.ViewHolder>() {

    private var platformNamesMap: Map<String, String> = emptyMap()
    private var genreNamesMap: Map<String, String> = emptyMap()

    fun submit(
        list: List<Videogame>,
        platformMap: Map<String, String>,
        genreMap: Map<String, String>
    ) {
        items = list
        platformNamesMap = platformMap
        genreNamesMap = genreMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.videogame_admin_recycleview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.load(item.cover_image?.url)
        holder.textViewTitle.text = item.title
        holder.textViewPrice.text = "$${item.price}"
        holder.textViewPlatform.text = platformNamesMap[item.platform_id] ?: "N/A"
        holder.textViewGenre.text = genreNamesMap[item.genre_id] ?: "N/A"
        
        holder.itemView.findViewById<MaterialButton>(R.id.buttonVerMas).setOnClickListener { onVerMasClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewTitle: TextView = itemView.findViewById(R.id.TextViewVideogameTitle)
        val textViewPrice: TextView = itemView.findViewById(R.id.TextViewVideogamePrice)
        val textViewPlatform: TextView = itemView.findViewById(R.id.TextViewVideogamePlatformId)
        val textViewGenre: TextView = itemView.findViewById(R.id.TextViewVideogameGenreId)
    }
}