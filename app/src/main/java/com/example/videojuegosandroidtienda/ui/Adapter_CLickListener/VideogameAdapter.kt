package com.example.videojuegosandroidtienda.ui.Adapter_CLickListener

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.entities.Videogame

class VideogameAdapter(
    private var items: List<Videogame> = emptyList(),
    private var genreNames: Map<String, String> = emptyMap(),
    private var platformNames: Map<String, String> = emptyMap()
) : RecyclerView.Adapter<VideogameAdapter.VH>() {

    private var onItemClick: ((Videogame) -> Unit)? = null

    // Define el listener de click para navegar al detalle
    fun setOnItemClickListener(listener: (Videogame) -> Unit) {
        onItemClick = listener
    }

    // Carga items y mapas de nombres y refresca la vista
    fun submit(
        newItems: List<Videogame>,
        genreMap: Map<String, String>,
        platformMap: Map<String, String>
    ) {
        items = newItems
        genreNames = genreMap
        platformNames = platformMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_videogame, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val vg = items[position]
        holder.textTitle.text = vg.title
        holder.textGenre.text = "Género: ${genreNames[vg.genre_id] ?: "-"}"
        holder.textPlatform.text = "Plataforma: ${platformNames[vg.platform_id] ?: "-"}"
        val url = vg.cover_image?.url
        if (!url.isNullOrBlank()) {
            val request = ImageRequest.Builder(holder.imageCover.context)
                .data(url)
                .target(holder.imageCover)
                .build()
            holder.imageCover.context.imageLoader.enqueue(request)
        } else {
            holder.imageCover.setImageResource(android.R.color.darker_gray)
        }
        holder.itemView.setOnClickListener { onItemClick?.invoke(vg) }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCover: ImageView = itemView.findViewById(R.id.imageCover)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textGenre: TextView = itemView.findViewById(R.id.textGenre)
        val textPlatform: TextView = itemView.findViewById(R.id.textPlatform)
    }
}