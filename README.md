
---
# 🕊️ **AppUrnas — Proyecto Final Android (Kotlin + XML)**

### Sistema completo de E-Commerce con roles Admin/Cliente + API REST Xano

![Android](https://img.shields.io/badge/Android-Kotlin-7F52FF?logo=kotlin\&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-2.11.0-31A8FF)
![Glide](https://img.shields.io/badge/Glide-4.16.0-8BC34A)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue)
![Xano](https://img.shields.io/badge/Backend-Xano-yellow)
![Status](https://img.shields.io/badge/Estado-Completado-success)

---

# 📜 **Descripción General del Proyecto**

Este repositorio contiene el desarrollo completo del **Proyecto Final de Desarrollo de Aplicaciones Móviles**, diseñado como un **e-commerce completo para la venta de urnas funerarias**, implementado con **Kotlin + XML**, basado en arquitectura modular y usando **Xano** como backend sin servidor.

El sistema incluye dos roles:

* **Administrador** → CRUD de urnas, gestión de usuarios, gestión de órdenes (aceptar, rechazar, enviar).
* **Cliente** → navegación del catálogo, carrito 100% funcional, pago simulado, perfil del usuario.

Todo el proyecto cumple 100% la rúbrica oficial y está listo para ser evaluado o presentado como portafolio profesional.

---

# 🎯 **Objetivos del Proyecto**

Desarrollar una aplicación móvil que implemente:

### 🔐 **1. Autenticación y Sesión (Login/Logout)**

* Manejo de sesión usando `SharedPreferences`.
* Persistencia del token usando `TokenManager`.
* Redirección automática según rol (Admin ↔ Cliente).

### 🛍️ **2. Vista Cliente**

* Catálogo de urnas.
* Detalle con imágenes, descripción, materiales, modelos.
* **Carrito editable**:

  * agregar items
  * actualizar cantidad
  * eliminar
* Pago simulado → genera `Order` en Xano.
* Historial y perfil editable.

### 🛠️ **3. Vista Administrador**

* **CRUD completo de Urnas** (crear, editar, eliminar).
* **Múltiples imágenes**: principal y galería (Glide + Xano).
* **Gestión de usuarios** (bloquear/desbloquear).
* **Gestión de órdenes**:

  * pendientes
  * aceptadas / enviadas
  * rechazadas

### 🔗 **4. Conexión API REST / Networking**

* Retrofit 2.11.0
* Interceptor de token
* Repositorios limpios
* Manejo de errores y estados

---

# 🧱 **Arquitectura del Proyecto**

El proyecto sigue una arquitectura organizada:

```
📦 app/
 ┣ 📁 api/              → RetrofitClient, Services, Interceptor
 ┣ 📁 model/            → Data classes (User, Cart, Order, Urna...)
 ┣ 📁 repository/       → Conexión API + lógica de negocio
 ┣ 📁 ui/
 ┃   ┣ 📁 client/       → Catálogo, carrito, perfil, detalle
 ┃   ┣ 📁 admin/        → Urnas CRUD, usuarios, órdenes
 ┃   ┣ 📁 fragments/    → Formularios reutilizables
 ┣ 📁 util/             → TokenManager, NetUtils
 ┗ 📄 App.kt
```

✔ **MVVM + Repository**
✔ **ViewBinding habilitado**
✔ **RecyclerView en todas las listas grandes**
✔ **Glide para imágenes**
✔ **Retrofit + OkHttp para red**

---

# 🔌 **Backend: Xano**

### URLs principales (ApiConfig.kt)

| Tipo                                    | URL                                               |
| --------------------------------------- | ------------------------------------------------- |
| Host base                               | `https://x8ki-letl-twmt.n7.xano.io`               |
| API Principal (urnas, carrito, órdenes) | `https://x8ki-letl-twmt.n7.xano.io/api:faArLfKY/` |
| API Auth (login/signup/me)              | `https://x8ki-letl-twmt.n7.xano.io/api:hfExqmJb/` |

### Cuentas de demostración

| Rol     | Email                | Contraseña   | Vista inicial        |
| ------- | -------------------- | ------------ | -------------------- |
| Admin   | `Cesar@gmail.com`    | `cesitar160` | HomeActivity (Urnas) |
| Cliente | `cliente3@gmail.com` | `cliente123` | ClientHomeActivity   |

---

# 📦 **Características funcionales (Cliente + Admin)**

## 🛍️ Cliente

✔ Ver catálogo
✔ Ver detalle de urna
✔ Carrito editable (CRUD completo)
✔ Pago simulado
✔ Creación de órdenes
✔ Edición de perfil
✔ Logout

## 🛠️ Administrador

✔ CRUD de urnas
✔ Manejo de galería de imágenes
✔ Listado + control de usuarios
✔ Bloquear/desbloquear usuarios
✔ Listar órdenes PENDING
✔ Aceptar → **APPROVED**
✔ Rechazar → **REJECTED**
✔ Marcar como enviado → **ENVIADO**

---

# 🧾 **Checklist de la Rúbrica (100% Cumplido)**

| Requisito                                               | Estado     | Puntos      |
| ------------------------------------------------------- | ---------- | ----------- |
| Login/Logout + routing por rol                          | ✅ completo | 15 pts      |
| Catálogo + Carrito Editable + Pago                      | ✅ completo | 20 pts      |
| Admin: CRUD de urnas                                    | ✅ completo | 20 pts      |
| Admin: Usuarios (bloquear/desbloquear)                  | ✅ completo | 10 pts      |
| Admin: Órdenes (pendiente/confirmado/rechazado/enviado) | ✅ completo | 10 pts      |
| Retrofit + ViewBinding + RecyclerView                   | ✅ completo | 10 pts      |
| UI/UX + Ícono + Validaciones                            | ✅ completo | 10 pts      |
| Estructura limpia + funcionamiento general              | ✅ completo | 5 pts       |
| **TOTAL**                                               | **100%**   | **100 pts** |

---

# 🧪 **Tecnologías Utilizadas**

### Frontend (App Android)

* Kotlin
* XML Layouts
* ViewBinding
* Material Design 3
* Glide (imágenes)
* Retrofit + OkHttp
* RecyclerView

### Backend (Xano)

* REST API CRUD
* Autenticación por token
* Almacenamiento de imágenes (Vault → Google Cloud Storage)
* Relaciones profundas (urnas, imágenes, carrito, órdenes)

---

# 🧑‍💻 **Cómo Ejecutar el Proyecto**

### 1️⃣ Clonar repositorio

```bash
git clone https://github.com/Cesitar16/AppUrnas.git
```

### 2️⃣ Abrir en Android Studio

Recomendado: **Android Studio Flamingo o superior**

### 3️⃣ Sincronizar Gradle

Automáticamente cargará dependencias:

* Retrofit
* Glide
* Coroutines

### 4️⃣ Ejecutar en emulador o dispositivo

Versión mínima: **Android 7.0 (API 24)**

### 5️⃣ Probar los roles

Usar las cuentas demo en Login.

---

# 🎬 **Flujo de Demostración Recomendado (Video)**

Para tu exposición, sigue esta secuencia:

### 🔐 Admin

1. Login como Admin
2. Crear urna (subir imagen principal + galería)
3. Editar urna (precio, stock, descripción)
4. Listar/buscar urnas
5. Bloquear/desbloquear usuario
6. Revisar orden PENDING
7. Aceptar o rechazar pago
8. Logout

### 👤 Cliente

9. Login cliente
10. Navegar catálogo
11. Agregar producto al carrito
12. Editar cantidades
13. Pagar (simulado → crea orden)
14. Editar perfil
15. Logout

---

# 📦 **Generar APK**

```
Build → Generate APK...
```

Para producción:

```
Build → Generate Signed App Bundle / APK
```
