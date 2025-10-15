package com.example.prueba2appurnas.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.UrnaService
import com.example.prueba2appurnas.model.ImageUrl
import com.example.prueba2appurnas.model.Urna
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import com.example.prueba2appurnas.util.NetUtils

class EditUrnaActivity : AppCompatActivity() {

    private lateinit var urna: Urna
    private lateinit var urnaService: UrnaService

    // Vistas
    private lateinit var etName: EditText
    private lateinit var etShortDesc: EditText
    private lateinit var etDetailedDesc: EditText
    private lateinit var etPrice: EditText
    private lateinit var etStock: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var ivUrnaImage: ImageView
    private lateinit var btnChangeImage: Button

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                selectedImageUri = it
                ivUrnaImage.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_urna)

        etName = findViewById(R.id.etName)
        etShortDesc = findViewById(R.id.etShortDesc)
        etDetailedDesc = findViewById(R.id.etDetailedDesc)
        etPrice = findViewById(R.id.etPrice)
        etStock = findViewById(R.id.etStock)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        ivUrnaImage = findViewById(R.id.ivUrnaImage)
        btnChangeImage = findViewById(R.id.btnChangeImage)

        val intentUrna = intent.getSerializableExtra("urn") as? Urna
        if (intentUrna == null) {
            Toast.makeText(this, "Error: No se pudieron cargar los datos de la urna.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        urna = intentUrna

        val retrofit = RetrofitClient.createClient(ApiConfig.BASE_URL_V1, this)
        urnaService = retrofit.create(UrnaService::class.java)

        populateForm()

        btnChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnSaveChanges.setOnClickListener { saveChanges() }
    }

    private fun populateForm() {
        etName.setText(urna.name)
        etShortDesc.setText(urna.short_description)
        etDetailedDesc.setText(urna.detailed_description)
        etPrice.setText(urna.price?.toString())
        etStock.setText(urna.stock?.toString())

        val full = NetUtils.buildAbsoluteUrl(urna.image_url)
        val model = full?.let { NetUtils.glideModelWithAuth(this, it) }

        Glide.with(this)
            .load(model)
            .placeholder(R.drawable.bg_image_border)
            .error(R.drawable.bg_image_border)
            .into(ivUrnaImage)
    }

    private fun saveChanges() {
        if (selectedImageUri != null) {
            uploadImageAndUpdateUrna()
        } else {
            updateUrnaOnly()
        }
    }

    private fun uploadImageAndUpdateUrna() {
        selectedImageUri?.let { uri ->
            val filePath = getPathFromUri(uri)
            if (filePath == null) {
                Toast.makeText(this, "No se pudo obtener la ruta del archivo de imagen", Toast.LENGTH_SHORT).show()
                return
            }
            val file = File(filePath)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            urnaService.uploadImage(body).enqueue(object : Callback<ImageUrl> {
                override fun onResponse(call: Call<ImageUrl>, response: Response<ImageUrl>) {
                    if (response.isSuccessful) {
                        val newImageUrl = response.body()
                        if (newImageUrl != null) {
                            updateUrnaWithNewImage(newImageUrl)
                        } else {
                            Toast.makeText(this@EditUrnaActivity, "La API de imagen no devolvió una URL válida", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@EditUrnaActivity, "Error al subir la imagen: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ImageUrl>, t: Throwable) {
                    Toast.makeText(this@EditUrnaActivity, "Fallo en la conexión al subir imagen: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateUrnaWithNewImage(newImage: ImageUrl) {
        val updatedUrna = createUpdatedUrna(newImage)
        updateUrnaApiCall(updatedUrna)
    }

    private fun updateUrnaOnly() {
        val updatedUrna = createUpdatedUrna(null)
        updateUrnaApiCall(updatedUrna)
    }

    private fun createUpdatedUrna(newImage: ImageUrl?): Urna {
        val finalImageUrl = newImage ?: urna.image_url
        val correctedImageUrl = finalImageUrl?.copy(
            meta = finalImageUrl.meta ?: emptyMap()
        )

        return urna.copy(
            name = etName.text.toString(),
            short_description = etShortDesc.text.toString(),
            detailed_description = etDetailedDesc.text.toString(),
            price = etPrice.text.toString().toDoubleOrNull() ?: urna.price,
            stock = etStock.text.toString().toIntOrNull() ?: urna.stock,
            image_url = correctedImageUrl
        )
    }

    private fun updateUrnaApiCall(updatedUrna: Urna) {
        urna.id.let {
            urnaService.updateUrna(it, updatedUrna).enqueue(object : Callback<Urna> {
                override fun onResponse(call: Call<Urna>, response: Response<Urna>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditUrnaActivity, "Urna actualizada correctamente", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@EditUrnaActivity, "Error al actualizar la urna: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                        Log.e("EditUrna", "Error: ${response.code()} - $errorBody")
                    }
                }

                override fun onFailure(call: Call<Urna>, t: Throwable) {
                    Toast.makeText(this@EditUrnaActivity, "Fallo en la conexión al actualizar: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }

}