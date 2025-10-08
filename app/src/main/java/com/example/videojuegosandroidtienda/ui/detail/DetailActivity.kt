package com.example.videojuegosandroidtienda.ui.detail

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.R

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        val image = findViewById<ImageView>(R.id.detailImage)
        val title = findViewById<TextView>(R.id.detailTitle)
        val genre = findViewById<TextView>(R.id.detailGenre)
        val platform = findViewById<TextView>(R.id.detailPlatform)
        val price = findViewById<TextView>(R.id.detailPrice)
        val description = findViewById<TextView>(R.id.detailDescription)

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val titleText = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val genreText = intent.getStringExtra(EXTRA_GENRE_NAME).orEmpty()
        val platformText = intent.getStringExtra(EXTRA_PLATFORM_NAME).orEmpty()
        val priceValue = intent.getDoubleExtra(EXTRA_PRICE, 0.0)
        val descText = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()

        if (!imageUrl.isNullOrBlank()) {
            val request = ImageRequest.Builder(this)
                .data(imageUrl)
                .target(image)
                .build()
            image.context.imageLoader.enqueue(request)
        } else {
            image.setImageResource(android.R.color.darker_gray)
        }

        title.text = titleText
        genre.text = "Género: $genreText"
        platform.text = "Plataforma: $platformText"
        price.text = "Precio: $priceValue"
        description.text = "Descripción: $descText"
    }

    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_GENRE_NAME = "extra_genre_name"
        const val EXTRA_PLATFORM_NAME = "extra_platform_name"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_DESCRIPTION = "extra_description"
    }
}