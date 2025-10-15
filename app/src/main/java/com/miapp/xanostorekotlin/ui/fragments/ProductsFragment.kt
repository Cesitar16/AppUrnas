package com.miapp.xanostorekotlin.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.FragmentProductsBinding
import com.miapp.xanostorekotlin.model.Product
import com.miapp.xanostorekotlin.ui.ProductDetailActivity
import com.miapp.xanostorekotlin.ui.adapter.ProductAdapter
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private val productAdapter by lazy {
        ProductAdapter { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
            startActivity(intent)
        }
    }
    private val productService by lazy { RetrofitClient.createProductService(requireContext()) }
    private var fullProductList: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = productAdapter

        binding.swipeRefresh.setOnRefreshListener { fetchProducts() }
        binding.retryButton.setOnClickListener { fetchProducts() }
        binding.searchInput.doAfterTextChanged { query ->
            filterProducts(query?.toString().orEmpty())
        }

        if (fullProductList.isEmpty()) {
            fetchProducts()
        }
    }

    private fun fetchProducts() {
        if (binding.swipeRefresh.isRefreshing.not()) {
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.errorGroup.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val products = productService.getProducts()
                fullProductList = products
                productAdapter.submitList(products)
                binding.emptyView.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            } catch (ex: Exception) {
                handleError(ex)
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun filterProducts(query: String) {
        if (query.isBlank()) {
            productAdapter.submitList(fullProductList)
            binding.emptyView.visibility = if (fullProductList.isEmpty()) View.VISIBLE else View.GONE
            return
        }

        val filtered = fullProductList.filter {
            it.name.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
        }
        productAdapter.submitList(filtered)
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun handleError(ex: Exception) {
        binding.errorGroup.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.errorMessage.text = when (ex) {
            is HttpException -> "Error ${ex.code()} al cargar productos"
            else -> ex.localizedMessage ?: "Error desconocido"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProductsFragment"
        fun newInstance(): Pair<String, ProductsFragment> = TAG to ProductsFragment()
    }
}
