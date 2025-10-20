package com.example.prueba2appurnas.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
// import android.widget.ArrayAdapter // Ya no es necesario aquí
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
// import androidx.fragment.app.Fragment // Ahora hereda de BaseUrnaFormFragment
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
// import com.example.prueba2appurnas.api.ColorService // Movido a la base
// import com.example.prueba2appurnas.api.MaterialService // Movido a la base
// import com.example.prueba2appurnas.api.ModelService // Movido a la base
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.UrnaService
import com.example.prueba2appurnas.databinding.FragmentAddUrnaBinding
// import com.example.prueba2appurnas.model.Color // Movido a la base
// import com.example.prueba2appurnas.model.Material // Movido a la base
// import com.example.prueba2appurnas.model.Model // Movido a la base
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.ui.HomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okio.IOException

// 1. Heredar de BaseUrnaFormFragment
class AddUrnaFragment : BaseUrnaFormFragment() {

    private var _binding: FragmentAddUrnaBinding? = null
    private val binding get() = _binding!!

    // UrnaService sigue siendo específico de este fragmento (y de Edit)
    private lateinit var urnaService: UrnaService
    private var selectedImageUri: Uri? = null

    // --- Listas y Servicios de Spinners (Color, Material, Model) movidos a BaseUrnaFormFragment ---

    // --- Lógica de selección de imagen (Sin cambios) ---
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                if (selectedImageUri != null && context != null) {
                    Glide.with(requireContext())
                        .load(selectedImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.bg_image_border)
                        .error(R.drawable.bg_image_border)
                        .into(binding.imgUrnaPreview)
                }
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("PermissionDebug", "Permiso concedido por el usuario.")
                launchGallery()
            } else {
                Log.w("PermissionDebug", "Permiso DENEGADO por el usuario.")
                if (context != null) {
                    Toast.makeText(
                        requireContext(),
                        "El permiso para acceder a imágenes es necesario",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddUrnaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Llama a onCreate de la base

        // Inicializar UrnaService (los otros servicios ya están en la base)
        try {
            if (context == null) throw IllegalStateException("Contexto nulo al inicializar UrnaService")
            urnaService = RetrofitClient.getUrnaService(requireContext())
        } catch (e: Exception) {
            Log.e("AddUrnaFragment", "Error inicializando UrnaService: ${e.message}", e)
            if (context != null) {
                Toast.makeText(requireContext(), "Error configuración red", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack() // Salir si el servicio principal falla
            }
            return
        }

        binding.btnSelectMainImage.setOnClickListener {
            selectImage()
        }
        binding.btnSaveUrna.setOnClickListener {
            saveUrna()
        }

        // 2. Llamar al método de carga de la clase base
        loadSpinnersSequentially()
    }

    // --- 3. Implementar métodos abstractos de la clase base ---

    override fun showMainLoading(isLoading: Boolean) {
        if (_binding != null) {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveUrna.isEnabled = !isLoading
        }
    }

    override fun showSpinnerLoading(isLoading: Boolean) {
        if (_binding != null) {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.spinnerColor.isEnabled = !isLoading
            binding.spinnerMaterial.isEnabled = !isLoading
            binding.spinnerModel.isEnabled = !isLoading
        }
    }

    override fun getColorSpinner(): Spinner? = _binding?.spinnerColor
    override fun getMaterialSpinner(): Spinner? = _binding?.spinnerMaterial
    override fun getModelSpinner(): Spinner? = _binding?.spinnerModel

    // Para "Añadir", siempre devolvemos null (no hay preselección)
    override fun getSelectedColorId(): Int? = null
    override fun getSelectedMaterialId(): Int? = null
    override fun getSelectedModelId(): Int? = null


    // --- Lógica de selección de imagen y permisos (Sin cambios) ---
    private fun selectImage() {
        if (context == null) return
        Log.d("PermissionDebug", "Entrando en selectImage()")

        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        Log.d("PermissionDebug", "Versión SDK: ${Build.VERSION.SDK_INT}. Permiso a solicitar: $permissionToRequest")

        when {
            ContextCompat.checkSelfPermission(requireContext(), permissionToRequest) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("PermissionDebug", "Permiso YA concedido ($permissionToRequest). Lanzando galería...")
                launchGallery()
            }
            shouldShowRequestPermissionRationale(permissionToRequest) -> {
                Log.d("PermissionDebug", "Mostrando explicación para el permiso $permissionToRequest...")
                Toast.makeText(requireContext(), "Se necesita permiso para acceder a imágenes", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(permissionToRequest)
            }
            else -> {
                Log.d("PermissionDebug", "Permiso NO concedido ($permissionToRequest). Lanzando el diálogo...")
                requestPermissionLauncher.launch(permissionToRequest)
            }
        }
    }

    private fun launchGallery() {
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            galleryLauncher.launch(intent)
        } else {
            Log.e("AddUrnaFragment", "No se encontró actividad para manejar la selección de imágenes.")
            Toast.makeText(requireContext(), "No se encontró aplicación de galería.", Toast.LENGTH_SHORT).show()
        }
    }


    // --- Lógica para guardar la urna (Multipart) ---
    private fun saveUrna() {
        if (context == null || _binding == null) return

        val name = binding.etName.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val stockStr = binding.etStock.text.toString().trim()
        val shortDesc = binding.etShortDescription.text.toString().trim()
        val detailedDesc = binding.etDetailedDescription.text.toString().trim()
        val isAvailable = binding.switchAvailable.isChecked
        val internalId = binding.etInternalId.text.toString().trim()
        val widthStr = binding.etWidth.text.toString().trim()
        val depthStr = binding.etDepth.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()

        // 4. Usar el método de la clase base para obtener los IDs
        val colorId = getIdFromSpinnerSelection(binding.spinnerColor, colorsList)
        val materialId = getIdFromSpinnerSelection(binding.spinnerMaterial, materialsList)
        val modelId = getIdFromSpinnerSelection(binding.spinnerModel, modelsList)

        // Validaciones (sin cambios)
        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre, Precio y Stock son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()
        if (price == null || stock == null) {
            Toast.makeText(requireContext(), "Precio y Stock deben ser números válidos", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Por favor, selecciona una imagen principal", Toast.LENGTH_SHORT).show()
            return
        }
        val width = widthStr.toDoubleOrNull()
        val depth = depthStr.toDoubleOrNull()
        val height = heightStr.toDoubleOrNull()
        val weight = weightStr.toDoubleOrNull()
        if (widthStr.isNotBlank() && width == null ||
            depthStr.isNotBlank() && depth == null ||
            heightStr.isNotBlank() && height == null ||
            weightStr.isNotBlank() && weight == null) {
            Toast.makeText(requireContext(), "Dimensiones y Peso deben ser números válidos si se ingresan", Toast.LENGTH_SHORT).show()
            return
        }

        showMainLoading(true) // Usar el método implementado

        try {
            val namePart = createTextPart(name)
            val pricePart = createTextPart(priceStr)
            val stockPart = createTextPart(stockStr)
            val availablePart = createTextPart(isAvailable.toString())
            val imageFilePart = createImagePart(requireContext(), selectedImageUri!!, "image_url")

            val shortDescPart = if (shortDesc.isNotEmpty()) createTextPart(shortDesc) else null
            val detailedDescPart = if (detailedDesc.isNotEmpty()) createTextPart(detailedDesc) else null
            val internalIdPart = if (internalId.isNotEmpty()) createTextPart(internalId) else null

            val widthPart = if (width != null) createTextPart(widthStr) else null
            val depthPart = if (depth != null) createTextPart(depthStr) else null
            val heightPart = if (height != null) createTextPart(heightStr) else null
            val weightPart = if (weight != null) createTextPart(weightStr) else null
            val colorIdPart = colorId?.let { createTextPart(it.toString()) }
            val materialIdPart = materialId?.let { createTextPart(it.toString()) }
            val modelIdPart = modelId?.let { createTextPart(it.toString()) }


            urnaService.createUrnaMultipart(
                name = namePart,
                price = pricePart,
                stock = stockPart,
                available = availablePart,
                imageFile = imageFilePart,
                shortDescription = shortDescPart,
                detailedDescription = detailedDescPart,
                internalId = internalIdPart,
                width = widthPart,
                depth = depthPart,
                height = heightPart,
                weight = weightPart,
                colorId = colorIdPart,
                materialId = materialIdPart,
                modelId = modelIdPart
            ).enqueue(object : Callback<Urna> {
                override fun onResponse(call: Call<Urna>, response: Response<Urna>) {
                    if (_binding == null) return
                    showMainLoading(false) // Usar el método implementado
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "¡Urna creada con éxito!", Toast.LENGTH_LONG).show()
                        clearForm()
                        try {
                            (activity as? HomeActivity)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                                ?.selectedItemId = R.id.nav_urnas
                        } catch (e: Exception) {
                            Log.e("AddUrnaFragment", "Error al intentar navegar a Urnas", e)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                        Log.e("AddUrnaFragment", "Error al crear urna (Multipart): ${response.code()} - ${response.message()} | Cuerpo: $errorBody")
                        Toast.makeText(requireContext(), "Error ${response.code()}: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Urna>, t: Throwable) {
                    if (_binding == null) return
                    showMainLoading(false) // Usar el método implementado
                    Log.e("AddUrnaFragment", "Fallo al crear urna (Multipart)", t)
                    Toast.makeText(requireContext(), "Fallo de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            Log.e("AddUrnaFragment", "Error al preparar datos para la API", e)
            Toast.makeText(requireContext(), "Error al procesar datos o imagen", Toast.LENGTH_LONG).show()
            showMainLoading(false) // Usar el método implementado
        }

    }

    // --- Todos los métodos de carga de Spinners y callbacks fueron movidos a la clase base ---

    // --- Funciones Helper (sólo las que quedan) ---

    private fun createTextPart(text: String): RequestBody {
        return text.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    @Throws(IOException::class)
    private fun createImagePart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part {
        val mimeType = getMimeType(context, uri) ?: "image/jpeg"
        val fileName = getFileName(context, uri)
        Log.d("HelperUtil", "Creando imagePart: field='$fieldName', filename='$fileName', mime='$mimeType'")

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw _root_ide_package_.okio.IOException("No se pudo abrir InputStream para Uri: $uri")

        val bytes = inputStream.use { it.readBytes() }
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }

    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)?.lowercase()
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.lowercase()
            )
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) { result = cursor.getString(index) }
                    }
                }
            } catch (e: Exception) { Log.w("HelperUtil", "Error obteniendo nombre (ContentResolver)", e) }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) { result = result?.substring(cut + 1) }
        }
        val cleanedResult = result?.replace("[^a-zA-Z0-9._-]".toRegex(), "_") ?: "unknown_${System.currentTimeMillis()}"
        Log.d("HelperUtil", "Nombre archivo final: $cleanedResult (Original: $result)")
        return cleanedResult
    }

    private fun clearForm() {
        if (_binding != null) {
            binding.etName.text = null
            binding.etPrice.text = null
            binding.etStock.text = null
            binding.etShortDescription.text = null
            binding.etDetailedDescription.text = null
            binding.switchAvailable.isChecked = true
            binding.imgUrnaPreview.setImageResource(R.drawable.bg_image_border)
            selectedImageUri = null
            binding.etInternalId.text = null
            binding.etWidth.text = null
            binding.etDepth.text = null
            binding.etHeight.text = null
            binding.etWeight.text = null
            if (binding.spinnerColor.adapter?.count ?: 0 > 0) binding.spinnerColor.setSelection(0)
            if (binding.spinnerMaterial.adapter?.count ?: 0 > 0) binding.spinnerMaterial.setSelection(0)
            if (binding.spinnerModel.adapter?.count ?: 0 > 0) binding.spinnerModel.setSelection(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}