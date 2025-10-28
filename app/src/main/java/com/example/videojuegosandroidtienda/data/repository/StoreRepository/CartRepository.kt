package com.example.videojuegosandroidtienda.data.repository.StoreRepository

import com.example.videojuegosandroidtienda.data.api.StoreService
import com.example.videojuegosandroidtienda.data.api.CartService
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.example.videojuegosandroidtienda.data.entities.CartItem
import com.example.videojuegosandroidtienda.data.entities.createClasses.cartPost
import com.example.videojuegosandroidtienda.data.network.ApiConfig
import com.example.videojuegosandroidtienda.data.network.RetrofitProvider

class CartRepository {

    private val storeService: StoreService =
        RetrofitProvider.createService(ApiConfig.STORE_BASE_URL, StoreService::class.java)
    private val cartService: CartService =
        RetrofitProvider.createService(ApiConfig.CART_BASE_URL, CartService::class.java)
    // Lista carritos remotos (ejemplo/demo)
    suspend fun getCarts(): List<Cart> = storeService.listCarts()
    // Obtiene detalle de ítem de carrito por id
    suspend fun getCartItem(id: String): CartItem = storeService.getCartItem(id)
    suspend fun getCartById(cartId: String) = cartService.getCartById(cartId)

    suspend fun postCart(cart: Cart, videogamesIds: List<String>) = storeService.createCart(
        cartPost(
            id = cart.id,
            created_at = cart.created_at,
            total = cart.total.toDouble(),
            user_id = cart.user_id.toString(),
            videogames_id = videogamesIds
        )
    )

    suspend fun updateCartApproval(cartId: String, approved: Boolean): com.example.videojuegosandroidtienda.data.api.CartResponse {
        val current = cartService.getCartById(cartId)
        val req = com.example.videojuegosandroidtienda.data.api.CartUpdateRequest(
            id = current.id,
            created_at = current.created_at,
            total = current.total,
            user_id = current.user_id,
            aprobado = approved,
            videogames_id = current.videogames_id
        )
        return cartService.patchCart(cartId, req)
    }

}