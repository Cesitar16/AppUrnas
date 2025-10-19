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
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.ColorService
import com.example.prueba2appurnas.api.MaterialService
import com.example.prueba2appurnas.api.ModelService
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.UrnaService
import com.example.prueba2appurnas.databinding.FragmentAddUrnaBinding // Binding actualizado
import com.example.prueba2appurnas.model.Color
import com.example.prueba2appurnas.model.Material
import com.example.prueba2appurnas.model.Model
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.ui.HomeActivity // Para navegar
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okio.IOException

class AddUrnaFragment : Fragment() { // Nombre actualizado

    // View Binding seguro
    private var _binding: FragmentAddUrnaBinding? = null // Binding actualizado
    private val binding get() = _binding!!

    private lateinit var urnaService: UrnaService
    private lateinit var colorService: ColorService // Añadido
    private lateinit var materialService: MaterialService // Añadido
    private lateinit var modelService: ModelService // Añadido
    private var selectedImageUri: Uri? = null

    private var colorsList: List<Color> = emptyList()
    private var materialsList: List<Material> = emptyList()
    private var modelsList: List<Model> = emptyList()

    // --- Lógica para seleccionar imagen y pedir permisos ---
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                if (selectedImageUri != null && context != null) { // Añadir chequeo de contexto
                    Glide.with(requireContext()) // Usar requireContext()
                        .load(selectedImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.bg_image_border) // Placeholder
                        .error(R.drawable.bg_image_border) // Error image
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
                if (context != null) { // Chequeo de contexto
                    Toast.makeText(
                        requireContext(), // Usar requireContext()
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
        super.onViewCreated(view, savedInstanceState)

        // Inicializar TODOS los servicios (sin cambios)
        try {
            if (context == null) throw IllegalStateException("Contexto nulo al inicializar servicios")
            urnaService = RetrofitClient.getUrnaService(requireContext())
            colorService = RetrofitClient.getColorService(requireContext())
            materialService = RetrofitClient.getMaterialService(requireContext())
            modelService = RetrofitClient.getModelService(requireContext())
        } catch (e: Exception) {
            Log.e("AddUrnaFragment", "Error inicializando servicios: ${e.message}", e)
            if (context != null) {
                Toast.makeText(requireContext(), "Error configuración red", Toast.LENGTH_LONG).show()
                // Considera cerrar el fragment si falla la inicialización esencial
                // parentFragmentManager.popBackStack()
            }
            return // Salir si fallan los servicios
        }

        binding.btnSelectMainImage.setOnClickListener {
            selectImage()
        }
        binding.btnSaveUrna.setOnClickListener {
            saveUrna()
        }

        // Cargar datos en los Spinners (AHORA SECUENCIALMENTE)
        loadSpinnersSequentially() // Cambiado nombre de la función
    }

    // --- Funciones de selección de imagen y permisos (adaptadas para fragmento) ---
    private fun selectImage() {
        if (context == null) return // Chequeo de contexto
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
        // Usar el lanzador de resultados estándar para contenido
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
            type = "image/*" // Solo imágenes
        }
        // Asegurarse de que haya una app para manejar el intent
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

        // --- Recoger datos de TODOS los campos ---
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

        val colorId = getIdFromSpinnerSelection(binding.spinnerColor, colorsList)
        val materialId = getIdFromSpinnerSelection(binding.spinnerMaterial, materialsList)
        val modelId = getIdFromSpinnerSelection(binding.spinnerModel, modelsList)

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

        // Validación opcional para dimensiones/peso (si son obligatorios o deben ser números)
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
        // Aquí podrías añadir validación si width, depth, etc., no son nulos y fallan la conversión


        showLoading(true)

        try {
            // --- Crear partes Multipart para TODOS los campos ---
            val namePart = createTextPart(name)
            val pricePart = createTextPart(priceStr)
            val stockPart = createTextPart(stockStr)
            val availablePart = createTextPart(isAvailable.toString())
            val imageFilePart = createImagePart(requireContext(), selectedImageUri!!, "image_url")

            // Partes opcionales de texto
            val shortDescPart = if (shortDesc.isNotEmpty()) createTextPart(shortDesc) else null
            val detailedDescPart = if (detailedDesc.isNotEmpty()) createTextPart(detailedDesc) else null
            val internalIdPart = if (internalId.isNotEmpty()) createTextPart(internalId) else null

            // Partes opcionales numéricas (convertir ID a String si existe)
            val widthPart = if (width != null) createTextPart(widthStr) else null
            val depthPart = if (depth != null) createTextPart(depthStr) else null
            val heightPart = if (height != null) createTextPart(heightStr) else null
            val weightPart = if (weight != null) createTextPart(weightStr) else null
            val colorIdPart = colorId?.let { createTextPart(it.toString()) }
            val materialIdPart = materialId?.let { createTextPart(it.toString()) }
            val modelIdPart = modelId?.let { createTextPart(it.toString()) }


            // --- Llamada API con TODAS las partes ---
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
                colorId = colorIdPart, // Pasar parte
                materialId = materialIdPart, // Pasar parte
                modelId = modelIdPart // Pasar parte
            ).enqueue(object : Callback<Urna> {
                override fun onResponse(call: Call<Urna>, response: Response<Urna>) {
                    if (_binding == null) return
                    showLoading(false)
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
                    showLoading(false)
                    Log.e("AddUrnaFragment", "Fallo al crear urna (Multipart)", t)
                    Toast.makeText(requireContext(), "Fallo de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            Log.e("AddUrnaFragment", "Error al preparar datos para la API", e)
            Toast.makeText(requireContext(), "Error al procesar datos o imagen", Toast.LENGTH_LONG).show()
            showLoading(false)
        }

    }

    private fun <T> getIdFromSpinnerSelection(spinner: Spinner, dataList: List<T>): Int? where T: Any {
        val position = spinner.selectedItemPosition
        // Valida que la posición sea válida y que haya un item seleccionado
        if (position < 0 || position >= dataList.size || spinner.selectedItem == null) return null
        return try {
            when (val item = dataList[position]) {
                is Color -> item.id
                is Material -> item.id
                is Model -> item.id
                else -> null // Tipo no esperado
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e("AddUrnaFragment", "Error al obtener ID del spinner", e)
            null // Error inesperado
        }
    }

    private fun loadSpinnersSequentially() {
        // Muestra algún indicador de carga si lo deseas
        showSpinnerLoading(true) // Función helper opcional
        Log.d("LoadSpinners", "Iniciando carga secuencial...")

        // 1. Cargar Colores
        colorService.getAllColors().enqueue(object : Callback<List<Color>> {
            override fun onResponse(call: Call<List<Color>>, response: Response<List<Color>>) {
                if (!isAdded || _binding == null || context == null) return
                Log.d("LoadSpinners", "Respuesta Colores: ${response.code()}")
                if (response.isSuccessful) {
                    colorsList = response.body() ?: emptyList()
                    setupSpinnerAdapter(binding.spinnerColor, colorsList, "colores")
                    // 2. Si Colores cargó OK, cargar Materiales
                    Log.d("LoadSpinners", "Colores OK. Cargando Materiales...")
                    loadMaterials()
                } else {
                    handleApiError("colores", response.code(), response.message())
                    binding.spinnerColor.adapter = null
                    // Decide si continuar o detener la carga si uno falla
                    // Por ahora, continuamos para intentar cargar los otros
                    Log.w("LoadSpinners", "Fallo al cargar Colores. Cargando Materiales de todas formas...")
                    loadMaterials()
                }
            }

            override fun onFailure(call: Call<List<Color>>, t: Throwable) {
                if (!isAdded || _binding == null || context == null) return
                handleApiFailure("colores", t)
                binding.spinnerColor.adapter = null
                Log.w("LoadSpinners", "Fallo de red Colores. Cargando Materiales de todas formas...")
                loadMaterials() // Continuar aunque falle
            }
        })
    }

    private fun loadMaterials() {
        materialService.getAllMaterials().enqueue(object : Callback<List<Material>> {
            override fun onResponse(call: Call<List<Material>>, response: Response<List<Material>>) {
                if (!isAdded || _binding == null || context == null) return
                Log.d("LoadSpinners", "Respuesta Materiales: ${response.code()}")
                if (response.isSuccessful) {
                    materialsList = response.body() ?: emptyList()
                    setupSpinnerAdapter(binding.spinnerMaterial, materialsList, "materiales")
                    // 3. Si Materiales cargó OK, cargar Modelos
                    Log.d("LoadSpinners", "Materiales OK. Cargando Modelos...")
                    loadModels()
                } else {
                    handleApiError("materiales", response.code(), response.message())
                    binding.spinnerMaterial.adapter = null
                    Log.w("LoadSpinners", "Fallo al cargar Materiales. Cargando Modelos de todas formas...")
                    loadModels() // Continuar
                }
            }

            override fun onFailure(call: Call<List<Material>>, t: Throwable) {
                if (!isAdded || _binding == null || context == null) return
                handleApiFailure("materiales", t)
                binding.spinnerMaterial.adapter = null
                Log.w("LoadSpinners", "Fallo de red Materiales. Cargando Modelos de todas formas...")
                loadModels() // Continuar
            }
        })
    }

    private fun loadModels() {
        modelService.getAllModels().enqueue(object : Callback<List<Model>> {
            override fun onResponse(call: Call<List<Model>>, response: Response<List<Model>>) {
                if (!isAdded || _binding == null || context == null) return
                Log.d("LoadSpinners", "Respuesta Modelos: ${response.code()}")
                if (response.isSuccessful) {
                    modelsList = response.body() ?: emptyList()
                    setupSpinnerAdapter(binding.spinnerModel, modelsList, "modelos")
                    Log.d("LoadSpinners", "Modelos OK. Carga secuencial completa.")
                } else {
                    handleApiError("modelos", response.code(), response.message())
                    binding.spinnerModel.adapter = null
                    Log.w("LoadSpinners", "Fallo al cargar Modelos. Carga secuencial completa con errores.")
                }
                // 4. Fin de la carga, ocultar indicador
                showSpinnerLoading(false)
            }

            override fun onFailure(call: Call<List<Model>>, t: Throwable) {
                if (!isAdded || _binding == null || context == null) return
                handleApiFailure("modelos", t)
                binding.spinnerModel.adapter = null
                Log.w("LoadSpinners", "Fallo de red Modelos. Carga secuencial completa con errores.")
                showSpinnerLoading(false) // Fin de la carga
            }
        })
    }

    private fun <T> setupSpinnerAdapter(spinner: Spinner, dataList: List<T>, dataTypeName: String) where T : Any {
        if (!isAdded || context == null) return // Comprobación extra

        val names = dataList.mapNotNull { item ->
            when (item) {
                is Color -> item.name.takeIf { !it.isNullOrBlank() }
                is Material -> item.name.takeIf { !it.isNullOrBlank() }
                is Model -> item.name.takeIf { !it.isNullOrBlank() }
                else -> item.toString()
            }
        }

        if (names.isNotEmpty()) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(0, false) // Selecciona el primer item
            Log.d("SetupSpinner", "Adapter configurado para $dataTypeName con ${names.size} items.")
        } else {
            Log.w("SetupSpinner", "Lista de nombres vacía para $dataTypeName.")
            spinner.adapter = null // O un adapter con "No hay opciones"
        }
    }

    private fun <T> createSpinnerCallback(
        spinner: Spinner, dataList: List<T>, updateDataList: (List<T>) -> Unit,
        currentSelectedId: Int?, // Será null aquí, pero mantenemos la firma
        dataTypeName: String
    ): Callback<List<T>> where T : Any {
        return object : Callback<List<T>> {
            override fun onResponse(call: Call<List<T>>, response: Response<List<T>>) {
                // Comprobaciones cruciales antes de tocar la UI
                if (!isAdded || _binding == null || context == null) {
                    Log.w("SpinnerCallback", "Fragmento no adjunto o binding/context nulo en onResponse para $dataTypeName")
                    return
                }

                if (response.isSuccessful) {
                    val newList = response.body() ?: emptyList()
                    Log.d("SpinnerCallback", "Datos recibidos para $dataTypeName: ${newList.size} items")
                    updateDataList(newList) // Actualiza la lista en el fragmento

                    // Mapea los objetos a una lista de Strings (sus nombres)
                    val names = newList.mapNotNull { item -> // Usamos mapNotNull para seguridad
                        when (item) {
                            // Asegúrate que los modelos Color, Material, Model tienen un campo 'name'
                            is Color -> item.name.takeIf { !it.isNullOrBlank() }
                            is Material -> item.name.takeIf { !it.isNullOrBlank() }
                            is Model -> item.name.takeIf { !it.isNullOrBlank() }
                            else -> item.toString() // Fallback
                        }
                    }

                    // Verifica si la lista de nombres no está vacía antes de crear el adapter
                    if (names.isNotEmpty()) {
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                        // En "Crear", no preseleccionamos nada (o seleccionamos el primero por defecto)
                        spinner.setSelection(0, false) // Selecciona el primer item si existe
                        Log.d("SpinnerCallback", "Adapter configurado para $dataTypeName con ${names.size} nombres.")
                    } else {
                        Log.w("SpinnerCallback", "La lista de nombres para $dataTypeName está vacía después de mapear.")
                        // Opcional: Configurar un adapter vacío o con un mensaje "No hay opciones"
                        spinner.adapter = null // O un adapter con un mensaje
                    }

                } else {
                    // Manejar error de API
                    handleApiError(dataTypeName, response.code(), response.message())
                    spinner.adapter = null // Limpia el spinner en caso de error
                }
            }

            override fun onFailure(call: Call<List<T>>, t: Throwable) {
                if (!isAdded || _binding == null || context == null) {
                    Log.w("SpinnerCallback", "Fragmento no adjunto o binding/context nulo en onFailure para $dataTypeName")
                    return
                }
                // Manejar fallo de red
                handleApiFailure(dataTypeName, t)
                spinner.adapter = null // Limpia el spinner en caso de error
            }
        }
    }

    private fun handleApiError(dataType: String, code: Int, message: String?) {
        Log.e("AddUrnaFragment", "Error API cargando $dataType: $code - $message")
        if (context != null) Toast.makeText(context, "Error cargando $dataType ($code)", Toast.LENGTH_SHORT).show()
    }
    private fun handleApiFailure(dataType: String, t: Throwable) {
        Log.e("AddUrnaFragment", "Fallo red cargando $dataType", t)
        if (context != null) Toast.makeText(context, "Fallo red cargando $dataType", Toast.LENGTH_SHORT).show()
    }

    // --- Funciones Helper ---

    private fun showSpinnerLoading(isLoading: Boolean) {
        // Puedes usar el ProgressBar general o uno específico para los spinners
        _binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Podrías deshabilitar los spinners mientras cargan
        _binding?.spinnerColor?.isEnabled = !isLoading
        _binding?.spinnerMaterial?.isEnabled = !isLoading
        _binding?.spinnerModel?.isEnabled = !isLoading
    }

    /** Crea RequestBody de texto plano. */
    private fun createTextPart(text: String): RequestBody {
        return text.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /** Crea MultipartBody.Part para una imagen Uri. */
    @Throws(IOException::class)
    private fun createImagePart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part {
        val mimeType = getMimeType(context, uri) ?: "image/jpeg"
        val fileName = getFileName(context, uri)
        Log.d("HelperUtil", "Creando imagePart: field='$fieldName', filename='$fileName', mime='$mimeType'")

        // Usa contentResolver.openInputStream de forma segura con .use
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw _root_ide_package_.okio.IOException("No se pudo abrir InputStream para Uri: $uri")

        val bytes = inputStream.use { it.readBytes() } // .use cierra el stream automáticamente
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }

    /** Obtiene el MimeType de una Uri. */
    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)?.lowercase()
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.lowercase()
            )
    }

    /** Obtiene el nombre de archivo de una Uri de forma segura. */
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

    /** Muestra/oculta el ProgressBar y habilita/deshabilita el botón guardar. */
    private fun showLoading(isLoading: Boolean) {
        if (_binding != null) {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveUrna.isEnabled = !isLoading
            // Opcional: Deshabilitar otros campos durante la carga
        }
    }

    /** Limpia los campos del formulario. */
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
            // Resetear spinners a la primera posición (si tienen items)
            if (binding.spinnerColor.adapter?.count ?: 0 > 0) binding.spinnerColor.setSelection(0)
            if (binding.spinnerMaterial.adapter?.count ?: 0 > 0) binding.spinnerMaterial.setSelection(0)
            if (binding.spinnerModel.adapter?.count ?: 0 > 0) binding.spinnerModel.setSelection(0)
        }
    }

    /** Limpia el binding. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}