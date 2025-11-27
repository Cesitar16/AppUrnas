package com.example.prueba2appurnas.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.databinding.FragmentAdminOrdersBinding
import com.example.prueba2appurnas.model.OrderResponse
import com.example.prueba2appurnas.model.UpdateOrderStatusRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AdminOrdersFragment : Fragment() {

    private var _binding: FragmentAdminOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminOrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminOrderAdapter(
            onApprove = { order -> updateOrderStatus(order.id, "APPROVED") },
            onReject = { order -> updateOrderStatus(order.id, "REJECTED") }
        )

        binding.rvOrdersAdmin.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrdersAdmin.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        val service = RetrofitClient.getOrderService(requireContext())

        service.getAllOrders().enqueue(object : Callback<List<OrderResponse>> {
            override fun onResponse(
                call: Call<List<OrderResponse>>,
                response: Response<List<OrderResponse>>
            ) {
                if (response.isSuccessful) {
                    adapter.submitList(response.body() ?: emptyList())
                }
            }

            override fun onFailure(call: Call<List<OrderResponse>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error cargando órdenes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateOrderStatus(orderId: Int, status: String) {
        val service = RetrofitClient.getOrderService(requireContext())

        val body = UpdateOrderStatusRequest(
            status = status,
            updated_at = System.currentTimeMillis()
        )

        // Usar corrutina
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = service.updateOrderStatus(orderId, body)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Estado actualizado", Toast.LENGTH_SHORT).show()
                    loadOrders()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de red o servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
