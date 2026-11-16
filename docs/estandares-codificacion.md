# Estándares de codificación — Módulo Spring Boot

Estándar definido para el proyecto **Sistema de Gestión de Pedidos** (SENA ADSO, ficha 3186691).
Se basa en las *Code Conventions for the Java Programming Language* (Oracle) y en la *Google Java Style Guide*,
adaptadas al español, y en las convenciones propias de Spring Boot.
Se aplica en todas las evidencias del proyecto (AA2-EV01, AA2-EV02 y AA3-EV01).

Las reglas marcadas con **[R#]** se comprueban automáticamente con:

```bash
java herramientas/VerificadorEstandar.java
```

## 1. Nombramiento

| Elemento | Convención | Ejemplos del proyecto |
|---|---|---|
| Paquetes **[R6]** | minúsculas, dominio invertido, por capa | `co.sena.gestionpedidos.servicio`, `co.sena.gestionpedidos.web.api` |
| Clases **[R7]** | PascalCase, sustantivo; el archivo se llama igual | `PedidoServicio`, `ProductoRepositorioJdbc` |
| Interfaces | PascalCase, nombre del contrato | `ProductoRepositorio`, `PedidoRepositorio` |
| Implementaciones | interfaz + tecnología | `ProductoRepositorioJdbc implements ProductoRepositorio` |
| Componentes Spring | sufijo según el estereotipo | `...Controlador` (`@Controller`), `...ApiControlador` (`@RestController`), `...Servicio` (`@Service`), `...RepositorioJdbc` (`@Repository`) |
| DTO de la API | `record` con sufijo `Solicitud` o `Respuesta` | `ProductoSolicitud`, `PedidoRespuesta` |
| Objetos de formulario | sufijo `Formulario` | `PedidoFormulario`, `UsuarioFormulario` |
| Enumeraciones | PascalCase; constantes en MAYÚSCULAS | `EstadoPedido.PENDIENTE`, `Rol.ADMINISTRADOR` |
| Métodos | camelCase, inician con verbo | `listar`, `obtener`, `guardar`, `registrar`, `cambiarEstado` |
| Variables y atributos | camelCase, descriptivos, sin abreviaturas | `cantidadPorProducto`, `llaveGenerada` |
| Constantes **[R10]** | `static final` en MAYÚSCULAS_CON_GUION_BAJO | `SQL_INSERTAR`, `UMBRAL_STOCK_BAJO` |
| Rutas web | minúsculas, recurso en plural, acción como subruta | `/productos`, `/productos/{id}/editar`, `/pedidos/{id}/estado` |
| Rutas de la API | prefijo `/api`, recurso en plural, verbo HTTP según la acción | `GET /api/productos`, `POST /api/pedidos`, `PATCH /api/pedidos/{id}/estado` |
| Plantillas Thymeleaf | minúsculas, carpeta por módulo | `templates/productos/lista.html`, `templates/pedidos/detalle.html` |
| Pruebas | `NombreClaseTest`; el método describe el comportamiento y lleva `@DisplayName` | `PedidoServicioTest.stockInsuficienteRevierte` |
| Tablas y columnas SQL | snake_case en minúscula | `detalle_pedido`, `precio_unitario` |

## 2. Formato

- **[R1]** Archivos en UTF-8 con fin de línea LF (`.editorconfig` y `.gitattributes`).
- **[R2]** Sangría de 4 espacios, sin tabuladores (2 espacios en XML, HTML y Markdown).
- **[R3]** Líneas de máximo 120 caracteres.
- **[R4]** Sin espacios al final de las líneas.
- **[R5]** Importaciones explícitas (sin `*`), agrupadas: `java.*`, `jakarta.*`, `org.*`, proyecto.
- Llaves estilo K&R; un archivo por tipo público.
- Orden de miembros: constantes, atributos, constructor, métodos públicos, métodos privados.

## 3. Comentarios y documentación

- **[R8]** Javadoc con descripción y `@author` en cada clase, interfaz, `record` y anotación pública.
- **[R9]** Javadoc con `@param`, `@return` y `@throws` en los métodos públicos
  (se exceptúan los métodos `@Override`, documentados en la interfaz, y los getters/setters).
- Comentarios de línea (`//`) solo para explicar el *porqué* de una decisión, no el *qué*.
- En Thymeleaf se usan comentarios de bloque de parser `<!--/* ... */-->`, que no llegan al navegador.
- En SQL, encabezado con evidencia, motor, origen y autor.

## 4. Arquitectura con Spring Boot

- Capas: `web` (controladores y API) → `servicio` (reglas de negocio) → `repositorio` (acceso a datos) → MySQL.
- Inyección de dependencias **por constructor** (atributos `final`); no se usa `@Autowired` en atributos.
- Los controladores no contienen SQL ni reglas de negocio; solo reciben la petición, llaman al servicio y
  eligen la vista o la respuesta JSON.
- Las operaciones que modifican varias tablas se anotan con `@Transactional` en el servicio.
- Las reglas de negocio se señalan con `NegocioException`; los manejadores `@ControllerAdvice` y
  `@RestControllerAdvice` las convierten en la página de error o en un `ProblemDetail` (RFC 9457).

## 5. Acceso a datos

- `JdbcClient` de Spring con parámetros con nombre (`:id`); nunca se concatenan datos del usuario en el SQL.
- Sentencias SQL como constantes (`SQL_...`) con *text blocks* `"""`.
- El esquema y los datos se cargan con `spring.sql.init` (`db/schema.sql`, `db/data.sql`).
- Credenciales fuera del código: variables de entorno `DB_URL`, `DB_USUARIO`, `DB_CLAVE`.
- Contraseñas con hash PBKDF2 y sal aleatoria (`ClaveUtil`).

## 6. Web y API

- GET solo consulta; POST/PUT/PATCH/DELETE modifican. En la web, POST seguido de redirección (Post/Redirect/Get).
- Thymeleaf escapa el texto con `th:text` (prevención de XSS); no se usa `th:utext` con datos del usuario.
- La API responde JSON, usa los códigos HTTP correctos (200, 201 + `Location`, 204, 401, 403, 404, 422)
  y nunca devuelve el hash de la contraseña.
- El acceso se controla con `SesionInterceptor` y las anotaciones `@Publico` y `@RequiereRol`.

## 7. Control de versiones

- Repositorio Git con rama principal `main` y etiqueta por versión entregada (`v1.0.0`).
- Un commit por unidad lógica de trabajo con el formato *Conventional Commits*:
  `tipo(alcance): descripción en imperativo`. Tipos: `feat`, `fix`, `test`, `docs`, `chore`, `refactor`, `style`.
- No se suben carpetas generadas (`target/`) ni credenciales (`.gitignore`).
