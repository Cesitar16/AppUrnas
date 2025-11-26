package com.example.prueba2appurnas.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.UrnaImageService
import com.example.prueba2appurnas.databinding.FragmentUrnaDetailBinding
import com.example.prueba2appurnas.model.UrlObject
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.model.UrnaImage
import com.example.prueba2appurnas.ui.UrnaImageAdapter
import com.example.prueba2appurnas.util.NetUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UrnaDetailFragment : Fragment() {

    private var _binding: FragmentUrnaDetailBinding? = null
    private val binding get() = _binding!!

    private var currentUrna: Urna? = null
    private lateinit var urnaImageService: UrnaImageService

    companion object {
        private const val ARG_URNA = "urn_arg"

        fun newInstance(urna: Urna): UrnaDetailFragment {
            val fragment = UrnaDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_URNA, urna)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            currentUrna = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_URNA, Urna::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getSerializable(ARG_URNA) as? Urna
            }
        }

        if (currentUrna == null) {
            Toast.makeText(requireContext(), "Error al cargar la urna", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        urnaImageService = RetrofitClient.getUrnaImageService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUrnaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        currentUrna?.id?.let { fetchUrnaImages(it) }
    }

    private fun setupUI() {
        val urna = currentUrna ?: return

        val imagePath = urna.image_url?.path
        val fullUrl = NetUtils.buildAbsoluteUrl(imagePath)

        Glide.with(requireContext())
            .load(fullUrl)
            .placeholder(R.drawable.bg_image_border)
            .error(R.drawable.bg_image_border)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.imageUrna)

        binding.tvNombreUrna.text = urna.name ?: "-"
        binding.tvMedidas.text =
            "Ancho: ${urna.width ?: 0} cm | Prof.: ${urna.depth ?: 0} cm | Alto: ${urna.height ?: 0} cm"
        binding.tvDescripcionCorta.text = urna.short_description ?: "-"
        binding.tvDescripcionLarga.text = urna.detailed_description ?: "-"
        binding.tvPeso.text = "Peso: ${urna.weight ?: 0.0} kg"
        binding.tvStock.text = "Stock: ${urna.stock ?: 0}"
        binding.tvPrecio.text = "Precio: $${String.format("%.2f", urna.price ?: 0.0)}"
        binding.tvColor.text = "Color ID: ${urna.color_id ?: "-"}"
        binding.tvMaterial.text = "Material ID: ${urna.material_id ?: "-"}"

        val disponible = urna.available == true
        binding.tvDisponible.text = "Disponible: ${if (disponible) "Sí" else "No"}"
        binding.tvDisponible.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (disponible) R.color.stockNormal else R.color.stockBajo
            )
        )

        binding.recyclerViewImages.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun fetchUrnaImages(urnaId: Int) {
        urnaImageService.getImagesByUrnaId(urnaId)
            .enqueue(object : Callback<List<UrnaImage>> {

                override fun onResponse(
                    call: Call<List<UrnaImage>>,
                    response: Response<List<UrnaImage>>
                ) {
                    if (!isAdded || _binding == null) return

                    if (!response.isSuccessful) {
                        binding.recyclerViewImages.visibility = View.GONE
                        return
                    }

                    val galleryImages = response.body() ?: emptyList()
                    val finalImages = mutableListOf<UrnaImage>()

                    // Imagen principal
                    val mainPath = currentUrna?.image_url?.path
                    if (!mainPath.isNullOrBlank()) {
                        finalImages.add(
                            UrnaImage(
                                id = -1,
                                urna_id = urnaId,
                                alt = "Imagen principal",
                                is_cover = true,
                                sort_order = 0,
                                url = UrlObject(mainPath)
                            )
                        )
                    }

                    finalImages.addAll(galleryImages)

                    if (finalImages.isEmpty()) {
                        binding.recyclerViewImages.visibility = View.GONE
                        return
                    }

                    val adapter = UrnaImageAdapter(finalImages) { imageUrl ->
                        Glide.with(requireContext())
                            .load(NetUtils.buildAbsoluteUrl(imageUrl))
                            .placeholder(R.drawable.bg_image_border)
                            .centerCrop()
                            .into(binding.imageUrna)
                    }

                    binding.recyclerViewImages.adapter = adapter
                    binding.recyclerViewImages.visibility = View.VISIBLE
                }

                override fun onFailure(call: Call<List<UrnaImage>>, t: Throwable) {
                    if (!isAdded || _binding == null) return

                    binding.recyclerViewImages.visibility = View.GONE
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
