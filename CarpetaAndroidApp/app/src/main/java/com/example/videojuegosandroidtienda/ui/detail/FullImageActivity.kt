package com.example.videojuegosandroidtienda.ui.detail

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.R

class FullImageActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_full_image)

        val imageView = findViewById<ImageView>(R.id.fullImageView)
        val url = intent.getStringExtra(EXTRA_IMAGE_URL)

        if (!url.isNullOrBlank()) {
            val req = ImageRequest.Builder(this)
                .data(url)
                .target(imageView)
                .build()
            imageLoader.enqueue(req)
        } else {
            imageView.setImageResource(android.R.color.darker_gray)
        }

        // Tap to close
        imageView.setOnClickListener { finish() }
    }
}