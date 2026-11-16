package co.sena.gestionpedidos.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.soporte.ClienteHttp;

/**
 * Pruebas de las validaciones a través de la aplicación web: registro de clientes, filtros de fechas,
 * caracteres especiales y números enviados desde los formularios (incluso saltando la validación del navegador).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("Validaciones en la aplicación web (RF02, RF04, RF05, RF06)")
class ValidacionesWebTest {

    private static Map<String, String> registro(String nombre, String documento, String correo, String telefono,
                                                String clave) {
        return Map.of("nombre", nombre, "documento", documento, "correo", correo, "telefono", telefono, "clave", clave);
    }

    private static ClienteHttp sesion(String correo, String clave) {
        ClienteHttp navegador = new ClienteHttp();
        navegador.iniciarSesion(correo, clave);
        return navegador;
    }

    @Test
    @DisplayName("RF02: un visitante crea su cuenta y queda con rol CLIENTE aunque envíe otro rol")
    void registroDeCliente() {
        ClienteHttp visitante = new ClienteHttp();
        Map<String, String> datos = new java.util.HashMap<>(registro("Sofía Martínez", "1098765432",
                "sofia.martinez@correo.com", "3157654321", "Sofia2026"));
        datos.put("rol", "ADMINISTRADOR");
        HttpResponse<String> respuesta = visitante.postFormulario("/registro", datos);
        assertEquals(302, respuesta.statusCode());
        assertTrue(respuesta.headers().firstValue("Location").orElse("").contains("/login"));
        visitante.iniciarSesion("sofia.martinez@correo.com", "Sofia2026");
        assertTrue(visitante.get("/").body().contains("(CLIENTE)"));
    }

    @Test
    @DisplayName("RF02: el registro rechaza documento con letras, teléfono corto y correo repetido")
    void registroInvalido() {
        ClienteHttp visitante = new ClienteHttp();
        assertTrue(visitante.postFormulario("/registro", registro("Pedro Pérez", "ABC123", "pedro@correo.com",
                "3001234567", "Pedro2026")).body().contains("entre 6 y 10 dígitos"));
        assertTrue(visitante.postFormulario("/registro", registro("Pedro Pérez", "1012345678", "pedro@correo.com",
                "300123", "Pedro2026")).body().contains("10 dígitos"));
        assertTrue(visitante.postFormulario("/registro", registro("Pedro Pérez", "1012345678",
                "admin@gestionpedidos.local", "3001234567", "Pedro2026")).body().contains("Ya existe un usuario"));
    }

    @Test
    @DisplayName("Texto con código HTML se rechaza y nunca se ejecuta en la página (prevención de XSS)")
    void caracteresEspecialesYXss() {
        ClienteHttp vendedora = sesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*");
        HttpResponse<String> respuesta = vendedora.postFormulario("/productos/guardar", Map.of(
                "nombreProducto", "<script>alert('x')</script>", "precio", "1000", "stock", "1"));
        assertTrue(respuesta.body().contains("caracteres no permitidos"));
        assertFalse(respuesta.body().contains("<script>alert"));
        assertTrue(respuesta.body().contains("&lt;script&gt;"));
    }

    @Test
    @DisplayName("La búsqueda trata % y comillas como texto (sin comodines ni inyección SQL)")
    void busquedaConCaracteresEspeciales() {
        ClienteHttp admin = sesion("admin@gestionpedidos.local", "Admin2026*");
        assertTrue(admin.get("/productos?texto=%25").body().contains("No se encontraron productos"));
        String inyeccion = "/productos?texto=%27%20OR%20%271%27%3D%271"; // ' OR '1'='1
        assertTrue(admin.get(inyeccion).body().contains("No se encontraron productos"));
    }

    @Test
    @DisplayName("Números: precio con tres decimales y cantidades con decimales se rechazan")
    void numerosInvalidos() {
        ClienteHttp vendedora = sesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*");
        assertTrue(vendedora.postFormulario("/productos/guardar", Map.of("nombreProducto", "Producto prueba",
                "precio", "1000.555", "stock", "1")).body().contains("dos decimales"));
        ClienteHttp cliente = sesion("carlos.cliente@gestionpedidos.local", "Cliente2026*");
        assertTrue(cliente.postFormulario("/pedidos/guardar", Map.of("cantidades[1]", "1.5")).body()
                .contains("números enteros"));
    }

    @Test
    @DisplayName("RF06: filtro por fechas válido, invertido, futuro y con formato incorrecto")
    void filtroDeFechas() {
        ClienteHttp admin = sesion("admin@gestionpedidos.local", "Admin2026*");
        String hoy = LocalDate.now().toString();
        String filtroValido = admin.get("/pedidos?desde=2026-11-10&hasta=2026-11-10").body();
        assertTrue(filtroValido.contains("Carlos Ramírez"));
        assertTrue(admin.get("/pedidos?desde=" + hoy + "&hasta=2026-01-01").body().contains("no puede ser posterior"));
        assertTrue(admin.get("/pedidos?hasta=" + LocalDate.now().plusDays(1)).body().contains("posteriores a hoy"));
        assertTrue(admin.get("/pedidos?desde=2026-02-30").body().contains("no es válida"));
    }

    @Test
    @DisplayName("RF06: filtro por estado y por nombre de cliente")
    void filtroPorEstadoYCliente() {
        ClienteHttp admin = sesion("admin@gestionpedidos.local", "Admin2026*");
        assertTrue(admin.get("/pedidos?estado=PAGADO&cliente=carlos").body().contains("estado-pagado"));
        assertTrue(admin.get("/pedidos?cliente=nadie").body().contains("No hay pedidos que cumplan los filtros"));
        assertTrue(admin.get("/pedidos?estado=PERDIDO").body().contains("no existe"));
    }

    @Test
    @DisplayName("RF04: filtro de productos con stock bajo")
    void filtroStockBajo() {
        ClienteHttp admin = sesion("admin@gestionpedidos.local", "Admin2026*");
        String pagina = admin.get("/productos?filtro=stock-bajo").body();
        assertTrue(pagina.contains("Queso campesino 500 g"));
        assertFalse(pagina.contains("Panela orgánica 1 kg"));
    }

    @Test
    @DisplayName("RF05: el detalle del pedido muestra subtotal, IVA del 19 % y total")
    void ivaEnDetalle() {
        ClienteHttp admin = sesion("admin@gestionpedidos.local", "Admin2026*");
        String detalle = admin.get("/pedidos/2").body();
        assertTrue(detalle.contains("$ 38.800"));
        assertTrue(detalle.contains("IVA (19 %)"));
        assertTrue(detalle.contains("$ 7.372"));
        assertTrue(detalle.contains("$ 46.172"));
    }
}
