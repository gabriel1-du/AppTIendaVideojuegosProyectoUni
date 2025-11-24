# Videojuegos Android Tienda

Aplicación Android nativa en Kotlin para explorar un catálogo de videojuegos, ver detalles, gestionar un carrito local y crear nuevos videojuegos subiendo una portada a través de una API externa. Incluye autenticación por token, navegación inferior y toasts personalizados.

## Tecnologías y versiones

- Kotlin `2.0.21` y Android Gradle Plugin `8.12.3`.
- AndroidX: `core-ktx`, `appcompat`, `activity`, `constraintlayout`, `recyclerview`, `lifecycle-runtime-ktx`.
- Material Components (Material Design 3) `1.13.0`.
- Networking: `Retrofit 2.9.0`, `OkHttp 4.12.0`, `Gson Converter 2.9.0`.
- Asincronía: `kotlinx-coroutines-android 1.8.1` (cargas paralelas con `async/await`).
- Imágenes: `Coil 2.6.0` con caché automático y `ImageView.load(...)`.
- Inspector de red: `Chucker 4.0.0` (debug) y `library-no-op` (release).
- Soporte adicional: `Okio 3.9.0`.

## Cómo opera (arquitectura y flujo)

- Configuración de API:
  - Base URLs en `ApiConfig`: `STORE_BASE_URL`, `AUTH_BASE_URL` y `CART_BASE_URL` (Xano).
  - Clase `RetrofitProvider` centraliza la creación de `Retrofit` y `OkHttpClient`.
    - Interceptor de autenticación añade `Authorization: Bearer <token>` si existe en `TokenStore`.
    - Rate limiting y reintentos para mitigar `429`.
    - Cache HTTP en `GET` con `Cache-Control`.
    - `HttpLoggingInterceptor` y `Chucker` para trazas y depuración.
    - `Gson` registra `AuthTokenResponseDeserializer` para tolerar múltiples formatos de token (`token`, `authToken`, `access_token`, `jwt`).
- Autenticación:
  - `AuthService` expone `auth/login`, `auth/signup` y `auth/me`.
  - `AuthRepository` persiste el token en `SharedPreferences` y en `TokenStore`.
  - Tras login se verifica `bloqueo` con `auth/me`; si está bloqueado se cierra sesión y se muestra un toast.
- Catálogo y filtros:
  - `HomeFragment` carga plataformas, géneros y videojuegos en paralelo (coroutines) y aplica filtros con `SearchView` + spinners.
  - Los adapters usan `Coil` para cargar portadas y navegar al detalle.
- Detalle y carrito:
  - `DetailActivity` muestra datos y agrega al carrito.
  - `OrdersDashboardFragment` y `CartDetailActivity` permiten aprobar, rechazar y eliminar carritos vía API.
- Subida de videojuego:
  - `AddVideogameActivity` permite seleccionar imagen del dispositivo y crear videojuego.
  - `UploadClient` intenta subir la portada vía `/upload/image` con reintentos; si falla aplica fallbacks (`/file/upload`, `/files/upload`).
  - Tras subir, se llama a `StoreRepository.createVideogameJson` que construye el JSON completo con `cover_image` y hace POST a `videogame` (con fallback absoluto si hay 404).
- UX y navegación:
  - `setupBottomNavigation` coordina la barra inferior entre Buscar, Carrito y Perfil.
  - `showCustomOkToast` y `showCustomErrorToast` renderizan toasts personalizados.

## Estructura de archivos y carpetas

- `app/`: módulo principal.
  - `src/main/AndroidManifest.xml`: declara actividades, tema y permisos (`INTERNET`).
- `src/main/java/com/example/videojuegosandroidtienda/`:
    - `App.kt`: inicializa `appContext` global.
    - `MainActivity.kt`: navegación y estado de sesión.
    - `ui/`
      - `auth/`: `LoginFragment.kt`, `SignUpFragment.kt`.
      - `adminUi/`: dashboards de usuarios, videojuegos y órdenes.
      - `detail/`: `DetailActivity.kt`.
      - `fragments/`: `HomeFragment.kt` y otros.
      - `upload/`: `AddVideogameActivity.kt`.
    - `data/`
      - `api/`: `AuthService.kt`, `StoreService.kt`, `CartService.kt`.
      - `network/`: `ApiConfig.kt`, `RetrofitProvider.kt`, `TokenStore.kt`, `AuthTokenResponseDeserializer.kt`.
      - `repository/`: `AuthRepository.kt`, `StoreRepository/*`.
      - `entities/`: modelos.
      - `upload/`: `UploadClient.kt` y servicios de upload.
      - `functions/`: utilidades y toasts personalizados.
  - `src/main/res/`
    - `layout/`: `activity_*` y vistas de ítem (`item_videogame.xml`, `view_cart_product.xml`).
    - `menu/`: `menu_main.xml`, `menu_bottom_nav.xml`.
    - `drawable/`: recursos gráficos y estilos de borde (`cart_item_border.xml`).
    - `values/`: `colors.xml`, `strings.xml`, `themes.xml`.
    - `xml/`: `backup_rules.xml`, `data_extraction_rules.xml`.
- `build.gradle.kts` y `gradle/libs.versions.toml`: gestión de dependencias y versiones.

## Uso: instalación y ejecución

- Requisitos: Android Studio (última versión), SDK 24+, JDK 11.
- Pasos:
  - Abrir el proyecto (`settings.gradle.kts` incluye `:app`).
  - Sincronizar Gradle y compilar.
  - Ejecutar en emulador o dispositivo físico.
- Inicio:
  - La app arranca en `MainActivity` con catálogo y filtros.
  - Para acceder a Carrito/Perfil/Subir videojuego, primero inicia sesión o regístrate.

## Endpoints de backend (Xano)

- Auth (`AUTH_BASE_URL`): `auth/login`, `auth/signup`, `auth/me`.
- Store (`STORE_BASE_URL`): `videogame` (GET/POST/PATCH/DELETE), `platform` (GET), `genre` (GET), `cart` (GET), `cart_item/{id}` (GET).
- Cart (`CART_BASE_URL`): `cart/{cart_id}` (GET/PATCH/DELETE).
- Upload: `upload/image` (absoluto), `file/upload`, `files/upload` (fallbacks).

## Consideraciones de seguridad y depuración

- El token se persiste en `SharedPreferences` y se mantiene en `TokenStore`.
- `Chucker` sólo se incluye en debug; en release se usa `library-no-op`.
- `HttpLoggingInterceptor` está en nivel `BODY` para desarrollo; ajusta en producción según necesidad.

## Diseño visual

- Material Design 3 con `MaterialButton`, `MaterialCardView`, `MaterialAutoCompleteTextView`, `SearchBar` y `SearchView`.
- Tarjetas con borde negro y fondo claro (`MaterialCardView` + `cart_item_border.xml`).
- Barra inferior fija y toolbar con acciones (login, subir videojuego, carrito, refrescar).
- Toasts personalizados para estados OK y error.

## Cómo se muestran las imágenes

- `Coil` carga imágenes en `ImageView` con `imageView.load(url)`.
- Soporta caché automática, placeholders y manejo de errores.
- Las URLs provienen del backend (p. ej. `UploadResponse.url`) tras subir archivos.

## Ejemplos rápidos

```kotlin
interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthTokenResponse

    @GET("auth/me")
    suspend fun getAuthMe(): User

    @POST("auth/signup")
    suspend fun signup(@Body req: SignupRequest): AuthTokenResponse
}

suspend fun loginAndFetch(authService: AuthService, email: String, password: String): User {
    val token = authService.login(LoginRequest(email, password)).token
    com.example.videojuegosandroidtienda.data.network.TokenStore.token = token
    return authService.getAuthMe()
}
```

```kotlin
interface StoreService {
    @PATCH("videogame/{id}")
    suspend fun updateVideogame(@Path("id") id: String, @Body req: VideogameUpdateRequest): Videogame
}

suspend fun updateTitle(service: StoreService, id: String, title: String): Videogame {
    return service.updateVideogame(
        id,
        VideogameUpdateRequest(
            title = title,
            price = null,
            description = null,
            genre_id = null,
            platform_id = null
        )
    )
}
```

```kotlin
imageView.load(url) {
    crossfade(true)
    placeholder(R.drawable.placeholder)
    error(R.drawable.image_error)
}
```

```kotlin
lifecycleScope.launch {
    val vg = async { repo.getVideogames() }
    val platforms = async { repo.getPlatforms() }
    val genres = async { repo.getGenres() }

    val list = vg.await()
    val pls = platforms.await()
    val gns = genres.await()
}
```

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/buttonApruebo"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/approve_purchase"/>
```

## Testing y mantenimiento

- Pruebas instrumentadas y unitarias base (`junit`, `androidx.test.ext:junit`, `espresso`).
- Si cambian los formatos de respuesta de token, `AuthTokenResponseDeserializer` tolera variantes comunes.
- Ajusta las base URLs en `ApiConfig` si tu backend varía.
