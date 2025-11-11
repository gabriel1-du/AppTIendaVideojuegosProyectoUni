package com.example.videojuegosandroidtienda.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartViewModel : ViewModel() {
    private val repository = CartRepository()

    private val cache: MutableMap<String, Cart> = mutableMapOf()

    private val _currentCart = MutableLiveData<Cart?>()
    val currentCart: LiveData<Cart?> = _currentCart

    suspend fun getCart(cartId: String): Cart = withContext(Dispatchers.IO) {
        cache[cartId]?.let {
            _currentCart.postValue(it)
            return@withContext it
        }
        val fetched = repository.getCartById(cartId)
        cache[cartId] = fetched
        _currentCart.postValue(fetched)
        fetched
    }

    suspend fun updateCartApproval(cartId: String, approved: Boolean): Cart = withContext(Dispatchers.IO) {
        val updated = repository.updateCartApproval(cartId, approved)
        cache[cartId] = updated
        _currentCart.postValue(updated)
        updated
    }

    suspend fun listCarts(): List<Cart> = withContext(Dispatchers.IO) {
        val list = repository.getCarts()
        list.forEach { cart -> cart.id?.let { cache[it] = cart } }
        list
    }
}