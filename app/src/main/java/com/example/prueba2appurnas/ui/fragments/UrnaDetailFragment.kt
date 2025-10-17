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
import com.example.prueba2appurnas.databinding.FragmentUrnaDetailBinding // Asegúrate que el layout se llame fragment_urna_detail.xml
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

    // Patrón 'newInstance' para pasar la Urna de forma segura
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
        // Recuperar la Urna de los argumentos
        arguments?.let {
            currentUrna = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_URNA, Urna::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getSerializable(ARG_URNA) as? Urna
            }
        }

        if (currentUrna == null) {
            Log.e("UrnaDetailFragment", "⚠️ Urna no encontrada en argumentos. Cerrando fragment.")
            Toast.makeText(requireContext(), "Error al cargar la urna", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack() // Volver si no hay datos
            return
        }

        // Inicializar servicio API
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

        // Si currentUrna es nulo (aunque ya validamos en onCreate), no hacemos nada
        if (currentUrna == null) return

        setupUI()
        currentUrna?.id?.let { fetchUrnaImages(it) }

        // Navegación al fragmento de Edición
        binding.btnEditar.setOnClickListener {
            currentUrna?.let { urna ->
                val editFragment = EditUrnaFragment.newInstance(urna) // Crear instancia del fragment de edición
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, editFragment) // Reemplazar en el contenedor de HomeActivity
                    .addToBackStack(null) // Añadir a la pila para poder volver
                    .commit()
            }
        }

    }

    // Configura la interfaz con los datos de la urna
    private fun setupUI() {
        currentUrna?.let { urna ->
            // Cargar imagen principal
            val imagePath = urna.image_url?.path
            val fullUrl = NetUtils.buildAbsoluteUrl(imagePath)
            val glideModel = fullUrl?.let { NetUtils.glideModelWithAuth(requireContext(), it) }

            Glide.with(requireContext())
                .load(glideModel ?: R.drawable.bg_image_border) // Usa placeholder si no hay URL
                .placeholder(R.drawable.bg_image_border)
                .error(R.drawable.bg_image_border)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imageUrna)

            // Poblar campos de texto
            binding.tvNombreUrna.text = urna.name ?: "Sin nombre"
            binding.tvMedidas.text =
                "Ancho: ${urna.width ?: 0} cm | Prof.: ${urna.depth ?: 0} cm | Alto: ${urna.height ?: 0} cm" // Abreviado para claridad
            binding.tvDescripcionCorta.text = urna.short_description ?: "Sin descripción corta"
            binding.tvDescripcionLarga.text = urna.detailed_description ?: "Sin descripción detallada"
            binding.tvPeso.text = "Peso: ${urna.weight ?: 0.0} kg"
            binding.tvStock.text = "Stock: ${urna.stock ?: 0}"
            binding.tvPrecio.text = "Precio: $${String.format("%.2f", urna.price ?: 0.0)}" // Formatear precio
            binding.tvColor.text = "Color ID: ${urna.color_id ?: "-"}" // Idealmente mostrar nombre del color
            binding.tvMaterial.text = "Material ID: ${urna.material_id ?: "-"}" // Idealmente mostrar nombre

            // Disponibilidad con color
            val disponible = urna.available == true
            binding.tvDisponible.text = "Disponible: ${if (disponible) "Sí" else "No"}"
            binding.tvDisponible.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (disponible) R.color.stockNormal else R.color.stockBajo // Usa tus colores definidos
                )
            )

            // Configurar RecyclerView horizontal para imágenes adicionales
            binding.recyclerViewImages.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    // Obtiene y muestra las imágenes adicionales
    private fun fetchUrnaImages(urnaId: Int) {
        urnaImageService.getImagesByUrnaId(urnaId).enqueue(object : Callback<List<UrnaImage>> {
            override fun onResponse(call: Call<List<UrnaImage>>, response: Response<List<UrnaImage>>) {
                // Verificar si el fragmento sigue activo antes de tocar la UI
                if (_binding == null || !isAdded) return

                if (response.isSuccessful) {
                    // Filtrar por si acaso la API devuelve imágenes de otras urnas (aunque no debería si se usa Query Param)
                    val images = response.body()?.filter { it.urna_id == urnaId } ?: emptyList()
                    Log.d("UrnaDetailFragment", "Imágenes adicionales recibidas: ${images.size}")

                    if (images.isNotEmpty()) {
                        val adapter = UrnaImageAdapter(images) { imageUrl ->
                            // Al hacer clic en una miniatura, cárgala en la imagen principal
                            if (_binding != null && isAdded) { // Doble check por seguridad con Glide
                                val clickedGlideModel = NetUtils.glideModelWithAuth(requireContext(), imageUrl)
                                Glide.with(requireContext())
                                    .load(clickedGlideModel)
                                    .placeholder(R.drawable.bg_image_border)
                                    .error(R.drawable.bg_image_border)
                                    .centerCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade(200)) // Transición suave
                                    .into(binding.imageUrna)
                            }
                        }
                        binding.recyclerViewImages.adapter = adapter
                        binding.recyclerViewImages.visibility = View.VISIBLE // Asegura que sea visible
                    } else {
                        Log.w("UrnaDetailFragment", "⚠️ La urna no tiene imágenes adicionales asociadas.")
                        binding.recyclerViewImages.visibility = View.GONE // Oculta el RecyclerView si no hay imágenes
                    }
                } else {
                    Log.e("UrnaDetailFragment", "Error al cargar imágenes adicionales: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Error cargando galería (${response.code()})", Toast.LENGTH_SHORT).show()
                    binding.recyclerViewImages.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<UrnaImage>>, t: Throwable) {
                if (_binding == null || !isAdded) return
                Log.e("UrnaDetailFragment", "Fallo de red al conectar por imágenes adicionales: ${t.message}", t)
                Toast.makeText(context, "Fallo de red al cargar galería", Toast.LENGTH_LONG).show()
                binding.recyclerViewImages.visibility = View.GONE
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ¡Fundamental limpiar el binding!
    }
}