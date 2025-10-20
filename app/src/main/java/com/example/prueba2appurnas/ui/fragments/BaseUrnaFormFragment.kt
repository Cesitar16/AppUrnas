package com.example.prueba2appurnas.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.prueba2appurnas.api.ColorService
import com.example.prueba2appurnas.api.MaterialService
import com.example.prueba2appurnas.api.ModelService
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.model.Color
import com.example.prueba2appurnas.model.Material
import com.example.prueba2appurnas.model.Model
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragmento base abstracto que maneja la lógica de negocio compartida
 * para los formularios de Añadir y Editar Urna.
 *
 * Contiene:
 * - Instancias de servicios (Color, Material, Model).
 * - Listas de datos para los spinners.
 * - Lógica de carga SECUENCIAL de los spinners.
 * - Manejadores de errores de API.
 */
abstract class BaseUrnaFormFragment : Fragment() {

    // --- Servicios de API (protegidos para que las clases hijas los vean si es necesario) ---
    protected lateinit var colorService: ColorService
    protected lateinit var materialService: MaterialService
    protected lateinit var modelService: ModelService

    // --- Listas de datos (protegidas) ---
    protected var colorsList: List<Color> = emptyList()
    protected var materialsList: List<Material> = emptyList()
    protected var modelsList: List<Model> = emptyList()

    // --- Métodos abstractos que las clases hijas DEBEN implementar ---

    /**
     * Muestra u oculta el indicador de carga principal del formulario.
     */
    abstract fun showMainLoading(isLoading: Boolean)

    /**
     * Muestra u oculta el indicador de carga de los spinners (puede ser el mismo que showMainLoading).
     */
    abstract fun showSpinnerLoading(isLoading: Boolean)

    /**
     * Devuelve el Spinner de Color de la vista hija.
     */
    abstract fun getColorSpinner(): Spinner?

    /**
     * Devuelve el Spinner de Material de la vista hija.
     */
    abstract fun getMaterialSpinner(): Spinner?

    /**
     * Devuelve el Spinner de Modelo de la vista hija.
     */
    abstract fun getModelSpinner(): Spinner?

    /**
     * Devuelve el ID del color que debe preseleccionarse (null si es un formulario nuevo).
     */
    abstract fun getSelectedColorId(): Int?

    /**
     * Devuelve el ID del material que debe preseleccionarse (null si es un formulario nuevo).
     */
    abstract fun getSelectedMaterialId(): Int?

    /**
     * Devuelve el ID del modelo que debe preseleccionarse (null si es un formulario nuevo).
     */
    abstract fun getSelectedModelId(): Int?


    // --- Lógica Compartida (Movida desde los fragmentos) ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar servicios una sola vez
        try {
            if (context == null) throw IllegalStateException("Contexto nulo al inicializar servicios base")
            colorService = RetrofitClient.getColorService(requireContext())
            materialService = RetrofitClient.getMaterialService(requireContext())
            modelService = RetrofitClient.getModelService(requireContext())
        } catch (e: Exception) {
            Log.e("BaseUrnaFormFragment", "Error inicializando servicios: ${e.message}", e)
            if (context != null) {
                Toast.makeText(requireContext(), "Error configuración red", Toast.LENGTH_LONG).show()
            }
            // La clase hija decidirá si cerrar (popBackStack)
        }
    }

    /**
     * Inicia la cadena de carga secuencial.
     * Debe llamarse desde onViewCreated() en la clase hija.
     */
    protected fun loadSpinnersSequentially() {
        if (!::colorService.isInitialized || !::materialService.isInitialized || !::modelService.isInitialized) {
            Log.e("BaseUrnaFormFragment", "Los servicios no se inicializaron. Abortando carga de spinners.")
            return
        }

        showSpinnerLoading(true)
        Log.d("LoadSpinners", "Iniciando carga secuencial...")

        // 1. Cargar Colores
        colorService.getAllColors().enqueue(object : Callback<List<Color>> {
            override fun onResponse(call: Call<List<Color>>, response: Response<List<Color>>) {
                if (!isAdded || context == null) return
                Log.d("LoadSpinners", "Respuesta Colores: ${response.code()}")
                if (response.isSuccessful) {
                    colorsList = response.body() ?: emptyList()
                    setupSpinnerAdapter(getColorSpinner(), colorsList, "colores", getSelectedColorId())
                } else {
                    handleApiError("colores", response.code(), response.message())
                    getColorSpinner()?.adapter = null
                }
                Log.d("LoadSpinners", "Colores OK. Cargando Materiales...")
                loadMaterials() // 2. Cargar Materiales
            }

            override fun onFailure(call: Call<List<Color>>, t: Throwable) {
                if (!isAdded || context == null) return
                handleApiFailure("colores", t)
                getColorSpinner()?.adapter = null
                Log.w("LoadSpinners", "Fallo de red Colores. Cargando Materiales...")
                loadMaterials() // Continuar aunque falle
            }
        })
    }

    private fun loadMaterials() {
        materialService.getAllMaterials().enqueue(object : Callback<List<Material>> {
            override fun onResponse(call: Call<List<Material>>, response: Response<List<Material>>) {
                if (!isAdded || context == null) return
                Log.d("LoadSpinners", "Respuesta Materiales: ${response.code()}")
                if (response.isSuccessful) {
                    materialsList = response.body() ?: emptyList()
                    setupSpinnerAdapter(getMaterialSpinner(), materialsList, "materiales", getSelectedMaterialId())
                } else {
                    handleApiError("materiales", response.code(), response.message())
                    getMaterialSpinner()?.adapter = null
                }
                Log.d("LoadSpinners", "Materiales OK. Cargando Modelos...")
                loadModels() // 3. Cargar Modelos
            }

            override fun onFailure(call: Call<List<Material>>, t: Throwable) {
                if (!isAdded || context == null) return
                handleApiFailure("materiales", t)
                getMaterialSpinner()?.adapter = null
                Log.w("LoadSpinners", "Fallo de red Materiales. Cargando Modelos...")
                loadModels() // Continuar
            }
        })
    }

    private fun loadModels() {
        modelService.getAllModels().enqueue(object : Callback<List<Model>> {
            override fun onResponse(call: Call<List<Model>>, response: Response<List<Model>>) {
                if (!isAdded || context == null) return
                Log.d("LoadSpinners", "Respuesta Modelos: ${response.code()}")
                if (response.isSuccessful) {
                    modelsList = response.body() ?: emptyList()
                    setupSpinnerAdapter(getModelSpinner(), modelsList, "modelos", getSelectedModelId())
                } else {
                    handleApiError("modelos", response.code(), response.message())
                    getModelSpinner()?.adapter = null
                }
                Log.d("LoadSpinners", "Modelos OK. Carga secuencial completa.")
                showSpinnerLoading(false) // 4. Fin de la carga
            }

            override fun onFailure(call: Call<List<Model>>, t: Throwable) {
                if (!isAdded || context == null) return
                handleApiFailure("modelos", t)
                getModelSpinner()?.adapter = null
                Log.w("LoadSpinners", "Fallo de red Modelos.")
                showSpinnerLoading(false) // Fin de la carga
            }
        })
    }

    /**
     * Ayudante genérico para configurar el adapter y preseleccionar el ítem.
     * Esta es la lógica combinada de ambos fragmentos.
     */
    private fun <T> setupSpinnerAdapter(
        spinner: Spinner?,
        dataList: List<T>,
        dataTypeName: String,
        currentSelectedId: Int?
    ) where T : Any {
        if (spinner == null) {
            Log.e("BaseUrnaFormFragment", "Spinner para $dataTypeName es nulo.")
            return
        }
        if (!isAdded || context == null) return

        val names = dataList.mapNotNull { item ->
            when (item) {
                is Color -> item.name.takeIf { !it.isNullOrBlank() }
                is Material -> item.name.takeIf { !it.isNullOrBlank() }
                is Model -> item.name.takeIf { !it.isNullOrBlank() }
                else -> item.toString()
            }
        }

        if (names.isNotEmpty()) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Lógica de preselección
            currentSelectedId?.let { currentId ->
                val position = dataList.indexOfFirst { item ->
                    when (item) {
                        is Color -> item.id == currentId
                        is Material -> item.id == currentId
                        is Model -> item.id == currentId
                        else -> false
                    }
                }
                if (position >= 0) {
                    spinner.setSelection(position, false)
                } else {
                    Log.w("SetupSpinner", "ID $currentId no encontrado en $dataTypeName. Seleccionando 0.")
                    spinner.setSelection(0, false)
                }
            } ?: if (spinner.adapter.count > 0) {
                // No hay ID actual (modo "Añadir"), seleccionar 0
                spinner.setSelection(0, false)
            } else {
                Log.w("SetupSpinner", "Spinner vacío para $dataTypeName. No se puede preseleccionar.")
            }
        } else {
            Log.w("SetupSpinner", "Lista de nombres vacía para $dataTypeName.")
            spinner.adapter = null
        }
    }

    // --- Manejadores de Error (Movidos) ---
    protected fun handleApiError(dataType: String, code: Int, message: String?) {
        Log.e("BaseUrnaFormFragment", "Error API cargando $dataType: $code - $message")
        if (context != null) Toast.makeText(context, "Error cargando $dataType ($code)", Toast.LENGTH_SHORT).show()
    }

    protected fun handleApiFailure(dataType: String, t: Throwable) {
        Log.e("BaseUrnaFormFragment", "Fallo red cargando $dataType", t)
        if (context != null) Toast.makeText(context, "Fallo red cargando $dataType", Toast.LENGTH_SHORT).show()
    }

    /**
     * Ayudante para obtener el ID del spinner (las clases hijas lo necesitarán).
     */
    protected fun <T> getIdFromSpinnerSelection(spinner: Spinner, dataList: List<T>): Int? where T : Any {
        val position = spinner.selectedItemPosition
        if (position < 0 || position >= dataList.size || spinner.selectedItem == null) return null
        return try {
            when (val item = dataList[position]) {
                is Color -> item.id
                is Material -> item.id
                is Model -> item.id
                else -> null
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e("BaseUrnaFormFragment", "Error al obtener ID del spinner", e)
            null
        }
    }
}