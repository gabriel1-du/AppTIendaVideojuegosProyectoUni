package com.example.videojuegosandroidtienda.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.cart.CartManager
import com.example.videojuegosandroidtienda.data.entities.Cart
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.CartRepository
import com.example.videojuegosandroidtienda.databinding.FragmentCartBinding
import com.example.videojuegosandroidtienda.ui.auth.LoginFragment
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartRepository = CartRepository()
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        renderCart()

        binding.buttonPay.setOnClickListener {
            if (CartManager.getItems().isEmpty()) {
                showCustomErrorToast(requireContext(), "No hay productos en el carrito")
            } else {
                val token = TokenStore.token
                if (token.isNullOrBlank()) {
                    showCustomErrorToast(requireContext(), "Debes iniciar sesión para pagar")
                    startActivity(Intent(requireContext(), LoginFragment::class.java))
                    return@setOnClickListener
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val user = authRepository.getAuthMe()
                        val userId = user.id

                        val videogamesIds = CartManager.getItems()
                            .map { it.first.id }
                            .distinct()

                        val cart = Cart(
                            id = "",
                            user_id = userId,
                            total = CartManager.getTotal(),
                            created_at = System.currentTimeMillis()
                        )
                        cartRepository.postCart(cart, videogamesIds)

                        CartManager.clear()
                        showCustomOkToast(requireContext(), "Se ha realizado exitosamente la compra")

                        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        activity?.finish()
                    } catch (e: Exception) {
                        if (e is HttpException && e.code() == 429) {
                            showCustomErrorToast(requireContext(), "Límite de API alcanzado. Espera ~20s e intenta de nuevo")
                        } else {
                            showCustomErrorToast(requireContext(), "Error al procesar la compra: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private fun renderCart() {
        binding.cartItemsContainer.removeAllViews()
        val items = CartManager.getItems()
        if (items.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No hay productos en el carrito"
                setTextColor(resources.getColor(R.color.textSecondary, requireActivity().theme))
            }
            binding.cartItemsContainer.addView(tv)
        } else {
            items.forEach { (product, qty) ->
                val view = layoutInflater.inflate(R.layout.view_cart_product, binding.cartItemsContainer, false)
                val image = view.findViewById<android.widget.ImageView>(R.id.imageProduct)
                val title = view.findViewById<TextView>(R.id.textTitle)
                val price = view.findViewById<TextView>(R.id.textPrice)
                val quantity = view.findViewById<TextView>(R.id.textQuantity)
                val minus = view.findViewById<android.widget.Button>(R.id.buttonMinus)
                val plus = view.findViewById<View>(R.id.buttonPlus)


                title.text = product.title
                price.text = "Precio: ${product.price}"
                quantity.text = qty.toString()

                val url = product.imageUrl
                if (!url.isNullOrBlank()) {
                    val req = ImageRequest.Builder(requireContext())
                        .data(url)
                        .target(image)
                        .build()
                    image.context.imageLoader.enqueue(req)
                } else {
                    image.setImageResource(android.R.color.darker_gray)
                }

                minus.setOnClickListener {
                    CartManager.decrease(product.id)
                    updateTotal()
                    renderCart()
                }
                plus.setOnClickListener {
                    CartManager.increase(product.id)
                    updateTotal()
                    renderCart()
                }

                binding.cartItemsContainer.addView(view)
            }
        }
        updateTotal()
    }

    private fun updateTotal() {
        binding.cartTotal.text = "Total acumulado: ${CartManager.getTotal()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
