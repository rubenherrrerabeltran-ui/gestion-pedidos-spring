package co.sena.gestionpedidos.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.soporte.ClienteHttp;

/**
 * Pruebas de integración de la API REST ({@code @RestController} + Jackson).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("API REST /api (JSON)")
class ApiRestTest {

    private static ClienteHttp conSesion(String correo, String clave) {
        ClienteHttp cliente = new ClienteHttp();
        HttpResponse<String> respuesta = cliente.json("POST", "/api/sesion",
                "{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}");
        assertEquals(200, respuesta.statusCode());
        return cliente;
    }

    @Test
    @DisplayName("GET /api/productos es público y responde JSON")
    void catalogoPublico() {
        HttpResponse<String> respuesta = new ClienteHttp().get("/api/productos");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertTrue(respuesta.body().contains("\"nombreProducto\":\"Café de origen Huila 500 g\""));
    }

    @Test
    @DisplayName("POST /api/productos sin sesión responde 401")
    void crearSinSesion() {
        HttpResponse<String> respuesta = new ClienteHttp().json("POST", "/api/productos",
                "{\"nombreProducto\":\"X\",\"precio\":1,\"stock\":1}");
        assertEquals(401, respuesta.statusCode());
    }

    @Test
    @DisplayName("POST /api/sesion devuelve el usuario sin la contraseña")
    void sesionNoExponeClave() {
        ClienteHttp cliente = new ClienteHttp();
        HttpResponse<String> respuesta = cliente.json("POST", "/api/sesion",
                "{\"correo\":\"admin@gestionpedidos.local\",\"clave\":\"Admin2026*\"}");
        assertTrue(respuesta.body().contains("\"rol\":\"ADMINISTRADOR\""));
        assertTrue(!respuesta.body().contains("pbkdf2"));
    }

    @Test
    @DisplayName("CRUD por API: POST 201 + Location, PUT 200, DELETE 204, GET 404")
    void crudProductoPorApi() {
        ClienteHttp admin = conSesion("admin@gestionpedidos.local", "Admin2026*");
        HttpResponse<String> creado = admin.json("POST", "/api/productos",
                "{\"nombreProducto\":\"Té verde x 20\",\"precio\":12500,\"stock\":14}");
        assertEquals(201, creado.statusCode());
        String ubicacion = creado.headers().firstValue("Location").orElseThrow();
        assertTrue(ubicacion.startsWith("/api/productos/"));
        HttpResponse<String> actualizado = admin.json("PUT", ubicacion,
                "{\"nombreProducto\":\"Té verde x 25\",\"precio\":13000,\"stock\":10,\"activo\":true}");
        assertEquals(200, actualizado.statusCode());
        assertTrue(actualizado.body().contains("Té verde x 25"));
        assertEquals(204, admin.json("DELETE", ubicacion, "").statusCode());
        assertEquals(404, admin.get(ubicacion).statusCode());
    }

    @Test
    @DisplayName("Una regla de negocio incumplida responde 422 con ProblemDetail")
    void reglaDeNegocio422() {
        ClienteHttp admin = conSesion("admin@gestionpedidos.local", "Admin2026*");
        HttpResponse<String> respuesta = admin.json("POST", "/api/productos",
                "{\"nombreProducto\":\"\",\"precio\":1000,\"stock\":1}");
        assertEquals(422, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"detail\":\"El campo nombre del producto es obligatorio\""));
    }

    @Test
    @DisplayName("El CLIENTE no puede crear productos por la API (403)")
    void clienteNoCreaProductos() {
        ClienteHttp cliente = conSesion("carlos.cliente@gestionpedidos.local", "Cliente2026*");
        HttpResponse<String> respuesta = cliente.json("POST", "/api/productos",
                "{\"nombreProducto\":\"X\",\"precio\":1,\"stock\":1}");
        assertEquals(403, respuesta.statusCode());
    }

    @Test
    @DisplayName("Pedido por API: el cliente lo crea y el vendedor lo marca como PAGADO")
    void flujoDePedidoPorApi() {
        ClienteHttp cliente = conSesion("carlos.cliente@gestionpedidos.local", "Cliente2026*");
        HttpResponse<String> creado = cliente.json("POST", "/api/pedidos",
                "{\"items\":[{\"idProducto\":3,\"cantidad\":2}]}");
        assertEquals(201, creado.statusCode());
        assertTrue(creado.body().contains("\"estado\":\"PENDIENTE\""));
        assertTrue(creado.body().contains("\"total\":19600"));
        String ubicacion = creado.headers().firstValue("Location").orElseThrow();
        ClienteHttp vendedora = conSesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*");
        HttpResponse<String> pagado = vendedora.json("PATCH", ubicacion + "/estado", "{\"estado\":\"PAGADO\"}");
        assertEquals(200, pagado.statusCode());
        assertTrue(pagado.body().contains("\"estado\":\"PAGADO\""));
    }
}
