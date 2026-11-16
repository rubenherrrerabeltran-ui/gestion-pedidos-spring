package co.sena.gestionpedidos.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.soporte.ClienteHttp;

/**
 * Pruebas de integración del módulo de reportes con los módulos de seguridad, pedidos y vistas:
 * levantan la aplicación completa y hacen peticiones HTTP reales.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("Módulo de reportes integrado (web, CSV y API)")
class ReportesWebTest {

    private static ClienteHttp navegador(String correo, String clave) {
        ClienteHttp navegador = new ClienteHttp();
        navegador.iniciarSesion(correo, clave);
        return navegador;
    }

    @Test
    @DisplayName("El VENDEDOR ve el reporte de ventas con productos y clientes")
    void vendedorVeReporte() {
        HttpResponse<String> respuesta = navegador("laura.vendedora@gestionpedidos.local", "Vendedor2026*")
                .get("/reportes?desde=2026-11-01&hasta=2026-11-16");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Reporte de ventas"));
        assertTrue(respuesta.body().contains("Productos más vendidos"));
        assertTrue(respuesta.body().contains("Carlos Ramírez"));
    }

    @Test
    @DisplayName("Un rango de fechas invertido muestra el mensaje de error")
    void rangoInvertido() {
        HttpResponse<String> respuesta = navegador("admin@gestionpedidos.local", "Admin2026*")
                .get("/reportes?desde=2026-11-15&hasta=2026-11-01");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("no puede ser posterior"));
    }

    @Test
    @DisplayName("El CLIENTE no tiene acceso al módulo de reportes")
    void clienteSinAcceso() {
        HttpResponse<String> respuesta = navegador("carlos.cliente@gestionpedidos.local", "Cliente2026*")
                .get("/reportes");
        assertEquals(403, respuesta.statusCode());
    }

    @Test
    @DisplayName("GET /reportes/pedidos.csv descarga el archivo con encabezado")
    void descargaCsv() {
        HttpResponse<String> respuesta = navegador("admin@gestionpedidos.local", "Admin2026*")
                .get("/reportes/pedidos.csv?desde=2026-11-01&hasta=2026-11-16");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.headers().firstValue("Content-Type").orElse("").startsWith("text/csv"));
        assertTrue(respuesta.headers().firstValue("Content-Disposition").orElse("").contains("pedidos.csv"));
        assertTrue(respuesta.body().contains("id_pedido;fecha;cliente;estado;subtotal;iva;total"));
    }

    @Test
    @DisplayName("GET /api/reportes/ventas devuelve el reporte en JSON")
    void apiReporte() {
        ClienteHttp cliente = new ClienteHttp();
        cliente.json("POST", "/api/sesion",
                "{\"correo\":\"laura.vendedora@gestionpedidos.local\",\"clave\":\"Vendedor2026*\"}");
        HttpResponse<String> respuesta = cliente.get("/api/reportes/ventas?desde=2026-11-01&hasta=2026-11-16");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"pedidosVendidos\""));
        assertTrue(respuesta.body().contains("\"productosMasVendidos\""));
    }
}
