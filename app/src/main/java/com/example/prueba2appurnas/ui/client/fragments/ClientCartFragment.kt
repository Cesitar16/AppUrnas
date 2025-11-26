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
import com.example.prueba2appurnas.repository.CartLocalStorage
import com.example.prueba2appurnas.repository.CartRepository
import com.example.prueba2appurnas.ui.client.adapters.CartAdapter
import kotlinx.coroutines.launch

class ClientCartFragment : Fragment() {

    private var _binding: FragmentClientCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter

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
        cartAdapter = CartAdapter()
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
                Toast.makeText(requireContext(), "Tu carrito está vacío", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val response = repo.getItems(cartId)

            binding.progressBar.visibility = View.GONE

            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                cartAdapter.submitList(items)

                val total = items.sumOf { it.unit_price * it.quantity }
                binding.txtTotal.text = "Total: $${String.format("%.2f", total)}"

            } else {
                Toast.makeText(requireContext(), "Error cargando carrito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
