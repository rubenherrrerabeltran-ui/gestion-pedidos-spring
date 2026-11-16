# Sistema de Gestión de Pedidos — Módulo Spring Boot

Evidencia **GA7-220501096-AA3-EV01** · Codificación de módulos del software (web) aplicando un framework.
SENA · Tecnología en Análisis y Desarrollo de Software · Ficha 3186691 · **Rubén Darío Herrera Beltrán**

Este módulo reescribe el sistema de gestión de pedidos (usuarios, productos, pedidos y detalle de pedido)
con el framework **Spring Boot 4.1.1**. Reutiliza los artefactos de las evidencias anteriores: el modelo
físico de la base de datos (GA6), las entidades, las reglas de negocio y `ClaveUtil` de AA2-EV01 (JDBC), y el
diseño de las vistas y los estilos de AA2-EV02 (Servlets y JSP).

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

Requisitos: JDK 21 y Maven 3.9 (o el IDE: NetBeans, IntelliJ, VS Code).

**Con MySQL** (la base `sena_gestion_pedidos` se crea sola y las tablas y datos se cargan al iniciar):

```bash
# Windows PowerShell: $env:DB_USUARIO="root"; $env:DB_CLAVE="su_clave"
export DB_USUARIO=root DB_CLAVE=su_clave
mvn spring-boot:run
```

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

| Método y ruta | Descripción | Acceso |
|---|---|---|
| `POST /api/sesion` | Inicia sesión (`{"correo","clave"}`) | Público |
| `GET /api/sesion` · `DELETE /api/sesion` | Usuario actual · cerrar sesión | Con sesión |
| `GET /api/productos?texto=` · `GET /api/productos/{id}` | Catálogo | Público |
| `POST /api/productos` · `PUT /api/productos/{id}` | Crear (201 + Location) · actualizar | ADMINISTRADOR, VENDEDOR |
| `DELETE /api/productos/{id}` | Eliminar (204) | ADMINISTRADOR |
| `GET /api/pedidos` · `GET /api/pedidos/{id}` | Pedidos (el cliente solo ve los suyos) | Con sesión |
| `POST /api/pedidos` | Registrar (`{"idCliente","items":[{"idProducto","cantidad"}]}`) | Con sesión |
| `PATCH /api/pedidos/{id}/estado` | Cambiar estado (`{"estado":"PAGADO"}`) | ADMINISTRADOR, VENDEDOR |

Ejemplo con curl:

```bash
curl -c cookies.txt -H "Content-Type: application/json" \
     -d '{"correo":"laura.vendedora@gestionpedidos.local","clave":"Vendedor2026*"}' http://localhost:8080/api/sesion
curl -b cookies.txt -H "Content-Type: application/json" \
     -d '{"nombreProducto":"Té verde x 20","precio":12500,"stock":14}' http://localhost:8080/api/productos
```

## Pruebas y estándar de codificación

```bash
mvn test                                   # 35 pruebas JUnit 5 (servicios, vistas y API)
java herramientas/VerificadorEstandar.java # verifica las reglas de docs/estandares-codificacion.md
```

## Control de versiones

Commits con el formato *Conventional Commits* (`feat`, `fix`, `test`, `docs`, `chore`) y etiqueta `v1.0.0`.
Ver `git log --oneline`.
