# ENDPOINT de la API REST — Sistema de Gestión de Pedidos

Rubén Darío Herrera Beltrán · SENA ADSO ficha 3186691 · Evidencia GA7-220501096-AA5-EV04

- URL base local: `http://localhost:8080`
- Formato: JSON (`Content-Type: application/json`). Errores en formato ProblemDetail (RFC 9457): `{"title", "status", "detail", "instance"}`.
- Autenticación: `POST /api/sesion` devuelve la cookie `JSESSIONID`, que se envía en las demás peticiones.
- Total: 19 ENDPOINT.

## Sesión

| # | Método | Ruta | Acceso | Descripción | Cuerpo JSON | Respuestas |
|---|---|---|---|---|---|---|
| 1 | `POST` | `/api/sesion` | Público | Inicia sesión y crea la cookie JSESSIONID | `{"correo": "laura.vendedora@gestionpedidos.local", "clave": "Vendedor2026*"}` | 200 · 400 · 422 |
| 2 | `GET` | `/api/sesion` | Con sesión | Devuelve el usuario autenticado | — | 200 · 401 |
| 3 | `DELETE` | `/api/sesion` | Con sesión | Cierra la sesión | — | 204 |

## Clientes

| # | Método | Ruta | Acceso | Descripción | Cuerpo JSON | Respuestas |
|---|---|---|---|---|---|---|
| 4 | `POST` | `/api/clientes` | Público | Registro de un cliente nuevo (el rol siempre es CLIENTE) | `{"nombre", "documento", "correo", "telefono", "clave"}` | 201 + Location · 422 |

## Productos

| # | Método | Ruta | Acceso | Descripción | Cuerpo JSON | Respuestas |
|---|---|---|---|---|---|---|
| 5 | `GET` | `/api/productos?texto=&filtro=` | Público | Lista el catálogo; filtro = todos, activos, inactivos o stock-bajo | — | 200 · 422 |
| 6 | `GET` | `/api/productos/{id}` | Público | Consulta un producto | — | 200 · 400 · 404 |
| 7 | `POST` | `/api/productos` | ADMINISTRADOR, VENDEDOR | Crea un producto | `{"nombreProducto": "Té verde x 20", "precio": 12500, "stock": 14, "activo": true}` | 201 + Location · 401 · 403 · 422 |
| 8 | `PUT` | `/api/productos/{id}` | ADMINISTRADOR, VENDEDOR | Actualiza un producto | Igual que POST | 200 · 404 · 422 |
| 9 | `DELETE` | `/api/productos/{id}` | ADMINISTRADOR | Elimina un producto sin pedidos | — | 204 · 403 · 404 · 422 |

## Pedidos

| # | Método | Ruta | Acceso | Descripción | Cuerpo JSON | Respuestas |
|---|---|---|---|---|---|---|
| 10 | `GET` | `/api/pedidos?cliente=&estado=&desde=&hasta=` | Con sesión | Lista pedidos con filtros (el cliente solo ve los suyos) | — | 200 · 401 · 422 |
| 11 | `GET` | `/api/pedidos/{id}` | Con sesión | Detalle con subtotal, IVA (19 %) y total | — | 200 · 403 · 404 |
| 12 | `POST` | `/api/pedidos` | Con sesión | Registra un pedido y descuenta el stock en una transacción | `{"idCliente": 3, "items": [{"idProducto": 1, "cantidad": 2}]}` | 201 + Location · 404 · 422 |
| 13 | `PATCH` | `/api/pedidos/{id}/estado` | ADMINISTRADOR, VENDEDOR; CLIENTE solo cancela | Cambia el estado del pedido | `{"estado": "PAGADO"}` | 200 · 403 · 404 · 422 |

## Usuarios

| # | Método | Ruta | Acceso | Descripción | Cuerpo JSON | Respuestas |
|---|---|---|---|---|---|---|
| 14 | `GET` | `/api/usuarios` | ADMINISTRADOR | Lista usuarios (sin contraseña) | — | 200 · 403 |
| 15 | `GET` | `/api/usuarios/{id}` | ADMINISTRADOR | Consulta un usuario | — | 200 · 404 |
| 16 | `POST` | `/api/usuarios` | ADMINISTRADOR | Crea un usuario con rol | `{"nombre", "documento", "correo", "telefono", "clave", "rol": "VENDEDOR"}` | 201 + Location · 422 |
| 17 | `PUT` | `/api/usuarios/{id}` | ADMINISTRADOR | Actualiza un usuario (clave opcional) | Igual que POST | 200 · 404 · 422 |
| 18 | `DELETE` | `/api/usuarios/{id}` | ADMINISTRADOR | Elimina un usuario sin pedidos (no a sí mismo) | — | 204 · 404 · 422 |

## Indicadores

| # | Método | Ruta | Acceso | Descripción | Cuerpo JSON | Respuestas |
|---|---|---|---|---|---|---|
| 19 | `GET` | `/api/resumen` | ADMINISTRADOR, VENDEDOR | Total de productos, stock bajo, usuarios y pedidos por estado | — | 200 · 403 |

## Códigos de estado

| Código | Significado |
|---|---|
| 200 OK | Consulta o actualización correcta |
| 201 Created | Recurso creado; la cabecera Location trae su URL |
| 204 No Content | Eliminación o cierre de sesión correctos |
| 400 Bad Request | JSON mal formado o parámetro con tipo inválido |
| 401 Unauthorized | Petición sin sesión a un recurso protegido |
| 403 Forbidden | El rol no tiene permiso o el pedido es de otro cliente |
| 404 Not Found | El recurso no existe |
| 422 Unprocessable Content | Validación o regla de negocio incumplida |

## Usuarios de prueba

| Correo | Contraseña | Rol |
|---|---|---|
| admin@gestionpedidos.local | Admin2026* | ADMINISTRADOR |
| laura.vendedora@gestionpedidos.local | Vendedor2026* | VENDEDOR |
| carlos.cliente@gestionpedidos.local | Cliente2026* | CLIENTE |

## Pruebas con Postman

Importe `postman/GestionPedidos-API.postman_collection.json` y el entorno `postman/GestionPedidos-Local.postman_environment.json`, seleccione el entorno y ejecute la colección con **Run collection** (54 peticiones, 110 pruebas automáticas).
