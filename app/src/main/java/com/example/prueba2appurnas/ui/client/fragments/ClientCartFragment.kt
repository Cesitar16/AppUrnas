package com.example.prueba2appurnas.ui.client.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.databinding.FragmentClientCartBinding
import com.example.prueba2appurnas.model.CartItem
import com.example.prueba2appurnas.model.UpdateCartItemRequest
import com.example.prueba2appurnas.repository.CartLocalStorage
import com.example.prueba2appurnas.repository.CartRepository
import com.example.prueba2appurnas.ui.client.adapters.CartAdapter
import kotlinx.coroutines.launch

class ClientCartFragment : Fragment() {

    private var _binding: FragmentClientCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private var lastItems: List<CartItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        loadCartItems()

        binding.swipeRefresh.setOnRefreshListener {
            loadCartItems()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupRecycler() {
        cartAdapter = CartAdapter(
            items = mutableListOf(),
            onIncrease = { item -> updateCartItemQuantity(item, item.quantity + 1) },
            onDecrease = { item -> if (item.quantity > 1) updateCartItemQuantity(item, item.quantity - 1) },
            onDelete = { item -> deleteCartItem(item.id) }
        )

        binding.recyclerCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun loadCartItems() {
        val cartService = RetrofitClient.getCartService(requireContext())
        val localStore = CartLocalStorage(requireContext())
        val repo = CartRepository(cartService, localStore)

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val cartId = localStore.getCartId()

            if (cartId == null) {
                binding.progressBar.visibility = View.GONE
                cartAdapter.updateItems(emptyList())
                binding.txtTotal.text = "Total: $0.00"
                Toast.makeText(requireContext(), "Tu carrito está vacío", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val response = repo.getItems(cartId)
            binding.progressBar.visibility = View.GONE

            if (response.isSuccessful) {
                val items = response.body().orEmpty()
                lastItems = items
                cartAdapter.updateItems(items)
                updateTotal(items)
            } else {
                Toast.makeText(requireContext(), "Error cargando carrito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTotal(items: List<CartItem>) {
        val total = items.sumOf { (it.unit_price ?: 0.0) * (it.quantity ?: 0) }
        binding.txtTotal.text = "Total: $${String.format("%.2f", total)}"
    }

    private fun updateCartItemQuantity(item: CartItem, newQty: Int) {
        // Llamar al PATCH suspend (sin enqueue)  :contentReference[oaicite:2]{index=2}
        val service = RetrofitClient.getCartService(requireContext())

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val req = UpdateCartItemRequest(quantity = newQty) // sin unit_price  :contentReference[oaicite:3]{index=3}
                val resp = service.updateCartItem(itemId = item.id, request = req)
                if (resp.isSuccessful) {
                    // recargar lista para reflejar qty y total
                    loadCartItems()
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "No se pudo actualizar la cantidad", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCartItem(itemId: Int) {
        val service = RetrofitClient.getCartService(requireContext())

        lifecycleScope.launch {
            try {
                val response = service.deleteCartItem(itemId)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Item eliminado", Toast.LENGTH_SHORT).show()
                    loadCartItems()
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
