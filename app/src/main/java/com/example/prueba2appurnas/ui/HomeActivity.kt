package com.example.prueba2appurnas.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba2appurnas.R
import android.view.LayoutInflater

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val contenedor = findViewById<LinearLayout>(R.id.listaUrnas)

        // Datos simulados de ejemplo
        val urnas = listOf(
            Urna("Urna Clásica", "$250", 15, R.drawable.logo_escala_ajuste),
            Urna("Urna DeLujo", "$400", 7, R.drawable.logo_escala_ajuste),
            Urna("Urna Moderna", "$180", 10, R.drawable.logo_escala_ajuste)
        )

        urnas.forEach {
            val item = LayoutInflater.from(this).inflate(R.layout.item_urna_card, contenedor, false)

            item.findViewById<TextView>(R.id.txtNombreUrna).text = it.nombre
            item.findViewById<TextView>(R.id.txtPrecioUrna).text = it.precio
            item.findViewById<TextView>(R.id.txtStockUrna).text = "Stock: ${it.stock}"
            item.findViewById<ImageView>(R.id.imgUrna).setImageResource(it.imagen)

            contenedor.addView(item)
        }
    }
}

data class Urna(
    val nombre: String,
    val precio: String,
    val stock: Int,
    val imagen: Int
)
