# Gestión de Pedidos — App móvil Android (v1.0.0)

Evidencia **GA8-220501096-AA2-EV02** · Rubén Darío Herrera Beltrán · Ficha 3186691.

Aplicación Android nativa (Java) que consume la API REST del Sistema de Gestión de Pedidos
(backend Spring Boot de este mismo repositorio). Permite iniciar sesión, consultar el catálogo,
armar un carrito y registrar pedidos (cliente), gestionar el estado de los pedidos y consultar el
reporte de ventas (administrador y vendedor).

## Módulos

| Módulo | Pantallas | ENDPOINT |
|---|---|---|
| Autenticación | `LoginActivity` | `POST /api/sesion`, `DELETE /api/sesion` |
| Inicio e indicadores | `InicioActivity` | `GET /api/resumen` |
| Catálogo | `ProductosActivity` | `GET /api/productos?texto=` |
| Carrito y registro de pedidos | `CarritoActivity` | `POST /api/pedidos` |
| Pedidos | `PedidosActivity`, `DetallePedidoActivity` | `GET /api/pedidos`, `GET /api/pedidos/{id}`, `PATCH /api/pedidos/{id}/estado` |
| Reportes | `ReporteActivity` | `GET /api/reportes/ventas` |

## Arquitectura por capas y paquetes

```
co.sena.gestionpedidos.movil
├── ui/            capa de presentación (Activities, adaptadores de RecyclerView, BaseActivity, Ejecutor)
├── dominio/       modelo, carrito y reglas de negocio (Java puro, probado con JUnit)
├── datos/
│   ├── remoto/    ApiGestionPedidos (fachada), TransporteHttp (estrategia), ApiException
│   ├── json/      lector/escritor JSON propio
│   ├── mapeo/     Mapeador JSON → dominio
│   └── local/     PreferenciasApp (SharedPreferences)
└── util/          Validador y Formatos
```

Librerías de la capa de presentación: AndroidX AppCompat 1.7.1, Activity 1.10.1, RecyclerView 1.4.0,
ConstraintLayout 2.2.1 y Material Components 1.12.0 (Material 3). Pruebas: JUnit 4.13.2, AndroidX Test
y Espresso 3.6.1. Construcción: Gradle 8.14.3 con Android Gradle Plugin 8.13.0, compileSdk/targetSdk 35,
minSdk 24 (Android 7.0).

## Compilar y probar

```bash
# Requiere JDK 17+ y el Android SDK (local.properties con sdk.dir)
gradlew assembleRelease            # APK firmado con la clave de depuración en app/build/outputs/apk/release
gradlew testDebugUnitTest          # pruebas unitarias locales (JUnit)
gradlew connectedDebugAndroidTest  # pruebas instrumentadas (Espresso) con un emulador abierto
```

Para probar en el emulador, inicie primero la API (`java -jar gestion-pedidos-spring-1.3.0.jar
--spring.profiles.active=demo`) y use el servidor `http://10.0.2.2:8080`.

Usuarios de prueba: `laura.vendedora@gestionpedidos.local` / `Vendedor2026*`,
`carlos.cliente@gestionpedidos.local` / `Cliente2026*`, `admin@gestionpedidos.local` / `Admin2026*`.
