package com.example.videojuegosandroidtienda.data.cart

import com.example.videojuegosandroidtienda.data.entities.CartProduct

object CartManager {
    private val items: MutableMap<String, Pair<CartProduct, Int>> = mutableMapOf()

    // Agrega producto al carrito o incrementa cantidad
    fun add(product: CartProduct, quantity: Int = 1) {
        val current = items[product.id]?.second ?: 0
        items[product.id] = product to (current + quantity)
    }


    // Incrementa cantidad del producto
    fun increase(productId: String) {
        val pair = items[productId] ?: return
        items[productId] = pair.first to (pair.second + 1)
    }

    // Reduce cantidad y elimina si llega a cero
    fun decrease(productId: String) {
        val pair = items[productId] ?: return
        val newQty = (pair.second - 1).coerceAtLeast(0)
        if (newQty <= 0) items.remove(productId) else items[productId] = pair.first to newQty
    }

    // Fija cantidad explícita (elimina si es 0)


    // Devuelve lista de productos con sus cantidades
    fun getItems(): List<Pair<CartProduct, Int>> = items.values.toList()

    // Calcula el total del carrito
    fun getTotal(): Double = items.values.sumOf { it.first.price * it.second }

    // Limpia completamente el carrito
    fun clear() { items.clear() }
}