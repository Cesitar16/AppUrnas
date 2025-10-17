package com.example.prueba2appurnas.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import android.widget.ArrayAdapter // Necesario para Spinners
import android.widget.Spinner    // Necesario para Spinners
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager // Necesario para popBackStack
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.* // Importar todos los servicios y data classes necesarios
import com.example.prueba2appurnas.databinding.FragmentEditUrnaBinding // Asegúrate que el layout se llama fragment_edit_urna.xml
import com.example.prueba2appurnas.model.* // Importar modelos (Urna, Color, Material, Model, UrnaImage, ImageUrl)
import com.example.prueba2appurnas.util.NetUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody // Necesario para @Multipart
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException // Necesario para createImagePart
import kotlin.math.abs

class EditUrnaFragment : Fragment() {

    // --- Variables de Binding y Servicios ---
    private var _binding: FragmentEditUrnaBinding? = null
    private val binding get() = _binding!!

    private lateinit var urnaService: UrnaService
    private lateinit var uploadService: UploadService // Necesario si cambias img principal (PATCH usa 2 pasos)
    private lateinit var colorService: ColorService
    private lateinit var materialService: MaterialService
    private lateinit var modelService: ModelService
    private lateinit var urnaImageService: UrnaImageService // Clave para añadir a galería con @Multipart

    // --- Listas y Datos ---
    private var colorsList: List<Color> = emptyList()
    private var materialsList: List<Material> = emptyList()
    private var modelsList: List<Model> = emptyList()
    private var currentUrna: Urna? = null
    private var selectedMainImageUri: Uri? = null // Para cambiar imagen principal
    private var isSelectingForMainImage: Boolean = true // Para diferenciar acción de selección

    // --- Contadores para Subida Múltiple ---
    private var uploadTotalCount = 0
    private var uploadSuccessCount = 0
    private var uploadErrorCount = 0
    private var progressToast: Toast? = null

    // --- ActivityResultLaunchers ---
    // Launcher para selección simple (cambiar imagen principal)
    private val pickSingleImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleImageSelectionResult(result, false) // false = no múltiple
        }

    // Launcher para selección múltiple (añadir a galería)
    private val pickMultipleImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
            handleImageSelectionResult(null, true, uris) // true = múltiple
        }

    // Función unificada para manejar resultado de selección
    private fun handleImageSelectionResult(result: androidx.activity.result.ActivityResult?, isMultiple: Boolean, uris: List<Uri>? = null) {
        val selectedUris = mutableListOf<Uri>()

        if (isMultiple) {
            uris?.let { if (it.isNotEmpty()) selectedUris.addAll(it) }
        } else {
            if (result?.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { selectedUris.add(it) }
            }
        }

        if (selectedUris.isNotEmpty()) {
            if (isSelectingForMainImage) { // Cambiar imagen principal
                selectedMainImageUri = selectedUris[0]
                if (context != null && _binding != null) {
                    Glide.with(requireContext()).load(selectedMainImageUri).centerCrop()
                        .placeholder(R.drawable.bg_image_border).into(binding.ivUrnaImage)
                }
                Log.d("EditUrnaFragment", "Nueva img principal seleccionada: ${selectedMainImageUri}")
            } else { // Añadir a galería
                Log.d("EditUrnaFragment", "${selectedUris.size} imágenes seleccionadas para galería.")
                // *** CORRECCIÓN: LLAMAR A LA FUNCIÓN MULTIPART ***
                uploadMultipleImagesToGalleryMultipart(selectedUris) // Inicia subida múltiple con @Multipart
            }
        } else {
            Log.w("EditUrnaFragment", "Selección de imagen(es) cancelada o vacía.")
        }
    }


    // Launcher para permisos
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("EditUrnaFragment", "Permiso concedido.")
                launchGallery() // Reintentar lanzar selector
            } else {
                Log.w("EditUrnaFragment", "Permiso denegado.")
                if (context != null) Toast.makeText(requireContext(), "Permiso necesario", Toast.LENGTH_LONG).show()
            }
        }

    // --- newInstance ---
    companion object {
        private const val ARG_URNA = "urn_arg"
        fun newInstance(urna: Urna): EditUrnaFragment {
            val fragment = EditUrnaFragment()
            val args = Bundle()
            args.putSerializable(ARG_URNA, urna)
            fragment.arguments = args
            return fragment
        }
    }

    // --- onCreate ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recuperar Urna
        arguments?.let {
            currentUrna = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_URNA, Urna::class.java)
            } else { @Suppress("DEPRECATION") it.getSerializable(ARG_URNA) as? Urna }
        }
        if (currentUrna == null) {
            Log.e("EditUrnaFragment", "Urna no recibida en onCreate.")
            if (context != null) {
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            return
        }

        // Inicializar servicios
        try {
            if (context == null) throw IllegalStateException("Contexto nulo al inicializar servicios")
            urnaService = RetrofitClient.getUrnaService(requireContext())
            uploadService = RetrofitClient.getUploadService(requireContext())
            colorService = RetrofitClient.getColorService(requireContext())
            materialService = RetrofitClient.getMaterialService(requireContext())
            modelService = RetrofitClient.getModelService(requireContext())
            urnaImageService = RetrofitClient.getUrnaImageService(requireContext())
        } catch (e: Exception) {
            Log.e("EditUrnaFragment", "Error inicializando servicios: ${e.message}", e)
            if (context != null) {
                Toast.makeText(requireContext(), "Error configuración red", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    // --- onCreateView ---
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditUrnaBinding.inflate(inflater, container, false)
        return binding.root
    }

    // --- onViewCreated ---
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (currentUrna == null || !::urnaService.isInitialized || !::uploadService.isInitialized ||
            !::colorService.isInitialized || !::materialService.isInitialized || !::modelService.isInitialized ||
            !::urnaImageService.isInitialized) {
            Log.e("EditUrnaFragment", "Fallo en inicialización o datos. Cerrando.")
            if(context != null) Toast.makeText(context, "Error al iniciar pantalla", Toast.LENGTH_SHORT).show()
            if (isAdded) parentFragmentManager.popBackStack()
            return
        }

        populateFields()
        loadSpinners()

        // Listeners
        binding.btnSelectImage.setOnClickListener { selectImage(true) }       // Cambiar img principal
        binding.btnAddGalleryImage.setOnClickListener { selectImage(false) }   // Añadir a galería
        binding.btnSaveChanges.setOnClickListener { saveUrnaChanges() }       // Guardar cambios urna
        binding.btnDeleteUrna.setOnClickListener { showDeleteConfirmationDialog() } // Eliminar urna
    }

    // --- populateFields ---
    private fun populateFields() {
        currentUrna?.let { urna ->
            binding.etName.setText(urna.name ?: "")
            binding.etPrice.setText(urna.price?.toString() ?: "")
            binding.etStock.setText(urna.stock?.toString() ?: "")
            binding.etShortDescription.setText(urna.short_description ?: "")
            binding.etDetailedDescription.setText(urna.detailed_description ?: "")
            binding.switchAvailable.isChecked = urna.available ?: true

            if (context != null) {
                val imagePath = urna.image_url?.path
                val fullImageUrl = NetUtils.buildAbsoluteUrl(imagePath)
                val glideModel = fullImageUrl?.let { NetUtils.glideModelWithAuth(requireContext(), it) }
                Glide.with(requireContext())
                    .load(glideModel ?: R.drawable.bg_image_border)
                    .placeholder(R.drawable.bg_image_border).error(R.drawable.bg_image_border)
                    .centerCrop().into(binding.ivUrnaImage)
            }
        }
    }

    // --- loadSpinners ---
    private fun loadSpinners() {
        colorService.getAllColors().enqueue(createSpinnerCallback(binding.spinnerColor, colorsList, { colorsList = it }, currentUrna?.color_id, "colores"))
        materialService.getAllMaterials().enqueue(createSpinnerCallback(binding.spinnerMaterial, materialsList, { materialsList = it }, currentUrna?.material_id, "materiales"))
        modelService.getAllModels().enqueue(createSpinnerCallback(binding.spinnerModel, modelsList, { modelsList = it }, currentUrna?.model_id, "modelos"))
    }

    // --- createSpinnerCallback (Genérico) ---
    private fun <T> createSpinnerCallback(
        spinner: Spinner, dataList: List<T>, updateDataList: (List<T>) -> Unit,
        currentSelectedId: Int?, dataTypeName: String
    ): Callback<List<T>> where T : Any {
        return object : Callback<List<T>> {
            override fun onResponse(call: Call<List<T>>, response: Response<List<T>>) {
                if (!isAdded || _binding == null || context == null) return
                if (response.isSuccessful) {
                    val newList = response.body() ?: emptyList()
                    updateDataList(newList)
                    val names = newList.map { item ->
                        when (item) {
                            is Color -> item.name.takeIf { !it.isNullOrBlank() } ?: "ID: ${item.id}"
                            is Material -> item.name.takeIf { !it.isNullOrBlank() } ?: "ID: ${item.id}"
                            is Model -> item.name.takeIf { !it.isNullOrBlank() } ?: "ID: ${item.id}"
                            else -> item.toString()
                        }
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    currentSelectedId?.let { currentId ->
                        val position = newList.indexOfFirst { item ->
                            when (item) {
                                is Color -> item.id == currentId
                                is Material -> item.id == currentId
                                is Model -> item.id == currentId
                                else -> false
                            }
                        }
                        if (position >= 0) spinner.setSelection(position, false)
                        else if (spinner.adapter.count > 0) spinner.setSelection(0)
                    } ?: if (spinner.adapter.count > 0) spinner.setSelection(0) else TODO()
                } else handleApiError(dataTypeName, response.code(), response.message())
            }
            override fun onFailure(call: Call<List<T>>, t: Throwable) {
                if (!isAdded || _binding == null || context == null) return
                handleApiFailure(dataTypeName, t)
            }
        }
    }

    // --- handleApiError / handleApiFailure (Spinners) ---
    private fun handleApiError(dataType: String, code: Int, message: String?) {
        Log.e("EditUrnaFragment", "Error API $dataType: $code - $message")
        if (context != null) Toast.makeText(context, "Error cargando $dataType", Toast.LENGTH_SHORT).show()
    }
    private fun handleApiFailure(dataType: String, t: Throwable) {
        Log.e("EditUrnaFragment", "Fallo red $dataType", t)
        if (context != null) Toast.makeText(context, "Fallo red $dataType", Toast.LENGTH_SHORT).show()
    }

    // --- Selección Imagen y Permisos (Unificado) ---
    private fun selectImage(isForMain: Boolean) {
        if (context == null) return
        isSelectingForMainImage = isForMain
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else { Manifest.permission.READ_EXTERNAL_STORAGE }
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> launchGallery()
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(requireContext(), "Se necesita permiso...", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    // Lanza el selector adecuado
    private fun launchGallery() {
        if (context == null || activity == null) return
        try {
            if (isSelectingForMainImage) {
                // Selector SIMPLE para imagen principal
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    pickSingleImageLauncher.launch(intent)
                } else throw ActivityNotFoundException("No gallery app found") as Throwable
            } else {
                // Selector MÚLTIPLE para galería
                pickMultipleImagesLauncher.launch("image/*") // Usa el launcher múltiple directo
            }
        } catch (e: Exception) {
            Log.e("EditUrnaFragment", "Error al lanzar galería: ${e.message}")
            Toast.makeText(requireContext(), "No se pudo abrir la galería.", Toast.LENGTH_SHORT).show()
        }
    }


    // --- Guardar Cambios Urna (PATCH /urn) ---
    // Mantiene el flujo de 2 pasos si la imagen principal cambió
    private fun saveUrnaChanges() {
        if (context == null || _binding == null || !isAdded) return
        val urnaId = currentUrna?.id ?: return

        // --- Validaciones ---
        val name = binding.etName.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val stockStr = binding.etStock.text.toString().trim()
        var isValid = true
        binding.tilName.error = if (name.isBlank()) { isValid = false; "Nombre obligatorio" } else null
        val price = priceStr.toDoubleOrNull()
        binding.tilPrice.error = if (price == null) { isValid = false; "Precio inválido" } else null
        val stock = stockStr.toIntOrNull()
        binding.tilStock.error = if (stock == null) { isValid = false; "Stock inválido" } else null
        if (!isValid) {
            Toast.makeText(requireContext(), "Corrige los errores", Toast.LENGTH_SHORT).show()
            return
        }
        val shortDesc = binding.etShortDescription.text.toString().trim()
        val detailedDesc = binding.etDetailedDescription.text.toString().trim()
        val isAvailable = binding.switchAvailable.isChecked
        val colorId = getIdFromSpinnerSelection(binding.spinnerColor, colorsList)
        val materialId = getIdFromSpinnerSelection(binding.spinnerMaterial, materialsList)
        val modelId = getIdFromSpinnerSelection(binding.spinnerModel, modelsList)
        // --- Fin Validaciones ---

        showLoading(true)

        // Si se seleccionó una NUEVA imagen principal -> Flujo de 2 pasos
        if (selectedMainImageUri != null) {
            Log.d("EditUrnaFragment", "Guardando cambios CON nueva imagen principal...")
            try {
                // PASO 1: Subir nueva imagen a /upload/image
                val imagePart = createImagePart(requireContext(), selectedMainImageUri!!, "image") // Campo esperado por /upload/image
                uploadService.uploadImage(imagePart).enqueue(object : Callback<ImageUrl> {
                    override fun onResponse(call: Call<ImageUrl>, response: Response<ImageUrl>) {
                        if (!isAdded || _binding == null) return
                        if (response.isSuccessful && response.body() != null) {
                            Log.d("EditUrnaFragment", "Paso 1 (Guardar) Exitoso. Nueva ImageUrl: ${response.body()!!.path}")
                            // PASO 2: Actualizar urna con PATCH /urn/{id}, enviando la NUEVA ImageUrl
                            updateUrnaApiCall(urnaId, name, price!!, stock!!, shortDesc, detailedDesc, isAvailable, colorId, materialId, modelId, response.body()!!)
                        } else {
                            Log.e("EditUrnaFragment", "Error Paso 1 (Guardar): ${response.code()}")
                            handleUploadError(response) // Maneja error de subida y oculta loading
                        }
                    }
                    override fun onFailure(call: Call<ImageUrl>, t: Throwable) {
                        if (!isAdded || _binding == null) return
                        Log.e("EditUrnaFragment", "Fallo red Paso 1 (Guardar)", t)
                        handleUploadFailure(t) // Maneja fallo de red y oculta loading
                    }
                })
            } catch (e: Exception) {
                Log.e("EditUrnaFragment", "Error creando Part Paso 1 (Guardar)", e)
                handleImageProcessingError(e) // Maneja error local y oculta loading
            }
        } else {
            // SI NO se seleccionó nueva imagen principal -> Actualizar solo datos (ImageUrl = null)
            Log.d("EditUrnaFragment", "Guardando cambios SIN nueva imagen principal...")
            updateUrnaApiCall(urnaId, name, price!!, stock!!, shortDesc, detailedDesc, isAvailable, colorId, materialId, modelId, null)
        }
    }

    // --- Helper getIdFromSpinnerSelection (Genérico) ---
    private fun <T> getIdFromSpinnerSelection(spinner: Spinner, dataList: List<T>): Int? where T: Any {
        val position = spinner.selectedItemPosition
        if (position < 0 || position >= dataList.size) return null
        return try {
            when (val item = dataList[position]) {
                is Color -> item.id
                is Material -> item.id
                is Model -> item.id
                else -> null
            }
        } catch (e: IndexOutOfBoundsException) { null }
    }

    // --- updateUrnaApiCall (Llama a PATCH /urn) ---
    private fun updateUrnaApiCall(
        urnaId: Int, name: String, price: Double, stock: Int, shortDesc: String,
        detailedDesc: String, available: Boolean, colorId: Int?, materialId: Int?,
        modelId: Int?, newImageUrl: ImageUrl? // Acepta ImageUrl o null
    ) {
        if (context == null || _binding == null || !isAdded) return

        // Construir dataMap SOLO con campos modificados
        val dataMap = mutableMapOf<String, @JvmSuppressWildcards Any?>()
        if (name != currentUrna?.name) dataMap["name"] = name
        if (abs(price - (currentUrna?.price ?: -1.0)) > 0.001) dataMap["price"] = price
        if (stock != currentUrna?.stock) dataMap["stock"] = stock
        if (available != currentUrna?.available) dataMap["available"] = available
        if (shortDesc != (currentUrna?.short_description ?: "")) dataMap["short_description"] = shortDesc
        if (detailedDesc != (currentUrna?.detailed_description ?: "")) dataMap["detailed_description"] = detailedDesc
        if (colorId != currentUrna?.color_id) dataMap["color_id"] = colorId
        if (materialId != currentUrna?.material_id) dataMap["material_id"] = materialId
        if (modelId != currentUrna?.model_id) dataMap["model_id"] = modelId
        if (newImageUrl != null) dataMap["image_url"] = newImageUrl // Clave para imagen principal

        if (dataMap.isEmpty()) {
            Toast.makeText(context, "No se detectaron cambios", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        Log.d("EditUrnaFragment", "Paso 2 (Guardar): Llamando a PATCH /urn/$urnaId con datos: $dataMap")
        val finalDataMap = dataMap.filterValues { it != null } as Map<String, Any>

        urnaService.updateUrna(urnaId, finalDataMap).enqueue(object : Callback<Urna> {
            override fun onResponse(call: Call<Urna>, response: Response<Urna>) {
                if (!isAdded || _binding == null) return
                showLoading(false)
                if (response.isSuccessful) {
                    Log.i("EditUrnaFragment", "Paso 2 (Guardar) Exitoso. Urna actualizada.")
                    Toast.makeText(context, "Urna actualizada correctamente", Toast.LENGTH_SHORT).show()
                    // Limpiar la URI seleccionada para evitar re-subida accidental si se vuelve a guardar
                    selectedMainImageUri = null
                    parentFragmentManager.popBackStack() // Volver a la pantalla anterior
                } else {
                    val errorBody = response.errorBody()?.string() ?: "N/A"
                    Log.e("EditUrnaFragment", "Error API ${response.code()} en Paso 2 (Guardar): $errorBody")
                    Toast.makeText(context, "Error ${response.code()} al guardar cambios", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Urna>, t: Throwable) {
                if (!isAdded || _binding == null) return
                showLoading(false)
                Log.e("EditUrnaFragment", "Fallo red en Paso 2 (Guardar)", t)
                Toast.makeText(context, "Fallo de red al guardar: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Sube UNA imagen usando la función addUrnaImageMultipart (que usa @Multipart)
    private fun uploadSingleImageToGalleryMultipart(urnaId: Int, imageUri: Uri, imageIndex: Int) {
        if (context == null || !isAdded) return // Doble chequeo

        try {
            // *** CORRECCIÓN AQUÍ: Crear RequestBody y MultipartBody.Part ***
            // Crear partes Multipart para los datos de texto ANTES de la llamada
            val urnaIdPart = createTextPart(urnaId.toString())
            // Puedes hacer que el alt sea más descriptivo o dejarlo genérico
            val altPart = createTextPart("Galería ${System.currentTimeMillis()}") // Placeholder como ejemplo
            val isCoverPart = createTextPart("false") // Convertir booleano a String
            val sortOrderPart: RequestBody? = null // Crear si tienes valor, ej: createTextPart("1")

            // Crear parte Multipart para la imagen
            // ¡CRUCIAL! Usa el nombre "url" o el que espere tu API POST /urn_image para el archivo
            val imageFilePart = createImagePart(requireContext(), imageUri, "url") // Usa "url" o el nombre correcto

            Log.d("EditUrnaFragment", "Llamando a addUrnaImageMultipart para imagen ${imageIndex + 1}...")

            // *** Llamar a addUrnaImageMultipart con las partes CREADAS ***
            urnaImageService.addUrnaImageMultipart(
                urnaId = urnaIdPart,        // Pasar RequestBody creado
                altText = altPart,         // Pasar RequestBody creado (o null)
                isCover = isCoverPart,      // Pasar RequestBody creado (o null)
                sortOrder = sortOrderPart,   // Pasar RequestBody creado (o null)
                imageFile = imageFilePart    // Pasar MultipartBody.Part creado
            ).enqueue(object : Callback<UrnaImage> { //
                override fun onResponse(call: Call<UrnaImage>, response: Response<UrnaImage>) {
                    if (!isAdded) return // Verificar fragmento
                    if (response.isSuccessful) {
                        Log.i("EditUrnaFragment", "Img Galería ${imageIndex + 1} subida OK (Multipart).")
                        uploadSuccessCount++
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "N/A"
                        Log.e("EditUrnaFragment", "Error API (${response.code()}) subiendo img galería ${imageIndex + 1} (Multipart): $errorBody")
                        uploadErrorCount++
                    }
                    checkUploadCompletion() // Verificar si terminaron todas
                }
                override fun onFailure(call: Call<UrnaImage>, t: Throwable) {
                    if (!isAdded) return
                    Log.e("EditUrnaFragment", "Fallo red subiendo img galería ${imageIndex + 1} (Multipart)", t)
                    uploadErrorCount++
                    checkUploadCompletion() // Verificar si terminaron todas
                }
            }) //

        } catch (e: Exception) {
            // Error creando las partes (ej. leer archivo)
            if (!isAdded) return
            Log.e("EditUrnaFragment", "Error preparando img galería ${imageIndex + 1} (Multipart)", e)
            uploadErrorCount++
            if (context != null) Toast.makeText(context, "Error procesando imagen ${imageIndex + 1}", Toast.LENGTH_SHORT).show()
            checkUploadCompletion() // Verificar si terminaron todas
        }
    }

    // --- checkUploadCompletion y updateUploadProgressToast ---
    private fun checkUploadCompletion() {
        if (!isAdded || _binding == null) return
        val processedCount = uploadSuccessCount + uploadErrorCount
        updateUploadProgressToast() // Actualiza contador en Toast
        if (processedCount == uploadTotalCount) { // Si ya se procesaron todas
            showLoading(false) // Ocultar loading
            Log.d("EditUrnaFragment", "Subida múltiple a galería (Multipart) completa. Éxitos: $uploadSuccessCount, Errores: $uploadErrorCount")
            val message = if (uploadErrorCount == 0) "Se añadieron $uploadSuccessCount imágenes a la galería."
            else "Se añadieron $uploadSuccessCount de $uploadTotalCount. Errores: $uploadErrorCount."
            if (context != null) Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            // Aquí podrías querer refrescar la galería si la muestras en este fragment
            // o indicar al fragmento de detalle que refresque si se navega de vuelta.
        }
    }

    private fun updateUploadProgressToast() {
        if (context == null || !isAdded) return
        val processedCount = uploadSuccessCount + uploadErrorCount
        var message = ""
        // Mostrar progreso solo si hay imágenes para subir y no han terminado todas
        if (uploadTotalCount > 0 && processedCount < uploadTotalCount) {
            message = "Subiendo galería $processedCount de $uploadTotalCount... (E:$uploadErrorCount)"
        } else if (processedCount == 0 && uploadTotalCount > 0) {
            message = "Iniciando subida de $uploadTotalCount imágenes a galería..."
        } // No mostramos nada si processed == total o total es 0

        progressToast?.cancel() // Cancela el anterior
        if (message.isNotEmpty()) {
            progressToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            progressToast?.show()
        } else {
            progressToast = null // Limpia la referencia si ya no se muestra
        }
    }


    // --- Diálogo y Lógica de Borrado ---
    private fun showDeleteConfirmationDialog() {
        if (context == null || !isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Eliminar esta urna permanentemente?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Eliminar") { _, _ -> deleteUrna() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUrna() {
        if (context == null || _binding == null || !isAdded) return
        val urnaId = currentUrna?.id ?: return
        showLoading(true)
        urnaService.deleteUrna(urnaId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (!isAdded || _binding == null) return
                showLoading(false)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Urna eliminada", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE) // Limpia pila
                } else {
                    Log.e("EditUrnaFragment", "Error ${response.code()} eliminando: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Error ${response.code()} al eliminar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (!isAdded || _binding == null) return
                showLoading(false)
                Log.e("EditUrnaFragment", "Fallo red al eliminar", t)
                Toast.makeText(context, "Fallo red al eliminar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Función principal que itera y sube cada imagen seleccionada
    private fun uploadMultipleImagesToGalleryMultipart(uris: List<Uri>) {
        // ... (verificaciones iniciales) ...
        val urnaId = currentUrna?.id ?: return

        showLoading(true)
        // ... (inicializar contadores) ...
        updateUploadProgressToast()

        // Itera y llama a la función de subida individual CORRECTA
        uris.forEachIndexed { index, uri ->
            Log.d("EditUrnaFragment", "Subiendo imagen ${index + 1} a galería (Multipart)...")
            uploadSingleImageToGalleryMultipart(urnaId, uri, index) // Llama a la función de abajo
        }
    }

    // Sube UNA imagen usando addUrnaImageMultipart (ESTA ES LA FUNCIÓN CORREGIDA)


    // --- Helpers ---
    // Crea RequestBody de texto plano
    private fun createTextPart(text: String): RequestBody {
        return text.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // Muestra/oculta ProgressBar
    private fun showLoading(isLoading: Boolean) {
        _binding?.let {
            it.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Deshabilitar botones interactivos durante carga/subida
            it.btnSaveChanges.isEnabled = !isLoading
            it.btnDeleteUrna.isEnabled = !isLoading
            it.btnSelectImage.isEnabled = !isLoading
            it.btnAddGalleryImage.isEnabled = !isLoading
        }
    }

    // Errores de subida genérica (/upload/image) - Usados por saveUrnaChanges
    private fun handleUploadError(response: Response<*>) {
        if (!isAdded || _binding == null) return
        val errorBody = response.errorBody()?.string() ?: "N/A"
        Log.e("EditUrnaFragment", "Error API subiendo imagen (genérico): ${response.code()}. Body: $errorBody")
        if (context != null) Toast.makeText(requireContext(), "Error ${response.code()} (Subida Img)", Toast.LENGTH_SHORT).show()
        showLoading(false)
    }
    private fun handleUploadFailure(t: Throwable) {
        if (!isAdded || _binding == null) return
        Log.e("EditUrnaFragment", "Fallo red (Subida Img genérico)", t)
        if (context != null) Toast.makeText(requireContext(), "Fallo red (Subida Img): ${t.message}", Toast.LENGTH_SHORT).show()
        showLoading(false)
    }
    private fun handleImageProcessingError(e: Exception) {
        if (!isAdded || _binding == null) return
        Log.e("EditUrnaFragment", "Excepción procesando imagen", e)
        if (context != null) Toast.makeText(requireContext(), "Error procesando imagen", Toast.LENGTH_SHORT).show()
        showLoading(false)
    }

    // Crea MultipartBody.Part para una imagen Uri
    @Throws(IllegalStateException::class, IOException::class)
    private fun createImagePart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part {
        val mimeType = getMimeType(context, uri) ?: "image/jpeg"
        val fileName = getFileName(context, uri)
        Log.d("HelperUtil", "Creando imagePart: field='$fieldName', filename='$fileName', mime='$mimeType'")
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("No se pudo abrir InputStream para Uri: $uri")
        val bytes = inputStream.use { it.readBytes() }
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        // El fieldName aquí ('url', 'image', etc.) debe coincidir con el @Part name O el nombre esperado por la API
        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }

    // Obtiene el MimeType de una Uri
    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)?.lowercase()
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.lowercase()
            )
    }

    // Obtiene el nombre de archivo de una Uri
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

    // --- onDestroyView ---
    override fun onDestroyView() {
        super.onDestroyView()
        progressToast?.cancel() // Cancela toast si sigue mostrándose
        _binding = null // Limpia binding para evitar fugas de memoria
    }
}