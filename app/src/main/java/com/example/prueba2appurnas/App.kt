package com.example.prueba2appurnas

import android.app.Application
import android.util.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 🟢 Aquí puedes inicializar librerías globales si lo necesitas (Glide, Retrofit, etc.)
        Log.d("AppInit", "✅ Aplicación inicializada correctamente")
    }
}
