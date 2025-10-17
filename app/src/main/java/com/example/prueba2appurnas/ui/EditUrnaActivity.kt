package com.example.prueba2appurnas.ui

// --- IMPORTS (Mantén los mismos imports que la versión anterior) ---
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.*
import com.example.prueba2appurnas.databinding.ActivityEditUrnaBinding // Correcto
import com.example.prueba2appurnas.model.*
import com.example.prueba2appurnas.util.NetUtils


class EditUrnaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditUrnaBinding // El nombre de la clase Binding es correcto
    private lateinit var urnaService: UrnaService
    private lateinit var uploadService: UploadService
    private lateinit var colorService: ColorService
    private lateinit var materialService: MaterialService
    private lateinit var modelService: ModelService

    private var colorsList: List<Color> = emptyList()
    private var materialsList: List<Material> = emptyList()
    private var modelsList: List<Model> = emptyList()

    private var currentUrna: Urna? = null
    private var selectedImageUri: Uri? = null

    // --- ActivityResultLaunchers ---
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.bg_image_border)
                        .error(R.drawable.bg_image_border)
                        // *** ID CORREGIDO PARA IMAGEN ***
                        .into(binding.ivUrnaImage) // Usa el ID del XML: ivUrnaImage
                } else {
                    Log.w("EditUrnaActivity", "ActivityResult: La Uri de la imagen es nula.")
                }
            } else {
                Log.d("EditUrnaActivity", "ActivityResult: Selección de imagen cancelada o fallida.")
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("EditUrnaActivity", "Permiso concedido via launcher.")
                launchGallery()
            } else {
                Log.w("EditUrnaActivity", "Permiso denegado via launcher.")
                Toast.makeText(this, "Permiso necesario para seleccionar imágenes", Toast.LENGTH_LONG).show()
            }
        }

    // --- onCreate ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUrnaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar servicios
        try {
            urnaService = RetrofitClient.getUrnaService(this)
            uploadService = RetrofitClient.getUploadService(this)
            colorService = RetrofitClient.getColorService(this)
            materialService = RetrofitClient.getMaterialService(this)
            modelService = RetrofitClient.getModelService(this)
        } catch (e: Exception) {
            Log.e("EditUrnaActivity", "Error crítico inicializando servicios Retrofit", e)
            Toast.makeText(this, "Error de configuración de red.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Obtener Urna del Intent
        currentUrna = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("urn", Urna::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("urn") as? Urna
        }

        if (currentUrna == null) {
            Log.e("EditUrnaActivity", "Objeto Urna nulo recibido del Intent.")
            Toast.makeText(this, "Error al cargar datos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d("EditUrnaActivity", "Editando Urna ID: ${currentUrna?.id}")

        populateFields()
        loadSpinners()

        // *** ID CORREGIDO PARA BOTÓN SELECCIONAR IMAGEN ***
        binding.btnSelectImage.setOnClickListener { selectImage() } // Usa el ID del XML: btnSelectImage
        binding.btnSaveChanges.setOnClickListener { saveUrnaChanges() }
        binding.btnDeleteUrna.setOnClickListener { showDeleteConfirmationDialog() }
    }

    // --- populateFields ---
    private fun populateFields() {
        currentUrna?.let { urna ->
            // *** IDs CORREGIDOS PARA EDITTEXTS Y SWITCH ***
            binding.etName.setText(urna.name ?: "") // Usa el ID del EditText interno
            binding.etPrice.setText(urna.price?.toString() ?: "")
            binding.etStock.setText(urna.stock?.toString() ?: "")
            binding.etShortDescription.setText(urna.short_description ?: "")
            binding.etDetailedDescription.setText(urna.detailed_description ?: "")
            binding.switchAvailable.isChecked = urna.available ?: true // Usa el ID del XML: switch_available

            // Cargar imagen actual
            val imagePath = urna.image_url?.path
            val fullImageUrl = NetUtils.buildAbsoluteUrl(imagePath)
            Log.d("EditUrnaActivity", "Populate: Cargando imagen actual desde: $fullImageUrl")
            val glideModel = fullImageUrl?.let { NetUtils.glideModelWithAuth(this, it) }

            Glide.with(this)
                .load(glideModel ?: R.drawable.bg_image_border)
                .placeholder(R.drawable.bg_image_border)
                .error(R.drawable.bg_image_border)
                .centerCrop()
                // *** ID CORREGIDO PARA IMAGEN ***
                .into(binding.ivUrnaImage) // Usa el ID del XML: ivUrnaImage
        } ?: Log.e("EditUrnaActivity", "currentUrna nulo en populateFields.")
    }

    // --- loadSpinners ---
    private fun loadSpinners() {
        // Cargar Colores
        colorService.getAllColors().enqueue(object : Callback<List<Color>> {
            override fun onResponse(call: Call<List<Color>>, response: Response<List<Color>>) {
                if (response.isSuccessful) {
                    colorsList = response.body() ?: emptyList()
                    Log.d("EditUrnaActivity", "Colores: ${colorsList.size}")
                    val colorNames = colorsList.map { it.name?.takeIf { n -> n.isNotBlank() } ?: "ID: ${it.id}" }
                    val adapter = ArrayAdapter(this@EditUrnaActivity, android.R.layout.simple_spinner_item, colorNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // *** ID CORREGIDO PARA SPINNER ***
                    binding.spinnerColor.adapter = adapter // Usa el ID del XML: spinner_color
                    currentUrna?.color_id?.let { currentId ->
                        val position = colorsList.indexOfFirst { it.id == currentId }
                        // *** ID CORREGIDO PARA SPINNER ***
                        if (position >= 0) binding.spinnerColor.setSelection(position, false)
                        else Log.w("EditUrnaActivity", "ID Color $currentId no hallado.")
                    }
                } else {
                    handleApiError("colores", response.code(), response.message())
                }
            }
            override fun onFailure(call: Call<List<Color>>, t: Throwable) { handleApiFailure("colores", t) }
        })

        // Cargar Materiales
        materialService.getAllMaterials().enqueue(object : Callback<List<Material>> {
            override fun onResponse(call: Call<List<Material>>, response: Response<List<Material>>) {
                if (response.isSuccessful) {
                    materialsList = response.body() ?: emptyList()
                    Log.d("EditUrnaActivity", "Materiales: ${materialsList.size}")
                    val materialNames = materialsList.map { it.name?.takeIf { n -> n.isNotBlank() } ?: "ID: ${it.id}" }
                    val adapter = ArrayAdapter(this@EditUrnaActivity, android.R.layout.simple_spinner_item, materialNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // *** ID CORREGIDO PARA SPINNER ***
                    binding.spinnerMaterial.adapter = adapter // Usa el ID del XML: spinner_material
                    currentUrna?.material_id?.let { currentId ->
                        val position = materialsList.indexOfFirst { it.id == currentId }
                        // *** ID CORREGIDO PARA SPINNER ***
                        if (position >= 0) binding.spinnerMaterial.setSelection(position, false)
                        else Log.w("EditUrnaActivity", "ID Material $currentId no hallado.")
                    }
                } else { handleApiError("materiales", response.code(), response.message()) }
            }
            override fun onFailure(call: Call<List<Material>>, t: Throwable) { handleApiFailure("materiales", t) }
        })

        // Cargar Modelos
        modelService.getAllModels().enqueue(object : Callback<List<Model>> {
            override fun onResponse(call: Call<List<Model>>, response: Response<List<Model>>) {
                if (response.isSuccessful) {
                    modelsList = response.body() ?: emptyList()
                    Log.d("EditUrnaActivity", "Modelos: ${modelsList.size}")
                    val modelNames = modelsList.map { it.name?.takeIf { n -> n.isNotBlank() } ?: "ID: ${it.id}" }
                    val adapter = ArrayAdapter(this@EditUrnaActivity, android.R.layout.simple_spinner_item, modelNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // *** ID CORREGIDO PARA SPINNER ***
                    binding.spinnerModel.adapter = adapter // Usa el ID del XML: spinner_model
                    currentUrna?.model_id?.let { currentId ->
                        val position = modelsList.indexOfFirst { it.id == currentId }
                        // *** ID CORREGIDO PARA SPINNER ***
                        if (position >= 0) binding.spinnerModel.setSelection(position, false)
                        else Log.w("EditUrnaActivity", "ID Modelo $currentId no hallado.")
                    }
                } else { handleApiError("modelos", response.code(), response.message()) }
            }
            override fun onFailure(call: Call<List<Model>>, t: Throwable) { handleApiFailure("modelos", t) }
        })
    }

    // --- Helpers para Manejo de Errores API en Spinners ---
    private fun handleApiError(dataType: String, code: Int, message: String?) { /* ... (igual que antes) ... */ }
    private fun handleApiFailure(dataType: String, t: Throwable) { /* ... (igual que antes) ... */ }

    // --- Lógica de Selección de Imagen y Permisos ---
    private fun selectImage() { /* ... (igual que antes) ... */ }
    private fun launchGallery() { /* ... (igual que antes) ... */ }

    // --- saveUrnaChanges ---
    private fun saveUrnaChanges() {
        val urnaId = currentUrna?.id
        if (urnaId == null) { /* ... (igual que antes) ... */ return }

        // --- Recolectar y Validar Datos (IDs CORREGIDOS) ---
        val name = binding.etName.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val stockStr = binding.etStock.text.toString().trim()
        val shortDesc = binding.etShortDescription.text.toString().trim()
        val detailedDesc = binding.etDetailedDescription.text.toString().trim()
        val isAvailable = binding.switchAvailable.isChecked // ID Corregido

        binding.tilName.error = if (name.isEmpty()) "El nombre es obligatorio" else null
        val price = priceStr.toDoubleOrNull()
        binding.tilPrice.error = if (price == null) "Precio inválido" else null
        val stock = stockStr.toIntOrNull()
        binding.tilStock.error = if (stock == null) "Stock inválido" else null

        if (binding.tilName.error != null || binding.tilPrice.error != null || binding.tilStock.error != null) {
            Toast.makeText(this, "Por favor, corrige los errores.", Toast.LENGTH_SHORT).show()
            return
        }
        val validPrice = price!!
        val validStock = stock!!

        // Obtener IDs de Spinners (IDs CORREGIDOS)
        val selectedColorId = getIdFromSpinnerSelection(binding.spinnerColor, "color") // ID Corregido
        val selectedMaterialId = getIdFromSpinnerSelection(binding.spinnerMaterial, "material") // ID Corregido
        val selectedModelId = getIdFromSpinnerSelection(binding.spinnerModel, "model") // ID Corregido

        Log.d("EditUrnaActivity", "Datos validados. IDs: Color=$selectedColorId, Material=$selectedMaterialId, Modelo=$selectedModelId")

        showLoading(true)

        // --- Lógica de Subida y Actualización (igual que antes) ---
        if (selectedImageUri != null) {
            // Caso 1: Imagen Cambiada
            // ... (subir imagen y llamar a updateUrnaApiCall con newImageUrl) ...
            // (Sin cambios aquí, usa uploadService.uploadImage)
            Log.d("EditUrnaActivity", "Nueva imagen detectada. Subiendo...")
            try {
                val imagePart = createImagePart(this, selectedImageUri!!, "image")
                uploadService.uploadImage(imagePart).enqueue(object : Callback<ImageUrl> {
                    override fun onResponse(call: Call<ImageUrl>, response: Response<ImageUrl>) {
                        if (response.isSuccessful && response.body() != null) {
                            val newImageUrl = response.body()!!
                            Log.d("EditUrnaActivity", "Nueva imagen subida OK: ${newImageUrl.path}")
                            updateUrnaApiCall(urnaId, name, validPrice, validStock, shortDesc, detailedDesc, isAvailable, selectedColorId, selectedMaterialId, selectedModelId, newImageUrl)
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "N/A"
                            Log.e("EditUrnaActivity", "Error API subiendo imagen: ${response.code()}. Body: $errorBody")
                            Toast.makeText(this@EditUrnaActivity, "Error ${response.code()} (Subida Imagen)", Toast.LENGTH_SHORT).show()
                            showLoading(false)
                        }
                    }
                    override fun onFailure(call: Call<ImageUrl>, t: Throwable) {
                        Log.e("EditUrnaActivity", "Fallo red (Subida Imagen)", t)
                        Toast.makeText(this@EditUrnaActivity, "Fallo red (Subida Imagen): ${t.message}", Toast.LENGTH_SHORT).show()
                        showLoading(false)
                    }
                })
            } catch (e: Exception) {
                Log.e("EditUrnaActivity", "Excepción creando imagePart", e)
                Toast.makeText(this@EditUrnaActivity, "Error procesando imagen", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
        } else {
            // Caso 2: Imagen NO Cambiada
            Log.d("EditUrnaActivity", "Imagen no cambió. Actualizando sólo datos...")
            updateUrnaApiCall(urnaId, name, validPrice, validStock, shortDesc, detailedDesc, isAvailable, selectedColorId, selectedMaterialId, selectedModelId, null)
        }
    }

    // --- getIdFromSpinnerSelection (igual que antes) ---
    private fun getIdFromSpinnerSelection(spinner: Spinner, type: String): Int? {
        val position = spinner.selectedItemPosition
        if (position < 0 || position == Spinner.INVALID_POSITION) {
            Log.w("EditUrnaActivity", "Posición inválida ($position) para spinner $type.")
            return null
        }
        return try { /* ... (código igual que antes, usando colorsList, etc.) ... */
            when (type) {
                "color" -> if (colorsList.isNotEmpty() && position < colorsList.size) colorsList[position].id else { Log.w("EditUrnaActivity","Pos $position >= tamaño lista color ${colorsList.size}"); null }
                "material" -> if (materialsList.isNotEmpty() && position < materialsList.size) materialsList[position].id else { Log.w("EditUrnaActivity","Pos $position >= tamaño lista material ${materialsList.size}"); null }
                "model" -> if (modelsList.isNotEmpty() && position < modelsList.size) modelsList[position].id else { Log.w("EditUrnaActivity","Pos $position >= tamaño lista modelo ${modelsList.size}"); null }
                else -> { Log.w("EditUrnaActivity", "Tipo '$type' no reconocido"); null }
            }
        } catch (e: IndexOutOfBoundsException) { /* ... (código igual que antes) ... */
            val listSize = when(type) { "color" -> colorsList.size; "material" -> materialsList.size; "model" -> modelsList.size; else -> 0 }
            Log.e("EditUrnaActivity", "IndexOutOfBounds en getIdFromSpinnerSelection ($type): Pos $position, Tamaño $listSize", e)
            null
        }
    }


    // --- updateUrnaApiCall (igual que antes) ---
    private fun updateUrnaApiCall(
        urnaId: Int, name: String, price: Double, stock: Int,
        shortDesc: String, detailedDesc: String, available: Boolean,
        colorId: Int?, materialId: Int?, modelId: Int?,
        newImageUrl: ImageUrl?
    ) {
        // ... (lógica igual que antes, construir dataMap y llamar a urnaService.updateUrna) ...
        val dataMap = mutableMapOf<String, @JvmSuppressWildcards Any?>() // Permite nulos explícitos
        if (name != currentUrna?.name) dataMap["name"] = name
        if (abs(price - (currentUrna?.price ?: -1.0)) > 0.001) dataMap["price"] = price
        if (stock != currentUrna?.stock) dataMap["stock"] = stock
        if (available != currentUrna?.available) dataMap["available"] = available
        if (shortDesc != (currentUrna?.short_description ?: "")) dataMap["short_description"] = shortDesc
        if (detailedDesc != (currentUrna?.detailed_description ?: "")) dataMap["detailed_description"] = detailedDesc
        if (colorId != currentUrna?.color_id) dataMap["color_id"] = colorId
        if (materialId != currentUrna?.material_id) dataMap["material_id"] = materialId
        if (modelId != currentUrna?.model_id) dataMap["model_id"] = modelId
        if (newImageUrl != null) dataMap["image_url"] = newImageUrl

        if (dataMap.isEmpty()) {
            Toast.makeText(this, "No se detectaron cambios.", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        Log.i("EditUrnaActivity", "Llamando a PATCH /urn/$urnaId con data: $dataMap")

        // Decide si filtrar nulos o no basado en tu API
        // Opción A: Filtrar nulos (si la API ignora campos omitidos)
        val finalDataMap = dataMap.filterValues { it != null } as Map<String, Any>
        // Opción B: No filtrar nulos (si la API necesita {"color_id": null} explícito)
        // val finalDataMap = dataMap as Map<String, Any?> // O ajusta el tipo en UrnaService

        urnaService.updateUrna(urnaId, finalDataMap).enqueue(object : Callback<Urna> {
            override fun onResponse(call: Call<Urna>, response: Response<Urna>) { /* ... (igual que antes) ... */
                showLoading(false)
                if (response.isSuccessful) {
                    Log.i("EditUrnaActivity", "PATCH exitoso.")
                    Toast.makeText(this@EditUrnaActivity, "Urna actualizada", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "N/A"
                    Log.e("EditUrnaActivity", "Error API actualizando: ${response.code()}. Body: $errorBody")
                    Toast.makeText(this@EditUrnaActivity, "Error ${response.code()} (Actualizar)", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Urna>, t: Throwable) { /* ... (igual que antes) ... */
                showLoading(false)
                Log.e("EditUrnaActivity", "Fallo red (Actualizar)", t)
                Toast.makeText(this@EditUrnaActivity, "Fallo red (Actualizar): ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- Diálogo y Lógica de Borrado (igual que antes) ---
    private fun showDeleteConfirmationDialog() { /* ... (igual que antes) ... */
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Eliminar esta urna permanentemente?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Eliminar") { _, _ -> deleteUrna() }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun deleteUrna() { /* ... (igual que antes, usa urnaService.deleteUrna) ... */
        val urnaId = currentUrna?.id ?: return
        Log.d("EditUrnaActivity", "Intentando eliminar urna ID: $urnaId")
        showLoading(true)
        urnaService.deleteUrna(urnaId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) { /* ... (igual que antes) ... */
                showLoading(false)
                if (response.isSuccessful) {
                    Log.i("EditUrnaActivity", "Urna ID $urnaId eliminada.")
                    Toast.makeText(this@EditUrnaActivity, "Urna eliminada", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "N/A"
                    Log.e("EditUrnaActivity", "Error API eliminando: ${response.code()}. Body: $errorBody")
                    Toast.makeText(this@EditUrnaActivity, "Error ${response.code()} (Eliminar)", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) { /* ... (igual que antes) ... */
                showLoading(false)
                Log.e("EditUrnaActivity", "Fallo red (Eliminar)", t)
                Toast.makeText(this@EditUrnaActivity, "Fallo red (Eliminar): ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- Funciones Helper (igual que antes) ---
    private fun showLoading(isLoading: Boolean) { /* ... (igual que antes) ... */
        // Asegúrate de que binding no sea nulo
        if (binding != null) {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveChanges.isEnabled = !isLoading
            binding.btnDeleteUrna.isEnabled = !isLoading
            binding.btnSelectImage.isEnabled = !isLoading
        } else {
            Log.w("EditUrnaActivity", "showLoading llamado pero _binding es nulo.")
        }
    }
    @Throws(IllegalStateException::class)
    private fun createImagePart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part { /* ... (igual que antes) ... */
        val mimeType = getMimeType(context, uri) ?: "image/jpeg"
        val fileName = getFileName(context, uri)
        Log.d("EditUrnaActivity","Creando imagePart: field='$fieldName', filename='$fileName', mime='$mimeType'")
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir InputStream para Uri: $uri")
        val bytes = inputStream.use { it.readBytes() }
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }
    private fun getMimeType(context: Context, uri: Uri): String? { /* ... (igual que antes) ... */
        return context.contentResolver.getType(uri)?.lowercase()
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.lowercase()
            )
    }
    private fun getFileName(context: Context, uri: Uri): String { /* ... (igual que antes) ... */
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) { result = cursor.getString(index) }
                    }
                }
            } catch (e: Exception) { Log.w("EditUrnaActivity", "Error obteniendo nombre de archivo (ContentResolver)", e) }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) { result = result?.substring(cut + 1) }
        }
        val cleanedResult = result?.replace("[^a-zA-Z0-9._-]".toRegex(), "_") ?: "unknown_file_${System.currentTimeMillis()}"
        Log.d("EditUrnaActivity", "Nombre archivo final: $cleanedResult (Original: $result)")
        return cleanedResult
    }

    // --- onDestroy (No necesario aquí, ya que es una Activity) ---
    // No necesitamos onDestroyView como en los Fragmentos
}