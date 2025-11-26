package com.example.prueba2appurnas.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    val id: Int,

    // Logcat muestra "name", no "user". Usamos alternate por seguridad.
    @SerializedName("name", alternate = ["user"])
    val name: String?, // Puede venir nulo según logcat

    val email: String?,

    // Logcat muestra "role" (inglés), tu código usaba "rol".
    @SerializedName("role", alternate = ["rol"])
    val rol: String = "USER", // Valor por defecto para evitar crashes

    // Logcat e Imagen Swagger confirman "Activo" con mayúscula
    @SerializedName("Activo")
    val activo: Boolean? = false, // Por defecto false si es null

    val created_at: Long? = null
) : Serializable