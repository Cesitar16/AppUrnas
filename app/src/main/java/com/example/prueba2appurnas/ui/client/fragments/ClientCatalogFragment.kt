package com.example.prueba2appurnas.ui.client.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.databinding.FragmentClientCatalogBinding
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.repository.CartLocalStorage
import com.example.prueba2appurnas.repository.CartRepository
import com.example.prueba2appurnas.ui.UrnaAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientCatalogFragment : Fragment() {

    private var _binding: FragmentClientCatalogBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UrnaAdapter
    private lateinit var repo: CartRepository
    private var urnasList: List<Urna> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Repositorios
        repo = CartRepository(
            service = RetrofitClient.getCartService(requireContext()),
            orderService = RetrofitClient.getOrderService(requireContext()),
            localStore = CartLocalStorage(requireContext())
        )

        // Setup UI
        setupRecyclerView()
        setupSearch()
        loadUrnas()
    }


    // -------------------------------------------
    // CONFIGURAR LISTA
    // -------------------------------------------
    private fun setupRecyclerView() {
        adapter = UrnaAdapter(emptyList())
        binding.rvUrnasClient.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUrnasClient.adapter = adapter

        adapter.setOnItemClickListener { urna ->
            openDetail(urna)
        }
    }

    // -------------------------------------------
    // OBTENER URNAS DESDE API
    // -------------------------------------------
    private fun loadUrnas() {
        val service = RetrofitClient.getUrnaService(requireContext())

        service.getUrnas().enqueue(object : Callback<List<Urna>> {
            override fun onResponse(call: Call<List<Urna>>, response: Response<List<Urna>>) {
                if (response.isSuccessful) {
                    urnasList = response.body() ?: emptyList()
                    adapter.updateList(urnasList)
                }
            }

            override fun onFailure(call: Call<List<Urna>>, t: Throwable) {
                // Puedes agregar Toast si deseas
            }
        })
    }

    // -------------------------------------------
    // SEARCHBAR
    // -------------------------------------------
    private fun setupSearch() {
        binding.searchUrnasClient.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    // -------------------------------------------
    // NAVEGAR A DETALLE
    // -------------------------------------------
    private fun openDetail(urna: Urna) {
        val fragment = ClientUrnaDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("urna_id", urna.id)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(com.example.prueba2appurnas.R.id.clientFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
