package com.example.prueba2appurnas.ui.client.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.FragmentClientUrnaDetailBinding
import com.example.prueba2appurnas.model.AddToCartRequest
import com.example.prueba2appurnas.model.CartItem
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.model.UrnaImage
import com.example.prueba2appurnas.ui.UrnaImageAdapter
import com.example.prueba2appurnas.util.NetUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientUrnaDetailFragment : Fragment() {

    private var _binding: FragmentClientUrnaDetailBinding? = null
    private val binding get() = _binding!!

    private var urnaId: Int = 0
    private lateinit var tokenManager: TokenManager
    private var urnaObject: Urna? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        urnaId = arguments?.getInt("urna_id") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientUrnaDetailBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMiniaturesRecycler()
        loadUrnaData()
        loadImages()

        binding.btnAddToCartClient.setOnClickListener { addToCart() }
    }

    private fun setupMiniaturesRecycler() {
        binding.recyclerUrnaImagesClient.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadUrnaData() {
        val api = RetrofitClient.getUrnaService(requireContext())

        api.getUrnaById(urnaId).enqueue(object : Callback<Urna> {
            override fun onResponse(call: Call<Urna>, response: Response<Urna>) {
                if (response.isSuccessful) {
                    urnaObject = response.body()
                    urnaObject?.let { bindData(it) }
                } else {
                    Toast.makeText(requireContext(), "Error cargando urna", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Urna>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindData(urna: Urna) {

        binding.txtUrnaNameClient.text = urna.name ?: "-"
        binding.txtUrnaPriceClient.text = "$${urna.price ?: 0.0}"

        binding.txtUrnaMaterialClient.text = "Material: ${urna.material_id ?: "-"}"
        binding.txtUrnaColorClient.text = "Color: ${urna.color_id ?: "-"}"
        binding.txtUrnaCapacityClient.text = "Modelo: ${urna.model_id ?: "-"}"

        binding.txtUrnaDescriptionClient.text =
            urna.short_description ?: urna.detailed_description ?: "-"

        // Cargar imagen principal
        val mainUrl = urna.image_url?.url

        if (!mainUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(mainUrl)
                .centerCrop()
                .into(binding.imgUrnaMainClient)
        }
    }

    private fun loadImages() {
        val api = RetrofitClient.getUrnaImageService(requireContext())

        api.getImagesByUrnaId(urnaId).enqueue(object : Callback<List<UrnaImage>> {
            override fun onResponse(
                call: Call<List<UrnaImage>>,
                response: Response<List<UrnaImage>>
            ) {
                if (!response.isSuccessful) {
                    Toast.makeText(requireContext(), "Error cargando imágenes", Toast.LENGTH_SHORT).show()
                    return
                }

                // 🔥 FILTRAR REALMENTE POR ID DE URNA
                val images = response.body()
                    ?.filter { it.urna_id == urnaId }
                    ?: emptyList()

                // Adaptador
                val adapter = UrnaImageAdapter(images) { imageUrl ->
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .centerCrop()
                        .into(binding.imgUrnaMainClient)
                }

                binding.recyclerUrnaImagesClient.adapter = adapter
            }

            override fun onFailure(call: Call<List<UrnaImage>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error cargando imágenes", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun addToCart() {
        val urna = urnaObject ?: return
        val cartService = RetrofitClient.getCartService(requireContext())

        val request = AddToCartRequest(
            cart_id = 1, // Temporal hasta asignar usuario real
            urn_id = urnaId,
            quantity = 1,
            unit_price = urna.price ?: 0.0
        )

        cartService.addItem(request).enqueue(object : Callback<CartItem> {
            override fun onResponse(call: Call<CartItem>, response: Response<CartItem>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Agregado al carrito ✔", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al agregar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CartItem>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
