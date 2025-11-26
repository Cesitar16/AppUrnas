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
import com.example.prueba2appurnas.model.OrderItemRequest
import com.example.prueba2appurnas.model.OrderRequest
import com.example.prueba2appurnas.model.UpdateCartItemRequest
import com.example.prueba2appurnas.repository.CartLocalStorage
import com.example.prueba2appurnas.repository.CartRepository
import com.example.prueba2appurnas.ui.client.adapters.CartAdapter
import kotlinx.coroutines.launch

class ClientCartFragment : Fragment() {

    private var _binding: FragmentClientCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private lateinit var repo: CartRepository

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

        val cartService = RetrofitClient.getCartService(requireContext())
        val orderService = RetrofitClient.getOrderService(requireContext())
        val localStore = CartLocalStorage(requireContext())

        repo = CartRepository(
            service = cartService,
            orderService = orderService,
            localStore = localStore
        )

        setupRecycler()
        loadCartItems()

        binding.swipeRefresh.setOnRefreshListener {
            loadCartItems()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnCheckout.setOnClickListener { checkout() }
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
        val localStore = CartLocalStorage(requireContext())
        val cartId = localStore.getCartId()

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
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
        val service = RetrofitClient.getCartService(requireContext())

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val req = UpdateCartItemRequest(quantity = newQty)
                val resp = service.updateCartItem(item.id, req)

                if (resp.isSuccessful) {
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
                val resp = service.deleteCartItem(itemId)

                if (resp.isSuccessful) {
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

    // ============================================================
    // 🟩 CREAR ORDEN (CHECKOUT)
    // ============================================================
    private fun checkout() {

        lifecycleScope.launch {
            try {
                val cartId = CartLocalStorage(requireContext()).getCartId()

                if (cartId == null) {
                    Toast.makeText(context, "Carrito vacío", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val subtotal = lastItems.sumOf { (it.unit_price ?: 0.0) * (it.quantity ?: 0) }

                val orderReq = OrderRequest(
                    items_total = subtotal,
                    discount_total = 0.0,
                    grand_total = subtotal,
                    total = subtotal,
                    status = "PENDING",
                    shipping_full_name = "",
                    shipping_phone = "",
                    shipping_line1 = "",
                    shipping_line2 = "",
                    shipping_city = "",
                    shipping_state = "",
                    shipping_postal_code = "",
                    shipping_country = "",
                    promotion_code = "",
                    updated_at = System.currentTimeMillis(),
                    user_id = 1,
                    cart_id = cartId,
                    promotion_id = 0
                )

                val resp = repo.createOrder(orderReq)

                if (!resp.isSuccessful) {
                    Toast.makeText(context, "Error creando orden", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val order = resp.body()
                if (order == null) {
                    Toast.makeText(context, "Orden nula", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Crear order_items
                lastItems.forEach { cartItem ->
                    repo.createOrderItem(
                        OrderItemRequest(
                            order_id = order.id,
                            urn_id = cartItem.urn_id ?: 0,
                            quantity = cartItem.quantity ?: 1,
                            unit_price = cartItem.unit_price ?: 0.0,
                            subtotal = (cartItem.quantity ?: 1) * (cartItem.unit_price ?: 0.0)
                        )
                    )
                }

                Toast.makeText(context, "Orden creada correctamente", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
