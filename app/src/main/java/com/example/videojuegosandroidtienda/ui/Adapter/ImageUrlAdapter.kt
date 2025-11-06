package com.example.videojuegosandroidtienda.ui.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.R

class ImageUrlAdapter(
    private var urls: List<String> = emptyList(),
    private val onImageClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<ImageUrlAdapter.VH>() {

    fun submit(newUrls: List<String>) {
        urls = newUrls
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_image, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = urls[position]
        if (url.isNotBlank()) {
            val req = ImageRequest.Builder(holder.image.context)
                .data(url)
                .target(holder.image)
                .build()
            holder.image.context.imageLoader.enqueue(req)
        } else {
            holder.image.setImageResource(android.R.color.darker_gray)
        }

        holder.itemView.setOnClickListener {
            if (!url.isNullOrBlank()) {
                onImageClick?.invoke(url)
            }
        }
    }

    override fun getItemCount(): Int = urls.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageThumb)
    }
}