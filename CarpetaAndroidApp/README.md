# Videojuegos Android Tienda

Aplicación Android nativa en Kotlin para explorar un catálogo de videojuegos, ver detalles, gestionar un carrito local y crear nuevos videojuegos subiendo una portada a través de una API externa. Incluye autenticación por token, navegación inferior y toasts personalizados.

## Tecnologías y versiones

- Kotlin `2.0.21` y Android Gradle Plugin `8.12.3`.
- AndroidX: `core-ktx`, `appcompat`, `activity`, `constraintlayout`, `recyclerview`, `lifecycle-runtime-ktx`.
- Material Components `1.13.0`.
- Networking: `Retrofit 2.9.0`, `OkHttp Logging 4.12.0`, `Gson Converter 2.9.0`.
- Asincronía: `kotlinx-coroutines-android 1.8.1`.
- Imágenes: `Coil 2.6.0`.
- Inspector de red: `Chucker 4.0.0` (debug) y `library-no-op` (release).
- Soporte adicional: `Okio 3.9.0` para manejo de streams.

## Cómo opera (arquitectura y flujo)

- Configuración de API:
  - Base URLs en `ApiConfig`: `STORE_BASE_URL` y `AUTH_BASE_URL` (Xano).
  - Clase `RetrofitProvider` centraliza la creación de `Retrofit` y `OkHttpClient`.
    - Interceptor de autenticación añade `Authorization: Bearer <token>` si existe en `TokenStore`.
    - `HttpLoggingInterceptor` y `Chucker` para trazas y depuración de tráfico.
    - `Gson` registra `AuthTokenResponseDeserializer` para tolerar múltiples formatos de token (`token`, `authToken`, `access_token`, `jwt`).
- Autenticación:
  - `AuthService` expone `auth/login`, `auth/register` y `auth/me` (además de `auth/signup` como fallback).
  - `StoreRepository.login/register` guardan el token en `TokenStore` para futuras llamadas.
  - Pantallas que requieren sesión (`CartActivity`, `ProfileActivity`, `AddVideogameActivity`) redirigen a `LoginActivity` si el token está vacío.
- Catálogo y filtros:
  - `MainActivity` carga plataformas, géneros y videojuegos desde `StoreRepository` y aplica filtros con `SearchView` + `Spinner`.
  - `VideogameAdapter` usa `Coil` para cargar portadas y navega al detalle con un listener.
- Detalle y carrito:
  - `DetailActivity` muestra datos y agrega al carrito con `CartManager.add`.
  - `CartManager` mantiene un mapa en memoria con cantidades, total y utilidades (`increase`, `decrease`, `clear`).
  - `CartActivity` renderiza items, actualiza cantidades y muestra total acumulado; el botón “Pagar” simula compra y vuelve al inicio.
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
    - `MainActivity.kt`: catálogo y filtros.
    - `ui/`
      - `auth/`: `LoginActivity.kt`, `SignupActivity.kt`.
      - `cart/`: `CartActivity.kt`.
      - `detail/`: `DetailActivity.kt`.
      - `profile/`: `ProfileActivity.kt`.
      - `upload/`: `AddVideogameActivity.kt`.
      - `main/`: `VideogameAdapter.kt`, `SimpleItemSelectedListener.kt`.
    - `data/`
      - `api/`: `AuthService.kt`, `StoreService.kt`.
      - `network/`: `ApiConfig.kt`, `RetrofitProvider.kt`, `TokenStore.kt`, `AuthTokenResponseDeserializer.kt`.
      - `repository/`: `StoreRepository.kt`.
      - `entities/`: modelos (`Videogame`, `Genre`, `Platform`, `FileInfo`, `UploadResponse`, `CartProduct`, etc.).
      - `cart/`: `CartManager.kt`.
      - `upload/`: `UploadClient.kt`, `UploadService.kt`, `UploadFilesService.kt`, `UploadFileService.kt`.
      - `functions/`: `setupBottomNavigation`, toasts personalizados.
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

- Auth (`AUTH_BASE_URL`): `auth/login`, `auth/register`, `auth/signup` (fallback), `auth/me`.
- Store (`STORE_BASE_URL`): `videogame` (GET/POST), `platform` (GET), `genre` (GET), `cart`, `cart_item/{id}`.
- Upload: `upload/image` (absoluto), `file/upload`, `files/upload` (fallbacks).

## Consideraciones de seguridad y depuración

- El token se almacena en `TokenStore` (en memoria). No persiste entre lanzamientos.
- `Chucker` sólo se incluye en debug; en release se usa `library-no-op`.
- `HttpLoggingInterceptor` está en nivel `BODY` para desarrollo; ajusta en producción según necesidad.

## Diseño visual

- Tarjetas con borde negro y fondo claro (`MaterialCardView` + `cart_item_border.xml`).
- Barra inferior fija y toolbar con acciones (login, subir videojuego, carrito, refrescar).
- Toasts personalizados para estados OK y error.

## Testing y mantenimiento

- Pruebas instrumentadas y unitarias base (`junit`, `androidx.test.ext:junit`, `espresso`).
- Si cambian los formatos de respuesta de token, `AuthTokenResponseDeserializer` tolera variantes comunes.
- Ajusta las base URLs en `ApiConfig` si tu backend varía.