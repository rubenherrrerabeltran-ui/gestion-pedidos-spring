package co.sena.gestionpedidos.movil.datos;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import co.sena.gestionpedidos.movil.datos.remoto.ApiException;
import co.sena.gestionpedidos.movil.datos.remoto.ApiGestionPedidos;
import co.sena.gestionpedidos.movil.dominio.carrito.Carrito;
import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Producto;
import co.sena.gestionpedidos.movil.dominio.modelo.ReporteVentas;
import co.sena.gestionpedidos.movil.dominio.modelo.Resumen;
import co.sena.gestionpedidos.movil.dominio.modelo.Rol;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Pruebas unitarias del cliente de la API REST con un transporte falso: verifican el método,
 * la URL y el cuerpo de cada petición y la conversión de las respuestas reales del backend.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ApiGestionPedidosTest {

    private static final String PEDIDO_JSON = "{\"idPedido\":2,\"idCliente\":3,\"nombreCliente\":\"Carlos Ramírez\","
            + "\"fechaPedido\":\"2026-11-12T16:05:00\",\"estado\":\"PENDIENTE\",\"detalles\":["
            + "{\"idProducto\":3,\"nombreProducto\":\"Chocolate de mesa 250 g\",\"cantidad\":1,"
            + "\"precioUnitario\":9800.0,\"subtotal\":9800.0},{\"idProducto\":5,\"nombreProducto\":"
            + "\"Queso campesino 500 g\",\"cantidad\":2,\"precioUnitario\":14500.0,\"subtotal\":29000.0}],"
            + "\"subtotal\":38800.0,\"iva\":7372,\"total\":46172.0}";

    private TransporteFalso transporte;
    private ApiGestionPedidos api;

    /** Crea los objetos de prueba antes de cada caso. */
    @Before
    public void preparar() {
        transporte = new TransporteFalso();
        api = new ApiGestionPedidos(transporte, "http://10.0.2.2:8080/");
    }

    /** Verifica que inicia sesión con POST y convierte el usuario. */
    @Test
    public void iniciaSesionConPostYConvierteElUsuario() throws ApiException {
        transporte.responder(200, "{\"idUsuario\":2,\"nombre\":\"Laura Gómez\","
                + "\"correo\":\"laura.vendedora@gestionpedidos.local\",\"rol\":\"VENDEDOR\"}");
        Usuario usuario = api.iniciarSesion(" laura.vendedora@gestionpedidos.local ", "Vendedor2026*");
        assertEquals("POST", transporte.ultima().metodo);
        assertEquals("http://10.0.2.2:8080/api/sesion", transporte.ultima().url);
        assertEquals("{\"correo\":\"laura.vendedora@gestionpedidos.local\",\"clave\":\"Vendedor2026*\"}",
                transporte.ultima().cuerpo);
        assertEquals(Rol.VENDEDOR, usuario.getRol());
        assertEquals("Laura", usuario.getPrimerNombre());
    }

    /** Verifica que con credenciales incorrectas se muestra el mensaje del servidor. */
    @Test
    public void credencialesIncorrectasMuestranElMensajeDelServidor() {
        transporte.responder(422, "{\"detail\":\"Correo o contraseña incorrectos\",\"status\":422}");
        try {
            api.iniciarSesion("x@y.co", "mala");
            fail("Debía lanzar ApiException");
        } catch (ApiException e) {
            assertEquals(422, e.getEstado());
            assertEquals("Correo o contraseña incorrectos", e.getMessage());
        }
    }

    /** Verifica que sin servidor informa que no hay conexión. */
    @Test
    public void sinServidorInformaQueNoHayConexion() {
        transporte.sinConexion = true;
        try {
            api.listarProductos(null);
            fail("Debía lanzar ApiException");
        } catch (ApiException e) {
            assertEquals(ApiException.SIN_CONEXION, e.getEstado());
            assertTrue(e.getMessage().contains("http://10.0.2.2:8080"));
        }
    }

    /** Verifica que la sesión expirada se detecta con el código 401. */
    @Test
    public void sesionExpiradaSeDetectaConElCodigo401() {
        transporte.responder(401, "");
        try {
            api.obtenerResumen();
            fail("Debía lanzar ApiException");
        } catch (ApiException e) {
            assertTrue(e.esSesionExpirada());
        }
    }

    /** Verifica que busca productos codificando el texto. */
    @Test
    public void buscaProductosCodificandoElTexto() throws ApiException {
        transporte.responder(200, "[{\"nombreProducto\":\"Café de origen Huila 500 g\",\"precio\":28000.0,"
                + "\"stock\":40,\"activo\":true,\"idProducto\":1}]");
        List<Producto> productos = api.listarProductos("café huila");
        assertEquals("http://10.0.2.2:8080/api/productos?texto=caf%C3%A9+huila", transporte.ultima().url);
        assertEquals(1, productos.size());
        assertEquals(0, new BigDecimal("28000").compareTo(productos.get(0).getPrecio()));
    }

    /** Verifica que lista pedidos filtrados y convierte detalles y totales. */
    @Test
    public void listaPedidosFiltradosYConvierteDetallesYTotales() throws ApiException {
        transporte.responder(200, "[" + PEDIDO_JSON + "]");
        List<Pedido> pedidos = api.listarPedidos(EstadoPedido.PENDIENTE);
        assertEquals("http://10.0.2.2:8080/api/pedidos?estado=PENDIENTE", transporte.ultima().url);
        Pedido p = pedidos.get(0);
        assertEquals(EstadoPedido.PENDIENTE, p.getEstado());
        assertEquals(2, p.getDetalles().size());
        assertEquals(3, p.getUnidades());
        assertEquals(0, new BigDecimal("46172").compareTo(p.getTotal()));
    }

    /** Verifica que crea el pedido con los productos del carrito. */
    @Test
    public void creaElPedidoConLosProductosDelCarrito() throws ApiException {
        Carrito carrito = new Carrito();
        Producto cafe = new Producto(1, "Café", new BigDecimal("28000"), 40, true);
        carrito.agregar(cafe);
        carrito.agregar(cafe);
        transporte.responder(201, PEDIDO_JSON);
        api.crearPedido(3, carrito);
        assertEquals("POST", transporte.ultima().metodo);
        assertEquals("{\"idCliente\":3,\"items\":[{\"idProducto\":1,\"cantidad\":2}]}", transporte.ultima().cuerpo);
    }

    /** Verifica que cambia el estado con PATCH. */
    @Test
    public void cambiaElEstadoConPatch() throws ApiException {
        transporte.responder(200, PEDIDO_JSON.replace("PENDIENTE", "PAGADO"));
        Pedido p = api.cambiarEstado(2, EstadoPedido.PAGADO);
        assertEquals("PATCH", transporte.ultima().metodo);
        assertEquals("http://10.0.2.2:8080/api/pedidos/2/estado", transporte.ultima().url);
        assertEquals("{\"estado\":\"PAGADO\"}", transporte.ultima().cuerpo);
        assertEquals(EstadoPedido.PAGADO, p.getEstado());
    }

    /** Verifica que convierte el resumen y el reporte de ventas. */
    @Test
    public void convierteElResumenYElReporteDeVentas() throws ApiException {
        transporte.responder(200, "{\"totalProductos\":5,\"productosStockBajo\":1,\"totalUsuarios\":3,"
                + "\"pedidosPorEstado\":{\"PENDIENTE\":1,\"PAGADO\":1,\"ENVIADO\":0,\"CANCELADO\":0}}");
        Resumen resumen = api.obtenerResumen();
        assertEquals(5, resumen.getTotalProductos());
        assertEquals(1, resumen.pedidosEn(EstadoPedido.PENDIENTE));

        transporte.responder(200, "{\"desde\":\"2026-11-01\",\"hasta\":\"2026-11-16\",\"pedidosEnPeriodo\":2,"
                + "\"pedidosVendidos\":1,\"subtotal\":75500.00,\"iva\":14345,\"total\":89845.00,"
                + "\"ticketPromedio\":89845,\"pedidosPorEstado\":{\"PENDIENTE\":1,\"PAGADO\":1,\"ENVIADO\":0,"
                + "\"CANCELADO\":0},\"productosMasVendidos\":[{\"idProducto\":2,\"nombreProducto\":"
                + "\"Panela orgánica 1 kg\",\"unidades\":3,\"total\":19500.00}],\"ventasPorCliente\":[]}");
        ReporteVentas reporte = api.obtenerReporte("2026-11-01", "2026-11-16");
        assertEquals("http://10.0.2.2:8080/api/reportes/ventas?desde=2026-11-01&hasta=2026-11-16",
                transporte.ultima().url);
        assertEquals(0, new BigDecimal("89845").compareTo(reporte.getTotal()));
        assertEquals(3, reporte.getProductosMasVendidos().get(0).getUnidades());
    }

    /** Verifica que cerrar sesión olvida la cookie aunque no haya red. */
    @Test
    public void cerrarSesionOlvidaLaCookieAunqueNoHayaRed() {
        transporte.sinConexion = true;
        api.cerrarSesion();
        assertEquals("DELETE", transporte.ultima().metodo);
        assertTrue(transporte.sesionLimpiada);
    }
}
