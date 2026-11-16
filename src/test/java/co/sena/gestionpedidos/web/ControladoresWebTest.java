package co.sena.gestionpedidos.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.soporte.ClienteHttp;

/**
 * Pruebas de integración de los controladores Spring MVC y las vistas Thymeleaf
 * sobre el servidor Tomcat embebido (peticiones HTTP reales).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("Controladores web (Spring MVC + Thymeleaf)")
class ControladoresWebTest {

    @Test
    @DisplayName("Sin sesión, el interceptor redirige a /login (302)")
    void sinSesionRedirigeALogin() {
        HttpResponse<String> respuesta = new ClienteHttp().get("/productos");
        assertEquals(302, respuesta.statusCode());
        assertTrue(respuesta.headers().firstValue("Location").orElse("").contains("/login"));
    }

    @Test
    @DisplayName("GET /login muestra el formulario renderizado por Thymeleaf")
    void muestraLogin() {
        HttpResponse<String> respuesta = new ClienteHttp().get("/login");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("name=\"correo\""));
        assertTrue(respuesta.body().contains("Spring Boot"));
    }

    @Test
    @DisplayName("Credenciales incorrectas vuelven a mostrar el login con el error")
    void loginIncorrecto() {
        HttpResponse<String> respuesta = new ClienteHttp().iniciarSesion("admin@gestionpedidos.local", "mala");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Correo o contraseña incorrectos"));
    }

    @Test
    @DisplayName("POST /login correcto redirige al tablero con los indicadores")
    void loginCorrectoMuestraTablero() {
        ClienteHttp navegador = new ClienteHttp();
        assertEquals(302, navegador.iniciarSesion("admin@gestionpedidos.local", "Admin2026*").statusCode());
        HttpResponse<String> inicio = navegador.get("/");
        assertEquals(200, inicio.statusCode());
        assertTrue(inicio.body().contains("Hola, Administrador del sistema"));
        assertTrue(inicio.body().contains("Últimos pedidos"));
    }

    @Test
    @DisplayName("El VENDEDOR registra un producto con el formulario (POST + redirección)")
    void vendedorCreaProducto() {
        ClienteHttp navegador = new ClienteHttp();
        navegador.iniciarSesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*");
        HttpResponse<String> guardar = navegador.postFormulario("/productos/guardar", Map.of(
                "nombreProducto", "Almojábanas x 6", "precio", "9000", "stock", "15", "activo", "true"));
        assertEquals(302, guardar.statusCode());
        HttpResponse<String> lista = navegador.get("/productos?texto=almoj");
        assertTrue(lista.body().contains("Almojábanas x 6"));
        assertTrue(lista.body().contains("registrado con id"));
    }

    @Test
    @DisplayName("Un precio no numérico vuelve al formulario con mensaje de error")
    void precioInvalido() {
        ClienteHttp navegador = new ClienteHttp();
        navegador.iniciarSesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*");
        HttpResponse<String> respuesta = navegador.postFormulario("/productos/guardar", Map.of(
                "nombreProducto", "Producto", "precio", "abc", "stock", "1"));
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("deben ser valores numéricos"));
    }

    @Test
    @DisplayName("El CLIENTE no puede entrar al módulo de usuarios (403)")
    void clienteSinPermisoEnUsuarios() {
        ClienteHttp navegador = new ClienteHttp();
        navegador.iniciarSesion("carlos.cliente@gestionpedidos.local", "Cliente2026*");
        HttpResponse<String> respuesta = navegador.get("/usuarios");
        assertEquals(403, respuesta.statusCode());
        assertTrue(respuesta.body().contains("no tiene permiso"));
    }

    @Test
    @DisplayName("El CLIENTE registra su pedido desde el formulario y ve el detalle")
    void clienteRegistraPedido() {
        ClienteHttp navegador = new ClienteHttp();
        navegador.iniciarSesion("carlos.cliente@gestionpedidos.local", "Cliente2026*");
        HttpResponse<String> guardar = navegador.postFormulario("/pedidos/guardar",
                Map.of("cantidades[2]", "2", "cantidades[4]", "1"));
        assertEquals(302, guardar.statusCode());
        String detalle = navegador.get(guardar.headers().firstValue("Location").orElseThrow()
                .replaceFirst("^https?://[^/]+", "")).body();
        assertTrue(detalle.contains("Panela orgánica 1 kg"));
        assertTrue(detalle.contains("$ 20.200"));
    }

    @Test
    @DisplayName("Un pedido inexistente muestra la página de error 404")
    void pedidoInexistente() {
        ClienteHttp navegador = new ClienteHttp();
        navegador.iniciarSesion("admin@gestionpedidos.local", "Admin2026*");
        HttpResponse<String> respuesta = navegador.get("/pedidos/99999");
        assertEquals(404, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Pedido con id 99999 no existe"));
    }
}
