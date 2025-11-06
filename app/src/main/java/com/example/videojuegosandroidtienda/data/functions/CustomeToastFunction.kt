package com.example.videojuegosandroidtienda.data.functions

import android.content.Context
import android.widget.Toast

fun showCustomOkToast(context: Context, message: String) {
    Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
}

fun showCustomErrorToast(context: Context, message: String) {
    Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
}
