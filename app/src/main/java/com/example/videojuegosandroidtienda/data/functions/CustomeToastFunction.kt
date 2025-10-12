package com.example.videojuegosandroidtienda.data.functions

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.videojuegosandroidtienda.R

fun showCustomOkToast(context: Context, message: String) { //Funcion para mostrar que esta funcionando
    val inflater = LayoutInflater.from(context)
    val layout = inflater.inflate(R.layout.custom_toast_ok, null)
    val textView = layout.findViewById<TextView>(R.id.toast_text)
    textView.text = message
    with(Toast(context.applicationContext)) {
        duration = Toast.LENGTH_LONG
        view = layout
        show()
    }
}

fun showCustomErrorToast(context: Context, message: String) { //funcion que ojola el
    val inflater = LayoutInflater.from(context)
    val layout = inflater.inflate(R.layout.custom_toast_error, null)
    val textView = layout.findViewById<TextView>(R.id.toast_text)
    textView.text = message
    with(Toast(context.applicationContext)) {
        duration = Toast.LENGTH_LONG
        view = layout
        show()
    }
}
