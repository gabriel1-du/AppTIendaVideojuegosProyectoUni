# Videojuegos Android Tienda

Aplicación Android (Kotlin) para explorar videojuegos, ver detalles y gestionar un carrito de compras con navegación inferior fija.

## Estructura del proyecto

- `app/`: módulo principal de la app.
  - `src/main/java/com/example/videojuegosandroidtienda/`: código Kotlin.
    - `MainActivity.kt`: pantalla de búsqueda/catálogo con filtros y navegación.
    - `ui/detail/DetailActivity.kt`: detalle del videojuego y botón “Agregar al carrito”.
    - `ui/cart/CartActivity.kt`: carrito local con tarjetas, cantidades y total dinámico.
    - `ui/profile/ProfileActivity.kt`: perfil que muestra nombre y email del usuario.
    - `ui/auth/LoginActivity.kt`: inicio de sesión.
    - `ui/auth/SignupActivity.kt`: creación de cuenta.
    - `ui/main/VideogameAdapter.kt`: adaptador del listado de videojuegos.
    - `data/cart/CartManager.kt`: gestor en memoria del carrito.
    - `data/entities/`: entidades de dominio (p. ej. `CartProduct`).
    - `data/repository/StoreRepository.kt`: capa de acceso a servicios remotos (auth y store).
  - `src/main/res/layout/`: vistas XML (pantallas y componentes).
    - `activity_main.xml`, `activity_detail.xml`, `activity_cart.xml`, `activity_profile.xml`, etc.
    - `item_videogame.xml`: tarjeta del videojuego en la lista.
    - `view_cart_product.xml`: tarjeta de producto dentro del carrito.
  - `src/main/res/menu/`: menús de la barra superior e inferior.
    - `menu_main.xml`: acciones del toolbar (p. ej. carrito).
    - `menu_bottom_nav.xml`: navegación inferior (Buscar, Carrito, Perfil).
  - `src/main/res/values/colors.xml`: paleta de colores (fondo claro, texto negro, bordes).

## Características principales

- Búsqueda y filtrado por plataforma y género.
- Vista de detalle con imagen, información y “Agregar al carrito”.
- Carrito local con tarjetas, imagen, título, precio, cantidad (sumar/restar) y eliminar.
- Total acumulado dinámico y acción de “Pagar”.
- Navegación inferior fija entre Buscar, Carrito y Perfil.
- Perfil que muestra nombre y email del usuario autenticado.
- Paleta visual con fondo blanco, texto negro y borde negro en tarjetas.

## Tecnologías

- Kotlin, AndroidX, Material Components.
- Coil para carga de imágenes.
- Retrofit + OkHttp para API; Coroutines para asincronía.

## Configuración y ejecución

1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle y ejecutar en un dispositivo/emulador (minSdk 24).
3. Crear cuenta o iniciar sesión para acceder a Carrito/Perfil.

## Notas de diseño

- La barra inferior se fija usando `CoordinatorLayout` y `layout_gravity="bottom"`.
- Las tarjetas (`MaterialCardView`) usan borde negro (`strokeColor`) y fondo claro.
- Controles del carrito usan iconos compactos y alineación vertical centrada.