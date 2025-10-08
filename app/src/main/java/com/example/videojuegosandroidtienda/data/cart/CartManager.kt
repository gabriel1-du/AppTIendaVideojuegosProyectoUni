package com.example.videojuegosandroidtienda.data.cart

import com.example.videojuegosandroidtienda.data.entities.CartProduct

object CartManager {
    private val items: MutableMap<String, Pair<CartProduct, Int>> = mutableMapOf()

    fun add(product: CartProduct, quantity: Int = 1) {
        val current = items[product.id]?.second ?: 0
        items[product.id] = product to (current + quantity)
    }

    fun remove(productId: String) {
        items.remove(productId)
    }

    fun increase(productId: String) {
        val pair = items[productId] ?: return
        items[productId] = pair.first to (pair.second + 1)
    }

    fun decrease(productId: String) {
        val pair = items[productId] ?: return
        val newQty = (pair.second - 1).coerceAtLeast(0)
        if (newQty <= 0) items.remove(productId) else items[productId] = pair.first to newQty
    }

    fun setQuantity(productId: String, quantity: Int) {
        val pair = items[productId] ?: return
        val q = quantity.coerceAtLeast(0)
        if (q <= 0) items.remove(productId) else items[productId] = pair.first to q
    }

    fun getItems(): List<Pair<CartProduct, Int>> = items.values.toList()

    fun getTotal(): Double = items.values.sumOf { it.first.price * it.second }

    fun isEmpty(): Boolean = items.isEmpty()

    fun clear() { items.clear() }
}