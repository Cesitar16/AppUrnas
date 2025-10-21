# AppUrnas - Gestión de Inventario (Android)

Aplicación Android nativa (Kotlin) desarrollada para **Descansos del Recuerdo SPA**. Funciona como una herramienta interna para la gestión y administración del inventario de urnas funerarias.

La aplicación permite a los usuarios autenticados realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre el catálogo de urnas, incluyendo la gestión detallada de productos, subida y visualización de imágenes, y consulta de métricas clave del inventario.

## 🚀 Características Principales

* **Autenticación de Usuarios:** Sistema completo de **Inicio de Sesión** (`MainActivity.kt`) y **Registro** (`RegisterActivity.kt`). La gestión de sesión se realiza mediante **Bearer Tokens JWT**, administrados por `TokenManager.kt`.
* **Perfil de Usuario:** Pantalla (`ProfileFragment.kt`) que consume el endpoint `/auth/me` para mostrar la información del usuario actual (nombre, email, rol) y ofrece la funcionalidad de **Cerrar Sesión**.
* **Dashboard de Métricas:** La pantalla principal (`UrnasFragment.kt`) presenta un dashboard visual con indicadores clave del inventario, como:
    * Total de urnas registradas.
    * Stock total disponible.
    * Precio promedio de las urnas.
    * Número de urnas con bajo stock (≤ 5 unidades).
* **Gestión de Urnas (CRUD Completo):**
    * **Listar (Read):** Muestra todas las urnas disponibles en un `RecyclerView` (`UrnasFragment.kt`, `UrnaAdapter.kt`). Incluye un **buscador dinámico** que filtra la lista en tiempo real por nombre o ID interno de la urna.
    * **Crear (Create):** Formulario dedicado (`AddUrnaFragment.kt`) para añadir nuevas urnas. Permite ingresar todos los detalles (nombre, precio, stock, descripciones, dimensiones, peso, etc.), seleccionar **Color**, **Material** y **Modelo** desde listas cargadas dinámicamente desde la API, y subir una **imagen principal** obligatoria mediante una petición `multipart/form-data`.
    * **Ver Detalle (Read):** Al seleccionar una urna de la lista, se navega a una vista detallada (`UrnaDetailFragment.kt`) que muestra toda la información del producto, la imagen principal y una **galería horizontal** de imágenes secundarias asociadas.
    * **Editar (Update):** Formulario (`EditUrnaFragment.kt`) pre-poblado con los datos de una urna existente. Permite modificar cualquier campo, **cambiar la imagen principal** (subiendo una nueva vía `/upload/image` y luego actualizando la urna), **añadir nuevas imágenes a la galería** (subiendo vía `/urn_image`), y seleccionar diferentes **Color**, **Material** o **Modelo**. La lógica común de los formularios de Crear y Editar se encapsula en `BaseUrnaFormFragment.kt`.
    * **Eliminar (Delete):** Desde la pantalla de edición (`EditUrnaFragment.kt`), se ofrece un botón para **eliminar permanentemente** la urna, mostrando un diálogo de confirmación antes de proceder.
* **Gestión de Imágenes:**
    * Subida de imagen principal al crear o editar.
    * Subida de imágenes adicionales a la galería desde la pantalla de edición.
    * Visualización de la imagen principal y galería secundaria en la vista de detalle, con carga eficiente mediante **Glide**.

## 🛠️ Stack Tecnológico y Librerías

* **Lenguaje:** 100% [Kotlin](https://kotlinlang.org/)
* **Arquitectura:** Single-Activity (`HomeActivity.kt`) basada en Fragmentos.
* **UI:**
    * [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
    * [Material Components](https://material.io/develop/android)
    * [AndroidX](https://developer.android.com/jetpack) (AppCompat, ConstraintLayout, RecyclerView, GridLayout, Core-KTX, Lifecycle-KTX)
* **Networking:**
    * [Retrofit](https://square.github.io/retrofit/): Cliente HTTP para consumir la API REST.
    * [OkHttp (Logging Interceptor)](https://square.github.io/okhttp/): Para interceptar y registrar llamadas de red con fines de depuración.
    * [Gson](https://github.com/google/gson): Convertidor para serialización/deserialización JSON con Retrofit.
* **Asincronía:**
    * [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) (utilizando `lifecycleScope` en Activities y Fragments).
* **Carga de Imágenes:**
    * [Glide](https://github.com/bumptech/glide): Librería eficiente para cargar, decodificar y cachear imágenes desde URLs. Se integra con `AuthInterceptor` para cargar imágenes protegidas.
* **Backend:**
    * Consume una API REST desarrollada en [**Xano**](https://www.xano.com/).

## 🔌 Conexión API (Xano Backend)

La configuración base de la API se gestiona en `api/ApiConfig.kt` y la creación de instancias de servicios Retrofit en `api/RetrofitClient.kt`.

* **Autenticación:** Se utiliza un `AuthInterceptor.kt` que inyecta automáticamente el `Bearer Token` (obtenido de `TokenManager.kt`) en las cabeceras `Authorization` de las peticiones a endpoints protegidos.
* **URL Base (Host):** `https://x8ki-letl-twmt.n7.xano.io`
* **URL Base (Auth):** `https://x8ki-letl-twmt.n7.xano.io/api:hfExqmJb/`
* **URL Base (API v1 - Urnas, Imágenes, etc.):** `https://x8ki-letl-twmt.n7.xano.io/api:faArLfKY/`
* **Configuración de Seguridad de Red:** Se utiliza `network_security_config.xml` para permitir tráfico (incluyendo `cleartext` si es necesario, aunque configurado para dominios específicos) hacia los dominios de Xano y Google Cloud Storage (donde Xano puede almacenar imágenes).

### Endpoints Utilizados

(Inferidos de las interfaces `*Service.kt`)

* **Autenticación (`AuthService.kt`, Base: Auth):**
    * `POST /auth/login`: Inicia sesión.
    * `POST /auth/signup`: Registra un nuevo usuario.
    * `GET /auth/me`: Obtiene datos del usuario autenticado.
* **Urnas (`UrnaService.kt`, Base: API v1):**
    * `GET /urn`: Obtiene la lista completa de urnas.
    * `GET /urn/{id}`: Obtiene detalles de una urna específica.
    * `POST /urn` (`@Multipart`): Crea una nueva urna (incluye imagen principal).
    * `PATCH /urn/{id}`: Actualiza datos de una urna existente (envía solo campos modificados).
    * `DELETE /urn/{id}`: Elimina una urna.
* **Imágenes de Urna (`UrnaImageService.kt`, Base: API v1):**
    * `GET /urn_image?urna_id={id}`: Obtiene las imágenes de la galería para una urna específica.
    * `POST /urn_image` (`@Multipart`): Añade una nueva imagen a la galería de una urna.
* **Subida Genérica (`UploadService.kt`, Base: API v1):**
    * `POST /upload/image` (`@Multipart`): Sube un archivo de imagen y devuelve sus metadatos (usado para cambiar imagen principal en Edición).
* **Catálogos (`ColorService.kt`, `MaterialService.kt`, `ModelService.kt`, Base: API v1):**
    * `GET /color`, `GET /color/{id}`
    * `GET /material`, `GET /material/{id}`
    * `GET /model`, `GET /model/{id}`

---

## 📁 Estructura del Proyecto

```

AppUrnas-e2af334d5cbce496cde64a18de9751f8e25c74d1/
├── .gitignore
├── .idea/                 \# Archivos de configuración de Android Studio
├── app/
│   ├── .gitignore
│   ├── build.gradle.kts     \# Dependencias y configuración del módulo app
│   ├── proguard-rules.pro   \# Reglas de ofuscación para Release
│   └── src/
│       ├── androidTest/     \# Tests instrumentados
│       ├── main/
│       │   ├── AndroidManifest.xml  \# Define Activities, permisos (INTERNET, READ\_MEDIA\_IMAGES, etc.)
│       │   ├── java/com/example/prueba2appurnas/
│       │   │   ├── App.kt           \# Clase Application (punto de entrada)
│       │   │   ├── api/             \# Clases de Networking (Retrofit, OkHttp, Servicios)
│       │   │   │   ├── ApiConfig.kt          \# Constantes URLs base
│       │   │   │   ├── AuthInterceptor.kt    \# Interceptor para añadir token JWT
│       │   │   │   ├── AuthService.kt        \# Endpoints de autenticación
│       │   │   │   ├── ColorService.kt       \# Endpoints para colores
│       │   │   │   ├── MaterialService.kt    \# Endpoints para materiales
│       │   │   │   ├── ModelService.kt       \# Endpoints para modelos
│       │   │   │   ├── RetrofitClient.kt     \# Factory para crear instancias de servicios
│       │   │   │   ├── TokenManager.kt       \# Gestión de token en SharedPreferences
│       │   │   │   ├── UploadService.kt      \# Endpoint genérico de subida de imágenes
│       │   │   │   ├── UrnaImageService.kt   \# Endpoints para galería de imágenes
│       │   │   │   └── UrnaService.kt        \# Endpoints CRUD para urnas
│       │   │   ├── model/           \# Data Classes (DTOs) para la API
│       │   │   │   ├── AuthResponse.kt     \# Respuesta de Login/Signup
│       │   │   │   ├── Color.kt            \# Modelo Color
│       │   │   │   ├── LoginRequest.kt     \# Payload de Login
│       │   │   │   ├── Material.kt         \# Modelo Material
│       │   │   │   ├── Model.kt            \# Modelo Modelo
│       │   │   │   ├── SignupRequest.kt    \# Payload de Signup
│       │   │   │   ├── Urna.kt             \# Modelo Urna (incluye ImageUrl anidado)
│       │   │   │   ├── UrnaImage.kt        \# Modelo para imagen de galería (incluye UrlObject)
│       │   │   │   └── User.kt             \# Modelo Usuario
│       │   │   ├── ui/              \# Interfaz de Usuario (Activities, Fragments, Adapters)
│       │   │   │   ├── fragments/
│       │   │   │   │   ├── AddUrnaFragment.kt       \# Formulario para añadir urna
│       │   │   │   │   ├── BaseUrnaFormFragment.kt  \# Lógica común formularios (spinners)
│       │   │   │   │   ├── EditUrnaFragment.kt      \# Formulario para editar/eliminar urna
│       │   │   │   │   ├── ProfileFragment.kt       \# Vista de perfil de usuario
│       │   │   │   │   ├── UrnaDetailFragment.kt    \# Vista de detalle de urna
│       │   │   │   │   └── UrnasFragment.kt         \# Lista/Dashboard/Búsqueda de urnas
│       │   │   │   ├── HomeActivity.kt      \# Actividad principal (host de fragmentos)
│       │   │   │   ├── MainActivity.kt      \# Actividad de Login
│       │   │   │   ├── RegisterActivity.kt  \# Actividad de Registro
│       │   │   │   ├── UrnaAdapter.kt       \# Adaptador para RecyclerView de urnas
│       │   │   │   ├── UrnaDetailActivity.kt \# (Posiblemente obsoleta, usar UrnaDetailFragment)
│       │   │   │   └── UrnaImageAdapter.kt  \# Adaptador para galería de imágenes
│       │   │   └── util/
│       │   │       └── NetUtils.kt          \# Helpers para URLs y autenticación Glide
│       │   └── res/
│       │       ├── color/             \# Selectores de color (bottom\_nav\_item\_color\_selector.xml)
│       │       ├── drawable/          \# Imágenes (logos, fondos, iconos) y shapes XML
│       │       ├── layout/            \# Archivos XML de UI para Activities y Fragments
│       │       ├── layout-land/       \# Layouts específicos para orientación horizontal
│       │       ├── menu/              \# Menú para BottomNavigationView (bottom\_nav\_menu.xml)
│       │       ├── mipmap-.../        \# Iconos adaptativos y legacy de la app
│       │       ├── values/            \# Recursos (colors.xml, strings.xml, themes.xml)
│       │       ├── values-night/      \# Recursos para modo oscuro
│       │       └── xml/               \# Configuraciones XML (network\_security\_config.xml, rules)
│       └── test/                \# Tests unitarios
├── build.gradle.kts         \# Configuración Gradle a nivel de proyecto
├── gradle/wrapper/          \# Gradle Wrapper
│   ├── gradle-wrapper.jar     \# Binario del Wrapper
│   └── gradle-wrapper.properties \# Configuración del Wrapper (versión Gradle)
├── gradle.properties        \# Propiedades globales de Gradle
├── gradlew                  \# Script Gradle Wrapper (Linux/Mac)
├── gradlew.bat              \# Script Gradle Wrapper (Windows)
└── settings.gradle.kts      \# Define los módulos incluidos en el proyecto

```

---

## Configuración de Android y Gradle

* **namespace:** `com.example.prueba2appurnas`
* **compileSdk:** 36
* **minSdk:** 24
* **targetSdk:** 36
* **versionCode:** 1
* **versionName:** "1.0"
* **compileOptions:** `JavaVersion.VERSION_11`
* **kotlinOptions:** `jvmTarget = "11"`
* **buildFeatures:** `viewBinding = true`
* **Plugins (app):** `com.android.application`, `org.jetbrains.kotlin.android`, `kotlin-kapt` (para Glide).
* **Librerías Principales (de `gradle/libs.versions.toml` y `app/build.gradle.kts`):**
    * AndroidX: `core-ktx`, `appcompat`, `constraintlayout`, `recyclerview`, `lifecycle-runtime-ktx`.
    * Material Design: `com.google.android.material:material`.
    * Networking: `retrofit`, `converter-gson`, `okhttp`, `logging-interceptor`.
    * Coroutines: `kotlinx-coroutines-android`.
    * Image Loading: `glide`, `glide:compiler`.
    * Testing: `junit`, `androidx.test.ext:junit`, `espresso-core`.

---

## Endpoints Utilizados

* **Base Auth:** `https://x8ki-letl-twmt.n7.xano.io/api:hfExqmJb`
    * `POST /auth/login` → `AuthResponse`
    * `POST /auth/signup` → `AuthResponse`
    * `GET /auth/me` → `User`
* **Base API v1:** `https://x8ki-letl-twmt.n7.xano.io/api:faArLfKY`
    * `GET /urn` → `List<Urna>`
    * `GET /urn/{id}` → `Urna`
    * `POST /urn` (Multipart) → `Urna`
    * `PATCH /urn/{id}` → `Urna`
    * `DELETE /urn/{id}` → `Void`
    * `GET /color` → `List<Color>`
    * `GET /material` → `List<Material>`
    * `GET /model` → `List<Model>`
    * `POST /upload/image` (Multipart) → `ImageUrl`
    * `GET /urn_image?urna_id={id}` → `List<UrnaImage>`
    * `POST /urn_image` (Multipart) → `UrnaImage`

---

## Detalle de Módulos y Clases Principales

### `api/`

* `ApiConfig`: Centraliza las URLs base de la API.
* `RetrofitClient`: Fábrica Singleton para crear instancias de servicios Retrofit, configurando OkHttp con logging y el `AuthInterceptor`.
* `AuthInterceptor`: Interceptor de OkHttp que añade el header `Authorization: Bearer <token>` a las peticiones requeridas.
* `TokenManager`: Gestiona el almacenamiento (lectura/escritura/borrado) del token de autenticación en `SharedPreferences`.
* `AuthService`: Define los endpoints para login, signup y obtener perfil (`/auth/me`).
* `UrnaService`: Define los endpoints CRUD para las urnas (`/urn`).
* `UploadService`: Define el endpoint genérico para subir imágenes (`/upload/image`).
* `UrnaImageService`: Define los endpoints para gestionar la galería de imágenes de una urna (`/urn_image`).
* `ColorService`, `MaterialService`, `ModelService`: Definen los endpoints para obtener los catálogos de colores, materiales y modelos.

### `model/`

* Contiene todas las `data class` que mapean las respuestas y payloads JSON de la API (e.g., `Urna`, `User`, `UrnaImage`, `AuthResponse`, `LoginRequest`). `Urna` y `UrnaImage` implementan `Serializable` para poder pasarlas entre fragments/activities.

### `ui/`

* `MainActivity`: Pantalla de inicio de sesión. Verifica si ya existe un token válido al iniciar; si no, maneja el flujo de login y navega a `HomeActivity`.
* `RegisterActivity`: Pantalla para el registro de nuevos usuarios.
* `HomeActivity`: Actividad principal que contiene el `BottomNavigationView` y hospeda los fragmentos principales (`UrnasFragment`, `AddUrnaFragment`, `ProfileFragment`). Gestiona la navegación base y limpia el backstack al cambiar de sección.
* `UrnaAdapter`: `RecyclerView.Adapter` para mostrar la lista de urnas en `UrnasFragment`. Implementa `Filterable` para la búsqueda y maneja el clic para navegar al detalle.
* `UrnaImageAdapter`: `RecyclerView.Adapter` para mostrar la galería horizontal de imágenes en `UrnaDetailFragment`.

### `ui/fragments/`

* `UrnasFragment`: Muestra el dashboard de métricas, la barra de búsqueda y la lista de urnas (`RecyclerView` + `UrnaAdapter`). Carga los datos iniciales desde la API.
* `AddUrnaFragment`: Formulario para crear una nueva urna. Hereda de `BaseUrnaFormFragment`. Maneja la selección y subida de la imagen principal.
* `EditUrnaFragment`: Formulario para editar una urna existente. Hereda de `BaseUrnaFormFragment`. Carga los datos de la urna, permite modificar campos, cambiar imagen principal, añadir a galería y eliminar la urna.
* `ProfileFragment`: Muestra los datos del usuario obtenidos de `/auth/me` y permite cerrar sesión.
* `UrnaDetailFragment`: Muestra todos los detalles de una urna, incluyendo la imagen principal y la galería secundaria (`RecyclerView` + `UrnaImageAdapter`). Permite navegar a `EditUrnaFragment`.
* `BaseUrnaFormFragment`: Clase abstracta con la lógica común para `AddUrnaFragment` y `EditUrnaFragment`, principalmente la carga y preselección de datos en los Spinners (Color, Material, Modelo) de forma secuencial.

### `util/`

* `NetUtils`: Contiene funciones de utilidad para construir URLs absolutas correctas (considerando si vienen de `/vault/` o no) y para crear un `GlideUrl` con el header de autenticación necesario para cargar imágenes protegidas con Glide.

---

## Layouts y ViewBinding

Se utiliza ViewBinding en todas las Activities y Fragments para un acceso seguro y tipado a las vistas definidas en los XML.

* `activity_main.xml` ↔ `ActivityMainBinding`
* `activity_register.xml` ↔ `ActivityRegisterBinding`
* `activity_home.xml` ↔ `ActivityHomeBinding`
* `fragment_urnas.xml` ↔ `FragmentUrnasBinding`
* `fragment_add_urna.xml` ↔ `FragmentAddUrnaBinding`
* `fragment_edit_urna.xml` ↔ `FragmentEditUrnaBinding`
* `fragment_profile.xml` ↔ `FragmentProfileBinding`
* `fragment_urna_detail.xml` ↔ `FragmentUrnaDetailBinding`
* `item_urna_card.xml` ↔ Usado por `UrnaAdapter` (acceso directo con `findViewById`)
* `item_urna_image.xml` ↔ Usado por `UrnaImageAdapter` (acceso directo con `findViewById`)
* `item_metric_card.xml` ↔ Inflado dinámicamente en `UrnasFragment`

---

## Flujo de Autenticación y Sesión

1.  Usuario abre la app. `MainActivity` verifica `TokenManager.isLoggedIn()`.
2.  Si **no** hay token, muestra el formulario de login.
3.  Usuario ingresa credenciales y presiona "Iniciar Sesión".
4.  Se llama a `AuthService.login` (sin token).
5.  Si es exitoso, se recibe `AuthResponse` con el token.
6.  Se guarda el token usando `TokenManager.saveToken()`.
7.  Se navega a `HomeActivity`, que carga `UrnasFragment` por defecto.
8.  Para las llamadas subsecuentes a endpoints protegidos (ej. `/urn`, `/auth/me`), `RetrofitClient` usa el `OkHttpClient` configurado con `AuthInterceptor`, el cual obtiene el token de `TokenManager` y lo añade a la cabecera `Authorization`.
9.  En `ProfileFragment`, al presionar "Cerrar Sesión", se llama a `TokenManager.clearToken()`, se navega de vuelta a `MainActivity` y se limpia la pila de actividades.

---

## Flujo de Urnas (CRUD)

* **Listado/Dashboard:** `UrnasFragment` llama a `UrnaService.getUrnas()` al iniciar. Los datos se usan para popular el dashboard y el `RecyclerView` via `UrnaAdapter`. La búsqueda filtra la lista localmente en el adapter.
* **Detalle:** Al hacer clic en un item de `UrnaAdapter`, se crea una instancia de `UrnaDetailFragment` pasándole la `Urna` seleccionada. Este fragmento llama a `UrnaImageService.getImagesByUrnaId()` para cargar la galería.
* **Creación:** `AddUrnaFragment` (hereda de `BaseUrnaFormFragment`) carga los spinners. Al guardar, valida los campos, prepara un `MultipartBody` con los datos y la imagen, y llama a `UrnaService.createUrnaMultipart()`.
* **Edición:** Al hacer clic en "Editar" en `UrnaDetailFragment`, se navega a `EditUrnaFragment`, pasándole la `Urna`. Este fragmento (hereda de `BaseUrnaFormFragment`) carga spinners y pre-popula los campos. Al guardar:
    * Si se cambió la imagen principal: Llama a `UploadService.uploadImage()` para subir la nueva, obtiene la `ImageUrl` resultante, y luego llama a `UrnaService.updateUrna()` (PATCH) enviando solo los campos modificados (incluida la nueva `image_url`).
    * Si no se cambió la imagen principal: Llama directamente a `UrnaService.updateUrna()` (PATCH) con los demás campos modificados.
    * Si se añadieron imágenes a la galería: Llama a `UrnaImageService.addUrnaImageMultipart()` para cada nueva imagen seleccionada.
* **Eliminación:** En `EditUrnaFragment`, el botón "Eliminar" muestra un diálogo de confirmación. Si se confirma, llama a `UrnaService.deleteUrna()`.

---

## Flujo de Creación y Subida de Imágenes

1.  **Crear Urna (`AddUrnaFragment`):**
    * Usuario selecciona **una** imagen principal usando `ActivityResultContracts.StartActivityForResult()`.
    * Al guardar, la URI de la imagen se convierte en `MultipartBody.Part` usando `createImagePart()`.
    * Se llama a `UrnaService.createUrnaMultipart()`, enviando todos los datos del formulario y la imagen principal como partes `multipart/form-data`. La API de Xano recibe todo en una sola petición.
2.  **Editar Urna (`EditUrnaFragment`):**
    * **Cambiar Imagen Principal:**
        * Usuario selecciona **una** nueva imagen principal (`pickSingleImageLauncher`).
        * Al guardar, se sube **primero** esta imagen al endpoint genérico `/upload/image` usando `UploadService.uploadImage()`.
        * Si la subida es exitosa, se obtiene la `ImageUrl` devuelta por la API.
        * Luego, se llama a `UrnaService.updateUrna()` (PATCH) incluyendo el nuevo objeto `image_url` en el mapa de datos a actualizar.
    * **Añadir a Galería:**
        * Usuario selecciona **una o varias** imágenes usando `ActivityResultContracts.GetMultipleContents()` (`pickMultipleImagesLauncher`).
        * Se itera sobre las URIs seleccionadas y para cada una se llama a `UrnaImageService.addUrnaImageMultipart()`, enviando el `urna_id` y el archivo de imagen como partes `multipart/form-data`.

---

## Compilación y Ejecución

* Clonar el repositorio.
* Abrir el proyecto en Android Studio.
* Asegurarse de tener un JDK compatible (Java 11 o superior configurado para el proyecto).
* Compilar el proyecto (Build > Make Project). Android Studio descargará las dependencias de Gradle y generará las clases de ViewBinding.
* Ejecutar la aplicación en un emulador o dispositivo físico (Run > Run 'app').

---

## Pruebas

El proyecto incluye archivos básicos para:

* **Tests Unitarios:** `app/src/test/java/.../ExampleUnitTest.kt`. Se ejecutan en la JVM local.
* **Tests Instrumentados:** `app/src/androidTest/java/.../ExampleInstrumentedTest.kt`. Se ejecutan en un dispositivo o emulador Android.

---

## Seguridad y Buenas Prácticas

* **Gestión de Tokens:** `TokenManager` usa `SharedPreferences` estándar, lo cual **no es seguro** para almacenar tokens sensibles en producción. Considerar usar `EncryptedSharedPreferences` o Jetpack DataStore con cifrado.
* **Errores de Red:** El manejo de errores de API es básico (muestra Toasts). En una app real, se debería implementar un manejo más robusto (ej. mostrar mensajes específicos, reintentos, estados de error en la UI).
* **Validaciones:** Las validaciones de formularios son simples. Se podrían añadir validaciones más complejas (formato de email, rangos de precio/stock, etc.).
* **Cleartext Traffic:** `network_security_config.xml` permite tráfico HTTP no cifrado a dominios específicos. Asegurarse que la API de Xano siempre use HTTPS en producción.
* **Permisos:** La app solicita permisos para leer almacenamiento externo/imágenes (`READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES`). El flujo de solicitud de permisos está implementado en `AddUrnaFragment` y `EditUrnaFragment`.
* **RecyclerView:** Se usa `notifyDataSetChanged()` en `UrnaAdapter`. Para listas potencialmente grandes, usar `ListAdapter` con `DiffUtil` mejoraría el rendimiento.
* **Abstracción:** Se utiliza una clase base (`BaseUrnaFormFragment`) para compartir lógica entre los formularios de añadir y editar, lo cual es una buena práctica.

---

## Contribución

* Ajustar las URLs base en `app/build.gradle.kts` si se clona el backend de Xano.
* Los modelos (`model/`) deben mantenerse sincronizados con la estructura de respuesta de la API.
* Considerar añadir tests unitarios para la lógica de negocio y tests instrumentados para los flujos de UI.
