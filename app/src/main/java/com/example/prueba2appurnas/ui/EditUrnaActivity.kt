package com.example.prueba2appurnas.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.ApiConfig
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.UrnaService
import com.example.prueba2appurnas.model.Urna
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditUrnaActivity : AppCompatActivity() {

    private lateinit var urna: Urna
    private lateinit var etUrnaName: EditText
    private lateinit var etShortDescription: EditText
    private lateinit var etDetailedDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etStock: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_urna)

        intent.getSerializableExtra("urn")?.let {
            urna = it as Urna
        } ?: run {
            Toast.makeText(this, "Error al cargar la urna", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etUrnaName = findViewById(R.id.etUrnaName)
        etShortDescription = findViewById(R.id.etShortDescription)
        etDetailedDescription = findViewById(R.id.etDetailedDescription)
        etPrice = findViewById(R.id.etPrice)
        etStock = findViewById(R.id.etStock)
        btnSave = findViewById(R.id.btnSave)

        populateFields()

        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun populateFields() {
        etUrnaName.setText(urna.name)
        etShortDescription.setText(urna.short_description)
        etDetailedDescription.setText(urna.detailed_description)
        etPrice.setText(urna.price.toString())
        etStock.setText(urna.stock.toString())
    }

    private fun saveChanges() {
        val updatedName = etUrnaName.text.toString()
        val updatedShortDesc = etShortDescription.text.toString()
        val updatedDetailedDesc = etDetailedDescription.text.toString()
        val updatedPrice = etPrice.text.toString().toDoubleOrNull() ?: urna.price
        val updatedStock = etStock.text.toString().toIntOrNull() ?: urna.stock

        val updatedUrna = urna.copy(
            name = updatedName,
            short_description = updatedShortDesc,
            detailed_description = updatedDetailedDesc,
            price = updatedPrice,
            stock = updatedStock,
            image_url = urna.image_url // Preservar explícitamente el image_url original
        )

        val retrofit = RetrofitClient.createClient(ApiConfig.BASE_URL_V1, this)
        val service = retrofit.create(UrnaService::class.java)

        service.updateUrna(urna.id, updatedUrna).enqueue(object : Callback<Urna> {
            override fun onResponse(call: Call<Urna>, response: Response<Urna>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditUrnaActivity, "Urna actualizada con éxito", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Notificar a la actividad anterior que hubo cambios
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@EditUrnaActivity, "Error al actualizar: ${response.code()} - ${errorBody}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Urna>, t: Throwable) {
                Toast.makeText(this@EditUrnaActivity, "Fallo en la conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
