package com.miapp.xanostorekotlin.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.FragmentAddProductBinding
import com.miapp.xanostorekotlin.model.CreateProductRequest
import com.miapp.xanostorekotlin.model.ProductImage
import com.miapp.xanostorekotlin.ui.adapter.ImagePreviewAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class AddProductFragment : Fragment() {
    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private val imagePreviewAdapter = ImagePreviewAdapter()
    private val selectedUris = mutableListOf<Uri>()
    private val uploadService by lazy { RetrofitClient.createUploadService(requireContext()) }
    private val productService by lazy { RetrofitClient.createProductService(requireContext()) }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (!uris.isNullOrEmpty()) {
                selectedUris.clear()
                selectedUris.addAll(uris)
                imagePreviewAdapter.submitList(selectedUris.toList())
                binding.imagesError.visibility = View.GONE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imagePreviewAdapter
        }

        binding.selectImagesButton.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.submitButton.setOnClickListener { submitProduct() }
    }

    private fun submitProduct() {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        val description = binding.descriptionInput.text?.toString()?.trim().orEmpty()
        val price = binding.priceInput.text?.toString()?.toDoubleOrNull()

        if (name.isBlank() || description.isBlank() || price == null) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedUris.isEmpty()) {
            binding.imagesError.visibility = View.VISIBLE
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val uploadedImages = uploadImages(selectedUris)
                val request = CreateProductRequest(
                    name = name,
                    description = description,
                    price = price,
                    images = uploadedImages
                )
                val response = productService.createProduct(request)
                Toast.makeText(
                    requireContext(),
                    response.message ?: "Producto creado correctamente",
                    Toast.LENGTH_LONG
                ).show()
                clearForm()
            } catch (ex: Exception) {
                handleError(ex)
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun uploadImages(uris: List<Uri>): List<ProductImage> {
        val context = requireContext().applicationContext
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<ProductImage>()
            for ((index, uri) in uris.withIndex()) {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "image/*"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("No se pudo leer la imagen")
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val fileName = "image_${System.currentTimeMillis()}_${index}.jpg"
            val part = MultipartBody.Part.createFormData("image", fileName, requestBody)
            val uploaded = uploadService.uploadImage(part)
            result.add(uploaded)
        }
            result
        }
    }

    private fun handleError(ex: Exception) {
        val message = when (ex) {
            is HttpException -> "Error ${ex.code()} al crear el producto"
            is IOException -> "Error de lectura de imagen"
            else -> ex.localizedMessage ?: "Error desconocido"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun clearForm() {
        binding.nameInput.text?.clear()
        binding.descriptionInput.text?.clear()
        binding.priceInput.text?.clear()
        selectedUris.clear()
        imagePreviewAdapter.submitList(emptyList())
        binding.imagesError.visibility = View.GONE
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.submitButton.isEnabled = !loading
        binding.selectImagesButton.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "AddProductFragment"
        fun newInstance(): Pair<String, AddProductFragment> = TAG to AddProductFragment()
    }
}
