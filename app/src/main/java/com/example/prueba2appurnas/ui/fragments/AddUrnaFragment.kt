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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.UrnaService
import com.example.prueba2appurnas.databinding.FragmentAddUrnaBinding // Binding actualizado
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

class AddUrnaFragment : Fragment() { // Nombre actualizado

    // View Binding seguro
    private var _binding: FragmentAddUrnaBinding? = null // Binding actualizado
    private val binding get() = _binding!!

    private lateinit var urnaService: UrnaService
    private var selectedImageUri: Uri? = null

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
        _binding = FragmentAddUrnaBinding.inflate(inflater, container, false) // Binding actualizado
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        urnaService = RetrofitClient.getUrnaService(requireContext()) // Usar requireContext()

        binding.btnSelectMainImage.setOnClickListener {
            selectImage()
        }

        binding.btnSaveUrna.setOnClickListener {
            saveUrna()
        }
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
        if (context == null || _binding == null) return // Chequeo de seguridad

        // Validación de campos
        val name = binding.etName.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val stockStr = binding.etStock.text.toString().trim()
        val shortDesc = binding.etShortDescription.text.toString().trim()
        val detailedDesc = binding.etDetailedDescription.text.toString().trim()
        val isAvailable = binding.switchAvailable.isChecked

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre, Precio y Stock son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Por favor, selecciona una imagen principal", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true) // Mostrar ProgressBar

        // Crear partes Multipart
        try {
            val namePart = createTextPart(name)
            val pricePart = createTextPart(priceStr)
            val stockPart = createTextPart(stockStr)
            val availablePart = createTextPart(isAvailable.toString())
            val shortDescPart = if (shortDesc.isNotEmpty()) createTextPart(shortDesc) else null
            val detailedDescPart = if (detailedDesc.isNotEmpty()) createTextPart(detailedDesc) else null
            val imageFilePart = createImagePart(requireContext(), selectedImageUri!!, "image_url") // Usa el nombre de campo correcto

            // Llamada API
            urnaService.createUrnaMultipart(
                name = namePart,
                price = pricePart,
                stock = stockPart,
                available = availablePart,
                shortDescription = shortDescPart,
                detailedDescription = detailedDescPart,
                imageFile = imageFilePart
            ).enqueue(object : Callback<Urna> {
                override fun onResponse(call: Call<Urna>, response: Response<Urna>) {
                    if (_binding == null) return // Chequeo si el fragmento aún existe
                    showLoading(false)

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "¡Urna creada con éxito!", Toast.LENGTH_LONG).show()
                        clearForm() // Limpiar formulario
                        // Navegar de vuelta a la lista (UrnasFragment)
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
                    if (_binding == null) return // Chequeo
                    showLoading(false)
                    Log.e("AddUrnaFragment", "Fallo al crear urna (Multipart)", t)
                    Toast.makeText(requireContext(), "Fallo de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            // Captura errores al crear las partes (ej: si no se puede leer la imagen)
            Log.e("AddUrnaFragment", "Error al preparar datos para la API", e)
            Toast.makeText(requireContext(), "Error al procesar la imagen seleccionada", Toast.LENGTH_LONG).show()
            showLoading(false)
        }
    }


    // --- Funciones Helper ---

    /** Crea RequestBody de texto plano. */
    private fun createTextPart(text: String): RequestBody {
        return text.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /** Crea MultipartBody.Part para una imagen Uri. */
    private fun createImagePart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part {
        val mimeType = getMimeType(context, uri) ?: "image/jpeg" // Mime type por defecto
        val fileName = getFileName(context, uri)

        // Obtener bytes de la imagen de forma segura
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir InputStream para la Uri: $uri")
        val bytes = inputStream.use { it.readBytes() } // 'use' cierra el stream automáticamente

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }

    /** Obtiene el MimeType de una Uri. */
    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri) ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        )
    }

    /** Obtiene el nombre de archivo de una Uri de forma segura. */
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor -> // 'use' cierra el cursor
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) {
                            result = cursor.getString(index)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("AddUrnaFragment", "Error al obtener nombre de archivo desde ContentResolver", e)
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        // Limpiar nombre de archivo y devolver uno por defecto si falla
        return result?.replace("[^a-zA-Z0-9._-]".toRegex(), "_") ?: "unknown_image_file"
    }

    /** Muestra/oculta el ProgressBar y habilita/deshabilita el botón guardar. */
    private fun showLoading(isLoading: Boolean) {
        if (_binding != null) { // Comprobar binding antes de usarlo
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveUrna.isEnabled = !isLoading
        }
    }

    /** Limpia los campos del formulario. */
    private fun clearForm() {
        if (_binding != null) { // Comprobar binding
            binding.etName.text = null
            binding.etPrice.text = null
            binding.etStock.text = null
            binding.etShortDescription.text = null
            binding.etDetailedDescription.text = null
            binding.switchAvailable.isChecked = true
            // Resetea la imagen usando el placeholder o un drawable por defecto
            binding.imgUrnaPreview.setImageResource(R.drawable.bg_image_border)
            selectedImageUri = null
        }
    }

    /** Limpia el binding. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}