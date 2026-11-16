package co.sena.gestionpedidos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.soporte.AplicacionPrueba;

/**
 * Pruebas de las reglas de pedidos, incluida la reversión (rollback) de {@code @Transactional}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("PedidoServicio (@Transactional e inventario)")
class PedidoServicioTest {

    private static final int ID_CLIENTE = 3;

    private PedidoServicio pedidoServicio;
    private ProductoServicio productoServicio;

    @BeforeEach
    void obtenerBeans() {
        pedidoServicio = AplicacionPrueba.bean(PedidoServicio.class);
        productoServicio = AplicacionPrueba.bean(ProductoServicio.class);
    }

    private Producto crearProducto(String nombre, String precio, int stock) {
        return productoServicio.guardar(new Producto(nombre, new BigDecimal(precio), stock));
    }

    @Test
    @DisplayName("Registrar un pedido fija precios, calcula subtotal, IVA y total y descuenta el stock")
    void registrarDescuentaStock() {
        Producto producto = crearProducto("Galletas de avena", "3000", 10);
        Pedido pedido = pedidoServicio.registrar(ID_CLIENTE, Map.of(producto.getIdProducto(), 4));
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(0, new BigDecimal("12000").compareTo(pedido.calcularSubtotal()));
        assertEquals(0, new BigDecimal("2280").compareTo(pedido.calcularIva()));
        assertEquals(0, new BigDecimal("14280").compareTo(pedido.calcularTotal()));
        assertEquals(6, productoServicio.obtener(producto.getIdProducto()).getStock());
    }

    @Test
    @DisplayName("Si un producto no tiene stock, se revierte TODO el pedido (rollback)")
    void stockInsuficienteRevierte() {
        Producto conStock = crearProducto("Producto con stock", "1000", 5);
        Producto sinStock = crearProducto("Producto escaso", "1000", 1);
        int pedidosAntes = pedidoServicio.listar(ID_CLIENTE).size();
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        cantidades.put(conStock.getIdProducto(), 2);   // este sí alcanza y se descuenta primero...
        cantidades.put(sinStock.getIdProducto(), 3);   // ...pero este falla
        NegocioException error = assertThrows(NegocioException.class,
                () -> pedidoServicio.registrar(ID_CLIENTE, cantidades));
        assertTrue(error.getMessage().startsWith("Stock insuficiente"));
        assertEquals(5, productoServicio.obtener(conStock.getIdProducto()).getStock());
        assertEquals(pedidosAntes, pedidoServicio.listar(ID_CLIENTE).size());
    }

    @Test
    @DisplayName("Solo se registran pedidos a nombre de usuarios con rol CLIENTE")
    void soloClientes() {
        assertThrows(NegocioException.class, () -> pedidoServicio.registrar(2, Map.of(1, 1)));
    }

    @Test
    @DisplayName("Un pedido sin cantidades mayores que cero no se registra")
    void pedidoVacio() {
        assertThrows(NegocioException.class, () -> pedidoServicio.registrar(ID_CLIENTE, Map.of(1, 0)));
    }

    @Test
    @DisplayName("Cancelar devuelve las unidades al inventario")
    void cancelarDevuelveStock() {
        Producto producto = crearProducto("Mermelada de mora", "7000", 8);
        Pedido pedido = pedidoServicio.registrar(ID_CLIENTE, Map.of(producto.getIdProducto(), 5));
        pedidoServicio.cambiarEstado(pedido.getIdPedido(), EstadoPedido.CANCELADO);
        assertEquals(8, productoServicio.obtener(producto.getIdProducto()).getStock());
    }

    @Test
    @DisplayName("Respeta las transiciones: PENDIENTE → PAGADO → ENVIADO; ENVIADO es final")
    void transicionesDeEstado() {
        Producto producto = crearProducto("Café molido 250 g", "15000", 5);
        Pedido pedido = pedidoServicio.registrar(ID_CLIENTE, Map.of(producto.getIdProducto(), 1));
        int id = pedido.getIdPedido();
        assertThrows(NegocioException.class, () -> pedidoServicio.cambiarEstado(id, EstadoPedido.ENVIADO));
        assertEquals(EstadoPedido.PAGADO, pedidoServicio.cambiarEstado(id, EstadoPedido.PAGADO).getEstado());
        assertEquals(EstadoPedido.ENVIADO, pedidoServicio.cambiarEstado(id, EstadoPedido.ENVIADO).getEstado());
        assertThrows(NegocioException.class, () -> pedidoServicio.cambiarEstado(id, EstadoPedido.CANCELADO));
    }
}
