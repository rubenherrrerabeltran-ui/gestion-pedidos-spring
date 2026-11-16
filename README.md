# Sistema de Gestión de Pedidos — Módulo Spring Boot

Evidencias **GA7-220501096-AA3-EV01** (codificación con un framework, v1.0.0) y
**GA7-220501096-AA3-EV02** (módulo codificado y probado según los requerimientos, v1.1.0).
SENA · Tecnología en Análisis y Desarrollo de Software · Ficha 3186691 · **Rubén Darío Herrera Beltrán**

Este módulo reescribe el sistema de gestión de pedidos (usuarios, productos, pedidos y detalle de pedido)
con el framework **Spring Boot 4.1.1**. Reutiliza los artefactos de las evidencias anteriores: el modelo
físico de la base de datos (GA6), las entidades, las reglas de negocio y `ClaveUtil` de AA2-EV01 (JDBC), y el
diseño de las vistas y los estilos de AA2-EV02 (Servlets y JSP).

## Requerimientos implementados

| Código | Requerimiento (mockups GA6-220501096-AA3-EV02) | Dónde |
|---|---|---|
| RF01 | Autenticar con correo y contraseña | `/login`, `POST /api/sesion` |
| RF02 | Registrar usuarios con datos personales, documento y rol | `/registro` (clientes) y `/usuarios` (administrador) |
| RF03 | Indicadores, últimos pedidos y alertas de inventario | `/` (tablero) |
| RF04 | Consultar, buscar, filtrar, registrar y editar productos | `/productos`, `/api/productos` |
| RF05 | Registrar pedidos con detalle, subtotal, IVA y total | `/pedidos/nuevo`, `POST /api/pedidos` |
| RF06 | Consultar y filtrar pedidos por cliente, estado y fecha | `/pedidos?cliente=&estado=&desde=&hasta=` |
| RF07 | Detalle del pedido y cambio de estado | `/pedidos/{id}`, `PATCH /api/pedidos/{id}/estado` |
| RF08 | Seguimiento de pedidos por el cliente | `/pedidos` con rol CLIENTE |
| RF09 | Gestionar usuarios y roles | `/usuarios` |
| RNF01 | Interfaz web y móvil consistente | `estilos.css` (diseño adaptable) |

## Tecnologías

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 4.1.1 (Spring Framework 7) · Java 21 · Maven |
| Web (vistas) | Spring MVC (`@Controller`) + Thymeleaf |
| API | Spring MVC (`@RestController`) + Jackson (JSON) · errores con `ProblemDetail` |
| Negocio | Servicios `@Service` con `@Transactional` |
| Datos | `JdbcClient` de Spring (`@Repository`) · MySQL 8 (H2 en modo MySQL para demo y pruebas) |
| Servidor | Tomcat embebido |
| Pruebas | JUnit 5 sobre la aplicación completa (servicios, vistas y API) |

## Estructura

```
src/main/java/co/sena/gestionpedidos
├── GestionPedidosApplication.java   # @SpringBootApplication (punto de entrada)
├── config/WebConfig.java            # registra el interceptor de sesión y roles
├── modelo/                          # Usuario, Producto, Pedido, DetallePedido, Rol, EstadoPedido
├── repositorio/                     # interfaces de acceso a datos
│   └── jdbc/                        # implementaciones con JdbcClient (@Repository)
├── servicio/                        # reglas de negocio (@Service, @Transactional)
├── web/controlador/                 # controladores MVC + Thymeleaf
├── web/api/                         # API REST (@RestController) y DTO (records)
├── web/formulario/                  # objetos de formulario
├── web/seguridad/                   # UsuarioSesion, @Publico, @RequiereRol, SesionInterceptor
├── excepcion/                       # NegocioException, RecursoNoEncontradoException, AccesoDenegadoException
└── util/                            # ClaveUtil (PBKDF2) y Validador
src/main/resources
├── application.properties           # MySQL (por defecto)
├── application-demo.properties      # perfil demo con H2 en memoria
├── db/schema.sql · db/data.sql      # esquema y datos de prueba (spring.sql.init)
├── templates/                       # vistas Thymeleaf
└── static/css/estilos.css
```

## Ejecutar

Requisitos: JDK 21. Maven no es obligatorio: el proyecto trae Maven Wrapper (`mvnw` / `mvnw.cmd`), que lo descarga solo.
En Windows reemplace `mvn` por `mvnw.cmd` en los comandos siguientes.

**Con MySQL** (la base `sena_gestion_pedidos` se crea sola y las tablas y datos se cargan al iniciar):

```bash
# Windows PowerShell: $env:DB_USUARIO="root"; $env:DB_CLAVE="su_clave"
export DB_USUARIO=root DB_CLAVE=su_clave
mvn spring-boot:run
```

> Si ya tenía la base de la versión 1.0.0, bórrela una vez (`DROP DATABASE sena_gestion_pedidos;`) para que
> se cree con las columnas nuevas `documento` y `telefono`.

**Sin instalar MySQL** (perfil demo con H2 en memoria):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Abrir <http://localhost:8080>. Usuarios de prueba:

| Correo | Contraseña | Rol |
|---|---|---|
| admin@gestionpedidos.local | Admin2026* | ADMINISTRADOR |
| laura.vendedora@gestionpedidos.local | Vendedor2026* | VENDEDOR |
| carlos.cliente@gestionpedidos.local | Cliente2026* | CLIENTE |

## Permisos por rol

| Opción | ADMINISTRADOR | VENDEDOR | CLIENTE |
|---|:---:|:---:|:---:|
| Ver productos | ✔ | ✔ | ✔ |
| Crear / editar productos | ✔ | ✔ | — |
| Eliminar productos | ✔ | — | — |
| Gestionar usuarios | ✔ | — | — |
| Registrar pedidos | ✔ (a un cliente) | ✔ (a un cliente) | ✔ (propios) |
| Cambiar estado de pedidos | ✔ | ✔ | Cancelar los propios pendientes |

## API REST

19 ENDPOINT JSON (detalle completo, cuerpos y códigos de respuesta en [`docs/API.md`](docs/API.md)):

| Recurso | ENDPOINT | Acceso |
|---|---|---|
| Sesión | `POST` · `GET` · `DELETE /api/sesion` | Público para iniciar |
| Clientes | `POST /api/clientes` (registro) | Público |
| Productos | `GET /api/productos?texto=&filtro=` · `GET /api/productos/{id}` | Público |
| | `POST /api/productos` · `PUT /api/productos/{id}` · `DELETE /api/productos/{id}` | ADMINISTRADOR, VENDEDOR (eliminar: ADMINISTRADOR) |
| Pedidos | `GET /api/pedidos?cliente=&estado=&desde=&hasta=` · `GET` · `POST` · `PATCH /api/pedidos/{id}/estado` | Con sesión (el cliente solo ve y cancela los suyos) |
| Usuarios | `GET` · `POST /api/usuarios` · `GET` · `PUT` · `DELETE /api/usuarios/{id}` | ADMINISTRADOR |
| Indicadores | `GET /api/resumen` | ADMINISTRADOR, VENDEDOR |

Los errores se responden en formato ProblemDetail (RFC 9457) con 400, 401, 403, 404 o 422.

### Pruebas con Postman

En `postman/` están la colección (54 peticiones y 110 pruebas automáticas en 5 carpetas: catálogo público, vendedor,
pedidos, cliente y administrador) y el entorno local. En Postman: **Import** → seleccionar los dos archivos → elegir el
entorno *Gestión de Pedidos - Local* → clic derecho en la colección → **Run collection**.

Ejemplo con curl:

```bash
curl -c cookies.txt -H "Content-Type: application/json" \
     -d '{"correo":"laura.vendedora@gestionpedidos.local","clave":"Vendedor2026*"}' http://localhost:8080/api/sesion
curl -b cookies.txt -H "Content-Type: application/json" \
     -d '{"nombreProducto":"Té verde x 20","precio":12500,"stock":14}' http://localhost:8080/api/productos
```

## Validaciones

Cada dato se valida dos veces: en el navegador (atributos HTML5 `required`, `minlength`, `maxlength`, `pattern`,
`min`, `max`, `type="date"`) y en el servidor (`util/Validador.java`), porque una petición puede llegar sin pasar
por el formulario.

| Tipo | Reglas |
|---|---|
| Textos y longitudes | Obligatorios, sin espacios sobrantes; nombre 3–100, producto 3–120, correo hasta 150, contraseña 8–64 |
| Caracteres especiales | Nombres de persona solo con letras, espacios, `'` y `-`; productos sin `< > { } ; " $`; búsquedas con `%` o comillas tratadas como texto |
| Números | Documento de 6 a 10 dígitos; teléfono de 10 dígitos (3 o 6); precio > 0, ≤ 100 000 000 y 2 decimales; stock 0–100 000; cantidades enteras de 1 a 1 000 y sin superar el stock |
| Fechas | Formato AAAA-MM-DD con días reales, sin fechas futuras y "desde" ≤ "hasta" |

## Pruebas y estándar de codificación

```bash
mvn test                                   # 61 pruebas JUnit 5 (validaciones, servicios, vistas y API)
java herramientas/VerificadorEstandar.java # verifica las reglas de docs/estandares-codificacion.md
```

## Control de versiones

Commits con el formato *Conventional Commits* (`feat`, `fix`, `test`, `docs`, `chore`, `style`) y etiquetas
`v1.0.0` (AA3-EV01), `v1.1.0` (AA3-EV02) y `v1.2.0` (AA5-EV04, API completa y pruebas con Postman).
Ver `git log --oneline`.
