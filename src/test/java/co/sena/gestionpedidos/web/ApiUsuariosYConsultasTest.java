package co.sena.gestionpedidos.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.soporte.ClienteHttp;

/**
 * Pruebas de los servicios web agregados en la versión 1.2.0: usuarios, registro de clientes,
 * resumen, filtros de pedidos y cancelación por el cliente.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("API REST v1.2.0: usuarios, clientes, resumen y filtros")
class ApiUsuariosYConsultasTest {

    private static ClienteHttp conSesion(String correo, String clave) {
        ClienteHttp cliente = new ClienteHttp();
        assertEquals(200, cliente.json("POST", "/api/sesion",
                "{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}").statusCode());
        return cliente;
    }

    @Test
    @DisplayName("GET /api/usuarios: el administrador obtiene la lista sin hash de contraseñas")
    void listarUsuarios() {
        HttpResponse<String> respuesta = conSesion("admin@gestionpedidos.local", "Admin2026*").get("/api/usuarios");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"documento\":\"1032456789\""));
        assertFalse(respuesta.body().contains("pbkdf2"));
    }

    @Test
    @DisplayName("GET /api/usuarios: el vendedor recibe 403")
    void vendedorSinAccesoAUsuarios() {
        assertEquals(403, conSesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*")
                .get("/api/usuarios").statusCode());
    }

    @Test
    @DisplayName("POST, PUT y DELETE /api/usuarios con validación del rol")
    void crudUsuarios() {
        ClienteHttp admin = conSesion("admin@gestionpedidos.local", "Admin2026*");
        HttpResponse<String> creado = admin.json("POST", "/api/usuarios", "{\"nombre\":\"Jorge Lara\","
                + "\"documento\":\"1077665544\",\"correo\":\"jorge.lara@correo.com\",\"telefono\":\"3014445566\","
                + "\"clave\":\"Jorge2026\",\"rol\":\"vendedor\"}");
        assertEquals(201, creado.statusCode());
        assertTrue(creado.body().contains("\"rol\":\"VENDEDOR\""));
        String ubicacion = creado.headers().firstValue("Location").orElseThrow();
        HttpResponse<String> actualizado = admin.json("PUT", ubicacion, "{\"nombre\":\"Jorge Iván Lara\","
                + "\"documento\":\"1077665544\",\"correo\":\"jorge.lara@correo.com\",\"telefono\":\"3014445599\","
                + "\"rol\":\"VENDEDOR\"}");
        assertEquals(200, actualizado.statusCode());
        assertTrue(actualizado.body().contains("Jorge Iván Lara"));
        HttpResponse<String> rolInvalido = admin.json("PUT", ubicacion, "{\"nombre\":\"Jorge Lara\","
                + "\"documento\":\"1077665544\",\"correo\":\"jorge.lara@correo.com\",\"telefono\":\"3014445599\","
                + "\"rol\":\"GERENTE\"}");
        assertEquals(422, rolInvalido.statusCode());
        assertEquals(204, admin.json("DELETE", ubicacion, "").statusCode());
        assertEquals(404, admin.get(ubicacion).statusCode());
    }

    @Test
    @DisplayName("POST /api/clientes: registro público con rol CLIENTE y validación de datos")
    void registroPublico() {
        ClienteHttp visitante = new ClienteHttp();
        HttpResponse<String> creado = visitante.json("POST", "/api/clientes", "{\"nombre\":\"Paula Ríos\","
                + "\"documento\":\"1066554433\",\"correo\":\"paula.rios@correo.com\",\"telefono\":\"3112223344\","
                + "\"clave\":\"Paula2026\",\"rol\":\"ADMINISTRADOR\"}");
        assertEquals(201, creado.statusCode());
        assertTrue(creado.body().contains("\"rol\":\"CLIENTE\""));
        HttpResponse<String> invalido = visitante.json("POST", "/api/clientes", "{\"nombre\":\"Paula2\","
                + "\"documento\":\"12\",\"correo\":\"x@y.com\",\"telefono\":\"3112223344\",\"clave\":\"Paula2026\"}");
        assertEquals(422, invalido.statusCode());
    }

    @Test
    @DisplayName("GET /api/resumen y filtros de GET /api/pedidos")
    void resumenYFiltros() {
        ClienteHttp vendedora = conSesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*");
        HttpResponse<String> resumen = vendedora.get("/api/resumen");
        assertEquals(200, resumen.statusCode());
        assertTrue(resumen.body().contains("\"pedidosPorEstado\""));
        assertTrue(vendedora.get("/api/pedidos?estado=PAGADO&desde=2026-11-10&hasta=2026-11-10").body()
                .contains("\"idPedido\":1"));
        assertEquals(422, vendedora.get("/api/pedidos?desde=2026-02-30").statusCode());
        assertEquals(400, vendedora.get("/api/productos/abc").statusCode());
    }

    @Test
    @DisplayName("PATCH estado: el cliente cancela su pedido pero no puede marcarlo como pagado")
    void clienteCancelaSuPedido() {
        ClienteHttp cliente = conSesion("carlos.cliente@gestionpedidos.local", "Cliente2026*");
        HttpResponse<String> creado = cliente.json("POST", "/api/pedidos",
                "{\"items\":[{\"idProducto\":4,\"cantidad\":1}]}");
        String ubicacion = creado.headers().firstValue("Location").orElseThrow();
        assertEquals(403, cliente.json("PATCH", ubicacion + "/estado", "{\"estado\":\"PAGADO\"}").statusCode());
        HttpResponse<String> cancelado = cliente.json("PATCH", ubicacion + "/estado", "{\"estado\":\"cancelado\"}");
        assertEquals(200, cancelado.statusCode());
        assertTrue(cancelado.body().contains("\"estado\":\"CANCELADO\""));
    }
}
