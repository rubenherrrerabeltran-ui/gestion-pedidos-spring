# Arquitectura e integración de módulos (v1.3.0)

Evidencia **GA8-220501096-AA1-EV01** · Desarrollar software a partir de la integración de sus módulos componentes.
Rubén Darío Herrera Beltrán · Ficha 3186691.

## 1. Estilo de arquitectura

Aplicación monolítica modular en **tres capas** (presentación, negocio y datos) sobre Spring Boot 4.1.1.
Cada capa solo conoce a la capa inmediatamente inferior y se comunica con ella mediante interfaces, de modo que
los módulos (usuarios, productos, pedidos y reportes) se integran a través de los servicios y los repositorios.

| Capa | Paquetes | Responsabilidad | Librerías / frameworks |
|---|---|---|---|
| Presentación web | `web.controlador`, `web.formulario`, `templates/` | Páginas HTML, formularios, mensajes | Spring MVC, Thymeleaf, HTML5 + CSS adaptable |
| Presentación API | `web.api`, `web.api.dto` | API REST JSON, errores RFC 9457 | Spring MVC `@RestController`, Jackson 3, `ProblemDetail` |
| Seguridad (transversal) | `web.seguridad`, `config` | Sesión, autorización por rol | `HandlerInterceptor`, anotaciones propias `@RequiereRol`, `@Publico` |
| Negocio | `servicio`, `servicio.reporte`, `util`, `excepcion` | Reglas de negocio, transacciones, validación | Spring `@Service`, `@Transactional`, `java.time`, `BigDecimal` |
| Datos | `repositorio`, `repositorio.jdbc`, `db/*.sql` | Acceso a la base de datos | Spring `JdbcClient`, MySQL Connector/J 9, H2 2 |
| Pruebas | `src/test/java` | Pruebas unitarias y de integración | JUnit 5 (Jupiter), Spring Boot Test, JaCoCo 0.8.13 |

## 2. Diagramas

| Diagrama | Archivo |
|---|---|
| Paquetes | [`diagramas/paquetes.png`](diagramas/paquetes.png) |
| Componentes y capas | [`diagramas/componentes.png`](diagramas/componentes.png) |
| Clases del módulo de reportes integrado | [`diagramas/clases.png`](diagramas/clases.png) |
| Mapa de navegación | [`diagramas/navegacion.png`](diagramas/navegacion.png) |

Los archivos `.dot` (Graphviz) están junto a cada imagen para poder editarlos.

## 3. Módulos y su integración

| Módulo | Componentes | Se integra con |
|---|---|---|
| Usuarios y seguridad | `UsuarioServicio`, `UsuarioRepositorioJdbc`, `ClaveUtil`, `SesionInterceptor` | Todos (la sesión identifica rol y cliente) |
| Productos e inventario | `ProductoServicio`, `ProductoRepositorioJdbc` | Pedidos (descuenta y devuelve stock) |
| Pedidos | `PedidoServicio`, `PedidoRepositorioJdbc`, `Pedido`, `DetallePedido` | Productos, Usuarios (cliente) |
| **Reportes (nuevo en v1.3.0)** | `ReporteServicio`, `ReporteVentas`, `VentaProducto`, `VentaCliente`, `ReporteControlador`, `ReporteApiControlador` | Pedidos + Productos + Usuarios: consolida ventas, estados, productos más vendidos y ventas por cliente |
| Tablero | `InicioControlador`, `ResumenApiControlador` | Productos y Pedidos |

El módulo de reportes es el ejemplo de integración: no accede a tablas propias, sino que reutiliza la interfaz
`PedidoRepositorio` (con sus líneas de detalle, productos y clientes) y produce un reporte para la vista web,
para la API (`GET /api/reportes/ventas`) y para un archivo CSV (`/reportes/pedidos.csv`).

## 4. Patrones de diseño aplicados

| Patrón | Dónde | Para qué |
|---|---|---|
| MVC (Modelo-Vista-Controlador) | `web.controlador` + `templates/` + `modelo` | Separar la presentación de la lógica |
| Arquitectura en capas / Service Layer | `servicio` | Centralizar reglas y transacciones |
| Repository | `repositorio` (interfaces) + `repositorio.jdbc` | Aislar el acceso a datos; permite sustituirlo en pruebas |
| Inyección de dependencias | Constructores de servicios y controladores | Bajo acoplamiento; Spring crea e inyecta los beans |
| DTO (Data Transfer Object) | `web.api.dto`, `servicio.reporte` (records) | Contratos JSON estables e inmutables |
| Interceptor / Chain of Responsibility | `SesionInterceptor` | Validar sesión y rol antes de cada controlador |
| Front Controller | `DispatcherServlet` de Spring MVC | Un único punto de entrada HTTP |
| Template Method | `JdbcClient` / plantillas Thymeleaf | Reutilizar el flujo y variar solo lo específico |
| Strategy (reloj) | `Clock` inyectado en `ReporteServicio` (`RelojConfig`) | Fechas deterministas en pruebas |
| Test Double (Fake) | `PedidoRepositorioEnMemoria` | Probar el servicio de reportes sin base de datos |

## 5. Componentes reutilizables

- `util/Validador` — validaciones de texto, números, fechas y rangos usadas por todos los servicios.
- `util/ClaveUtil` — hash PBKDF2-HMAC-SHA256 (120 000 iteraciones, sal aleatoria) reutilizado desde AA2-EV01.
- `web/seguridad/@RequiereRol` y `@Publico` — anotaciones declarativas aplicables a cualquier controlador.
- `templates/fragmentos.html` — encabezado, menú y mensajes compartidos por todas las vistas.
- `web/api/ManejadorErroresApi` y `web/controlador/ManejadorErroresWeb` — manejo centralizado de excepciones.
- `ReporteServicio.textoCsv` — escape seguro de valores CSV reutilizable en otras exportaciones.

## 6. Mecanismos de seguridad

| Mecanismo | Implementación |
|---|---|
| Autenticación | Correo y contraseña; contraseña almacenada como hash PBKDF2 con sal (nunca en texto plano) |
| Sesión | Cookie `JSESSIONID` (sin id en la URL), expira a los 30 min, `changeSessionId()` al iniciar sesión (evita fijación) |
| Autorización por rol | `SesionInterceptor` + `@RequiereRol` (ADMINISTRADOR, VENDEDOR, CLIENTE); 401 sin sesión y 403 sin permiso |
| Datos propios | Un CLIENTE solo ve y cancela sus pedidos |
| Validación en servidor | `Validador` en todos los servicios, además de la validación HTML5 del navegador |
| Inyección SQL | Consultas parametrizadas con `JdbcClient` (`:parametro`), nunca concatenación |
| XSS | Thymeleaf escapa la salida (`th:text`) |
| Inyección en CSV | Valores que empiezan con `= + - @` se neutralizan con `'` |
| Credenciales | Usuario y clave de MySQL en variables de entorno `DB_USUARIO`, `DB_CLAVE` |
| Errores | `ProblemDetail` sin trazas internas en la respuesta |

## 7. Metodología

Desarrollo iterativo e incremental (ciclos cortos inspirados en Scrum): cada evidencia es un incremento
versionado con una etiqueta Git, probado antes de publicarse.

| Versión | Incremento |
|---|---|
| v1.0.0 | Módulo Spring Boot base (usuarios, productos, pedidos) |
| v1.1.0 | Validaciones completas y pruebas del módulo |
| v1.2.0 | API REST completa y pruebas con Postman |
| v1.3.0 | Módulo de reportes integrado, pruebas unitarias con doble de prueba, cobertura JaCoCo y documentación de arquitectura y ambientes |
