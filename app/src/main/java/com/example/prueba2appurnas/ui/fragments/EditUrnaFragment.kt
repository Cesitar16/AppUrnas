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
// import android.widget.ArrayAdapter // Ya no es necesario aquí
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
// import androidx.fragment.app.Fragment // Ahora hereda de BaseUrnaFormFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.*
import com.example.prueba2appurnas.databinding.FragmentEditUrnaBinding
import com.example.prueba2appurnas.model.*
import com.example.prueba2appurnas.util.NetUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.math.abs

// 1. Heredar de BaseUrnaFormFragment
class EditUrnaFragment : BaseUrnaFormFragment() {

    private var _binding: FragmentEditUrnaBinding? = null
    private val binding get() = _binding!!

    // Servicios Urna y Upload
    private lateinit var urnaService: UrnaService
    private lateinit var uploadService: UploadService
    private lateinit var urnaImageService: UrnaImageService

    // --- Listas y Servicios de Spinners (Color, Material, Model) movidos a BaseUrnaFormFragment ---

    private var currentUrna: Urna? = null
    private var selectedMainImageUri: Uri? = null
    private var isSelectingForMainImage: Boolean = true

    // Contadores de subida (sin cambios)
    private var uploadTotalCount = 0
    private var uploadSuccessCount = 0
    private var uploadErrorCount = 0
    private var progressToast: Toast? = null

    // Launchers de imágenes (sin cambios)
    private val pickSingleImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleImageSelectionResult(result, false)
        }
    private val pickMultipleImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
            handleImageSelectionResult(null, true, uris)
        }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("EditUrnaFragment", "Permiso concedido.")
                launchGallery()
            } else {
                Log.w("EditUrnaFragment", "Permiso denegado.")
                if (context != null) Toast.makeText(requireContext(), "Permiso necesario", Toast.LENGTH_LONG).show()
            }
        }

    // Función unificada (sin cambios)
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
            if (isSelectingForMainImage) {
                selectedMainImageUri = selectedUris[0]
                if (context != null && _binding != null) {
                    Glide.with(requireContext()).load(selectedMainImageUri).centerCrop()
                        .placeholder(R.drawable.bg_image_border).into(binding.ivUrnaImage)
                }
                Log.d("EditUrnaFragment", "Nueva img principal seleccionada: ${selectedMainImageUri}")
            } else {
                Log.d("EditUrnaFragment", "${selectedUris.size} imágenes seleccionadas para galería.")
                uploadMultipleImagesToGalleryMultipart(selectedUris)
            }
        } else {
            Log.w("EditUrnaFragment", "Selección de imagen(es) cancelada o vacía.")
        }
    }

    // --- newInstance (Sin cambios) ---
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

    override fun onCreate(savedInstanceState: Bundle?) {
        // Recuperar Urna ANTES de llamar a super.onCreate()
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

        super.onCreate(savedInstanceState) // Llama a onCreate de la base

        // Inicializar servicios restantes
        try {
            if (context == null) throw IllegalStateException("Contexto nulo al inicializar servicios")
            urnaService = RetrofitClient.getUrnaService(requireContext())
            uploadService = RetrofitClient.getUploadService(requireContext())
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
        super.onViewCreated(view, savedInstanceState) // Llama a onViewCreated de la base

        // --- INICIO DE LA CORRECCIÓN ---
        // Se eliminó la comprobación de ::colorService, ::materialService y ::modelService
        // Solo comprobamos las variables locales de ESTA clase.
        if (currentUrna == null || !::urnaService.isInitialized || !::uploadService.isInitialized ||
            !::urnaImageService.isInitialized) {
            Log.e("EditUrnaFragment", "Fallo en inicialización (urna o servicios locales) o datos. Cerrando.")
            if(context != null) Toast.makeText(context, "Error al iniciar pantalla", Toast.LENGTH_SHORT).show()
            if (isAdded) parentFragmentManager.popBackStack()
            return
        }
        // --- FIN DE LA CORRECCIÓN ---

        populateFields()

        // 2. Llamar al método de carga de la clase base
        // (Este método YA comprueba internamente los servicios de la base)
        loadSpinnersSequentially()

        // Listeners
        binding.btnSelectImage.setOnClickListener { selectImage(true) }
        binding.btnAddGalleryImage.setOnClickListener { selectImage(false) }
        binding.btnSaveChanges.setOnClickListener { saveUrnaChanges() }
        binding.btnDeleteUrna.setOnClickListener { showDeleteConfirmationDialog() }
    }

    // --- 3. Implementar métodos abstractos de la clase base ---

    override fun showMainLoading(isLoading: Boolean) {
        _binding?.let {
            it.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            it.btnSaveChanges.isEnabled = !isLoading
            it.btnDeleteUrna.isEnabled = !isLoading
            it.btnSelectImage.isEnabled = !isLoading
            it.btnAddGalleryImage.isEnabled = !isLoading
        }
    }

    override fun showSpinnerLoading(isLoading: Boolean) {
        _binding?.let {
            // Usamos el mismo ProgressBar general
            it.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            it.spinnerColor.isEnabled = !isLoading
            it.spinnerMaterial.isEnabled = !isLoading
            it.spinnerModel.isEnabled = !isLoading
        }
    }

    override fun getColorSpinner(): Spinner? = _binding?.spinnerColor
    override fun getMaterialSpinner(): Spinner? = _binding?.spinnerMaterial
    override fun getModelSpinner(): Spinner? = _binding?.spinnerModel

    // Para "Editar", devolvemos el ID de la urna actual
    override fun getSelectedColorId(): Int? = currentUrna?.color_id
    override fun getSelectedMaterialId(): Int? = currentUrna?.material_id
    override fun getSelectedModelId(): Int? = currentUrna?.model_id


    // --- populateFields (Sin cambios) ---
    private fun populateFields() {
        currentUrna?.let { urna ->
            binding.etName.setText(urna.name ?: "")
            binding.etPrice.setText(urna.price?.toString() ?: "")
            binding.etStock.setText(urna.stock?.toString() ?: "")
            binding.etShortDescription.setText(urna.short_description ?: "")
            binding.etDetailedDescription.setText(urna.detailed_description ?: "")
            binding.switchAvailable.isChecked = urna.available ?: true
            binding.etInternalId.setText(urna.internal_id ?: "")
            binding.etWidth.setText(urna.width?.toString() ?: "")
            binding.etDepth.setText(urna.depth?.toString() ?: "")
            binding.etHeight.setText(urna.height?.toString() ?: "")
            binding.etWeight.setText(urna.weight?.toString() ?: "")

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

    // --- Toda la lógica de carga de spinners (loadSpinners, createSpinnerCallback, etc.) FUE MOVIDA A LA BASE ---

    // --- Selección Imagen y Permisos (Sin cambios) ---
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

    private fun launchGallery() {
        if (context == null || activity == null) return
        try {
            if (isSelectingForMainImage) {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    pickSingleImageLauncher.launch(intent)
                } else throw ActivityNotFoundException("No gallery app found") as Throwable
            } else {
                pickMultipleImagesLauncher.launch("image/*")
            }
        } catch (e: Exception) {
            Log.e("EditUrnaFragment", "Error al lanzar galería: ${e.message}")
            Toast.makeText(requireContext(), "No se pudo abrir la galería.", Toast.LENGTH_SHORT).show()
        }
    }


    // --- Guardar Cambios Urna (PATCH /urn) ---
    private fun saveUrnaChanges() {
        if (context == null || _binding == null || !isAdded) return
        val urnaId = currentUrna?.id ?: return

        val name = binding.etName.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val stockStr = binding.etStock.text.toString().trim()
        val shortDesc = binding.etShortDescription.text.toString().trim()
        val detailedDesc = binding.etDetailedDescription.text.toString().trim()
        val isAvailable = binding.switchAvailable.isChecked

        // 4. Usar el método de la clase base para obtener los IDs
        val colorId = getIdFromSpinnerSelection(binding.spinnerColor, colorsList)
        val materialId = getIdFromSpinnerSelection(binding.spinnerMaterial, materialsList)
        val modelId = getIdFromSpinnerSelection(binding.spinnerModel, modelsList)

        val internalId = binding.etInternalId.text.toString().trim()
        val widthStr = binding.etWidth.text.toString().trim()
        val depthStr = binding.etDepth.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()


        // Validaciones (sin cambios)
        var isValid = true
        binding.tilName.error = if (name.isBlank()) { isValid = false; "Nombre obligatorio" } else null
        val price = priceStr.toDoubleOrNull()
        binding.tilPrice.error = if (priceStr.isNotBlank() && price == null) { isValid = false; "Precio inválido" } else null
        val stock = stockStr.toIntOrNull()
        binding.tilStock.error = if (stockStr.isNotBlank() && stock == null) { isValid = false; "Stock inválido" } else null
        val width = widthStr.toDoubleOrNull()
        binding.tilWidth.error = if(widthStr.isNotBlank() && width == null) { isValid = false; "Ancho inválido"} else null
        val depth = depthStr.toDoubleOrNull()
        binding.tilDepth.error = if(depthStr.isNotBlank() && depth == null) { isValid = false; "Prof. inválida"} else null
        val height = heightStr.toDoubleOrNull()
        binding.tilHeight.error = if(heightStr.isNotBlank() && height == null) { isValid = false; "Alto inválido"} else null
        val weight = weightStr.toDoubleOrNull()
        binding.tilWeight.error = if(weightStr.isNotBlank() && weight == null) { isValid = false; "Peso inválido"} else null


        if (!isValid) {
            Toast.makeText(requireContext(), "Corrige los errores marcados", Toast.LENGTH_SHORT).show()
            return
        }

        showMainLoading(true) // Usar el método implementado

        // Lógica de subida (sin cambios)
        if (selectedMainImageUri != null) {
            Log.d("EditUrnaFragment", "Guardando cambios CON nueva imagen principal...")
            try {
                val imagePart = createImagePart(requireContext(), selectedMainImageUri!!, "image")
                uploadService.uploadImage(imagePart).enqueue(object : Callback<ImageUrl> {
                    override fun onResponse(call: Call<ImageUrl>, response: Response<ImageUrl>) {
                        if (!isAdded || _binding == null) return
                        if (response.isSuccessful && response.body() != null) {
                            Log.d("EditUrnaFragment", "Paso 1 (Guardar) Exitoso. Nueva ImageUrl: ${response.body()!!.path}")
                            updateUrnaApiCall(urnaId, name, price, stock, shortDesc, detailedDesc, isAvailable, colorId, materialId, modelId, internalId, width, depth, height, weight, response.body()!!)
                        } else {
                            Log.e("EditUrnaFragment", "Error Paso 1 (Guardar): ${response.code()}")
                            handleUploadError(response)
                        }
                    }
                    override fun onFailure(call: Call<ImageUrl>, t: Throwable) {
                        if (!isAdded || _binding == null) return
                        Log.e("EditUrnaFragment", "Fallo red Paso 1 (Guardar)", t)
                        handleUploadFailure(t)
                    }
                })
            } catch (e: Exception) {
                Log.e("EditUrnaFragment", "Error creando Part Paso 1 (Guardar)", e)
                handleImageProcessingError(e)
            }
        } else {
            Log.d("EditUrnaFragment", "Guardando cambios SIN nueva imagen principal...")
            updateUrnaApiCall(urnaId, name, price, stock, shortDesc, detailedDesc, isAvailable, colorId, materialId, modelId, internalId, width, depth, height, weight, null)
        }
    }

    // --- updateUrnaApiCall (Sin cambios) ---
    private fun updateUrnaApiCall(
        urnaId: Int, name: String, price: Double?, stock: Int?, shortDesc: String?,
        detailedDesc: String?, available: Boolean, colorId: Int?, materialId: Int?,
        modelId: Int?,
        internalId: String?, width: Double?, depth: Double?, height: Double?, weight: Double?,
        newImageUrl: ImageUrl?
    ) {
        if (context == null || _binding == null || !isAdded) return

        val dataMap = mutableMapOf<String, @JvmSuppressWildcards Any?>()
        if (name != currentUrna?.name) dataMap["name"] = name
        if (price != null && abs(price - (currentUrna?.price ?: Double.NaN)) > 0.001) dataMap["price"] = price else if (price == null && currentUrna?.price != null) dataMap["price"] = null
        if (stock != currentUrna?.stock) dataMap["stock"] = stock
        if (available != currentUrna?.available) dataMap["available"] = available
        if (shortDesc != (currentUrna?.short_description ?: "")) dataMap["short_description"] = shortDesc
        if (detailedDesc != (currentUrna?.detailed_description ?: "")) dataMap["detailed_description"] = detailedDesc
        if (colorId != currentUrna?.color_id) dataMap["color_id"] = colorId
        if (materialId != currentUrna?.material_id) dataMap["material_id"] = materialId
        if (modelId != currentUrna?.model_id) dataMap["model_id"] = modelId
        if (newImageUrl != null) dataMap["image_url"] = newImageUrl
        if (internalId != (currentUrna?.internal_id ?: "")) dataMap["internal_id"] = internalId
        if (width != null && abs(width - (currentUrna?.width ?: Double.NaN)) > 0.001) dataMap["width"] = width else if (width == null && currentUrna?.width != null) dataMap["width"] = null
        if (depth != null && abs(depth - (currentUrna?.depth ?: Double.NaN)) > 0.001) dataMap["depth"] = depth else if (depth == null && currentUrna?.depth != null) dataMap["depth"] = null
        if (height != null && abs(height - (currentUrna?.height ?: Double.NaN)) > 0.001) dataMap["height"] = height else if (height == null && currentUrna?.height != null) dataMap["height"] = null
        if (weight != null && abs(weight - (currentUrna?.weight ?: Double.NaN)) > 0.001) dataMap["weight"] = weight else if (weight == null && currentUrna?.weight != null) dataMap["weight"] = null

        if (dataMap.isEmpty()) {
            Toast.makeText(context, "No se detectaron cambios", Toast.LENGTH_SHORT).show()
            showMainLoading(false) // Usar el método implementado
            return
        }

        Log.d("EditUrnaFragment", "Paso 2 (Guardar): Llamando a PATCH /urn/$urnaId con datos: $dataMap")
        val finalDataMap = dataMap.filterValues { it != null } as Map<String, Any>

        urnaService.updateUrna(urnaId, finalDataMap).enqueue(object : Callback<Urna> {
            override fun onResponse(call: Call<Urna>, response: Response<Urna>) {
                if (!isAdded || _binding == null) return
                showMainLoading(false) // Usar el método implementado
                if (response.isSuccessful) {
                    Log.i("EditUrnaFragment", "Paso 2 (Guardar) Exitoso. Urna actualizada.")
                    Toast.makeText(context, "Urna actualizada correctamente", Toast.LENGTH_SHORT).show()
                    selectedMainImageUri = null
                    parentFragmentManager.popBackStack()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "N/A"
                    Log.e("EditUrnaFragment", "Error API ${response.code()} en Paso 2 (Guardar): $errorBody")
                    Toast.makeText(context, "Error ${response.code()} al guardar cambios", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Urna>, t: Throwable) {
                if (!isAdded || _binding == null) return
                showMainLoading(false) // Usar el método implementado
                Log.e("EditUrnaFragment", "Fallo red en Paso 2 (Guardar)", t)
                Toast.makeText(context, "Fallo de red al guardar: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- Lógica de subida múltiple a galería (Sin cambios) ---
    private fun uploadSingleImageToGalleryMultipart(urnaId: Int, imageUri: Uri, imageIndex: Int) {
        if (context == null || !isAdded) return

        try {
            val urnaIdPart = createTextPart(urnaId.toString())
            val altPart = createTextPart("Galería ${System.currentTimeMillis()}")
            val isCoverPart = createTextPart("false")
            val sortOrderPart: RequestBody? = null
            val imageFilePart = createImagePart(requireContext(), imageUri, "url")

            Log.d("EditUrnaFragment", "Llamando a addUrnaImageMultipart para imagen ${imageIndex + 1}...")

            urnaImageService.addUrnaImageMultipart(
                urnaId = urnaIdPart,
                altText = altPart,
                isCover = isCoverPart,
                sortOrder = sortOrderPart,
                imageFile = imageFilePart
            ).enqueue(object : Callback<UrnaImage> {
                override fun onResponse(call: Call<UrnaImage>, response: Response<UrnaImage>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        Log.i("EditUrnaFragment", "Img Galería ${imageIndex + 1} subida OK (Multipart).")
                        uploadSuccessCount++
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "N/A"
                        Log.e("EditUrnaFragment", "Error API (${response.code()}) subiendo img galería ${imageIndex + 1} (Multipart): $errorBody")
                        uploadErrorCount++
                    }
                    checkUploadCompletion()
                }
                override fun onFailure(call: Call<UrnaImage>, t: Throwable) {
                    if (!isAdded) return
                    Log.e("EditUrnaFragment", "Fallo red subiendo img galería ${imageIndex + 1} (Multipart)", t)
                    uploadErrorCount++
                    checkUploadCompletion()
                }
            })

        } catch (e: Exception) {
            if (!isAdded) return
            Log.e("EditUrnaFragment", "Error preparando img galería ${imageIndex + 1} (Multipart)", e)
            uploadErrorCount++
            if (context != null) Toast.makeText(context, "Error procesando imagen ${imageIndex + 1}", Toast.LENGTH_SHORT).show()
            checkUploadCompletion()
        }
    }

    private fun checkUploadCompletion() {
        if (!isAdded || _binding == null) return
        val processedCount = uploadSuccessCount + uploadErrorCount
        updateUploadProgressToast()
        if (processedCount == uploadTotalCount) {
            showMainLoading(false) // Usar el método implementado
            Log.d("EditUrnaFragment", "Subida múltiple a galería (Multipart) completa. Éxitos: $uploadSuccessCount, Errores: $uploadErrorCount")
            val message = if (uploadErrorCount == 0) "Se añadieron $uploadSuccessCount imágenes a la galería."
            else "Se añadieron $uploadSuccessCount de $uploadTotalCount. Errores: $uploadErrorCount."
            if (context != null) Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUploadProgressToast() {
        if (context == null || !isAdded) return
        val processedCount = uploadSuccessCount + uploadErrorCount
        var message = ""
        if (uploadTotalCount > 0 && processedCount < uploadTotalCount) {
            message = "Subiendo galería $processedCount de $uploadTotalCount... (E:$uploadErrorCount)"
        } else if (processedCount == 0 && uploadTotalCount > 0) {
            message = "Iniciando subida de $uploadTotalCount imágenes a galería..."
        }
        progressToast?.cancel()
        if (message.isNotEmpty()) {
            progressToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            progressToast?.show()
        } else {
            progressToast = null
        }
    }

    private fun uploadMultipleImagesToGalleryMultipart(uris: List<Uri>) {
        val urnaId = currentUrna?.id ?: return

        showMainLoading(true) // Usar el método implementado
        uploadTotalCount = uris.size
        uploadSuccessCount = 0
        uploadErrorCount = 0
        updateUploadProgressToast()

        uris.forEachIndexed { index, uri ->
            Log.d("EditUrnaFragment", "Subiendo imagen ${index + 1} a galería (Multipart)...")
            uploadSingleImageToGalleryMultipart(urnaId, uri, index)
        }
    }


    // --- Diálogo y Lógica de Borrado (Sin cambios) ---
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
        showMainLoading(true) // Usar el método implementado
        urnaService.deleteUrna(urnaId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (!isAdded || _binding == null) return
                showMainLoading(false) // Usar el método implementado
                if (response.isSuccessful) {
                    Toast.makeText(context, "Urna eliminada", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                } else {
                    Log.e("EditUrnaFragment", "Error ${response.code()} eliminando: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Error ${response.code()} al eliminar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (!isAdded || _binding == null) return
                showMainLoading(false) // Usar el método implementado
                Log.e("EditUrnaFragment", "Fallo red al eliminar", t)
                Toast.makeText(context, "Fallo red al eliminar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- Helpers (Sin cambios) ---
    private fun createTextPart(text: String): RequestBody {
        return text.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun handleUploadError(response: Response<*>) {
        if (!isAdded || _binding == null) return
        val errorBody = response.errorBody()?.string() ?: "N/A"
        Log.e("EditUrnaFragment", "Error API subiendo imagen (genérico): ${response.code()}. Body: $errorBody")
        if (context != null) Toast.makeText(requireContext(), "Error ${response.code()} (Subida Img)", Toast.LENGTH_SHORT).show()
        showMainLoading(false) // Usar el método implementado
    }
    private fun handleUploadFailure(t: Throwable) {
        if (!isAdded || _binding == null) return
        Log.e("EditUrnaFragment", "Fallo red (Subida Img genérico)", t)
        if (context != null) Toast.makeText(requireContext(), "Fallo red (Subida Img): ${t.message}", Toast.LENGTH_SHORT).show()
        showMainLoading(false) // Usar el método implementado
    }
    private fun handleImageProcessingError(e: Exception) {
        if (!isAdded || _binding == null) return
        Log.e("EditUrnaFragment", "Excepción procesando imagen", e)
        if (context != null) Toast.makeText(requireContext(), "Error procesando imagen", Toast.LENGTH_SHORT).show()
        showMainLoading(false) // Usar el método implementado
    }

    @Throws(IllegalStateException::class, IOException::class)
    private fun createImagePart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part {
        val mimeType = getMimeType(context, uri) ?: "image/jpeg"
        val fileName = getFileName(context, uri)
        Log.d("HelperUtil", "Creando imagePart: field='$fieldName', filename='$fileName', mime='$mimeType'")
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("No se pudo abrir InputStream para Uri: $uri")
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

    // --- onDestroyView ---
    override fun onDestroyView() {
        super.onDestroyView()
        progressToast?.cancel()
        _binding = null
    }
}