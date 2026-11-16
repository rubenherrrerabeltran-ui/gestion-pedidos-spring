package co.sena.gestionpedidos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del modelo de pedidos: cálculo de subtotal, IVA y total, y reglas de
 * cambio de estado. No usan base de datos ni Spring.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("Pedido y EstadoPedido (pruebas unitarias del modelo)")
class PedidoTest {

    private static DetallePedido detalle(int idProducto, int cantidad, String precio) {
        DetallePedido detalle = new DetallePedido(idProducto, cantidad);
        detalle.setPrecioUnitario(new BigDecimal(precio));
        return detalle;
    }

    @Test
    @DisplayName("Un pedido nuevo inicia en estado PENDIENTE y sin detalles")
    void pedidoNuevo() {
        Pedido pedido = new Pedido(3);
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(0, BigDecimal.ZERO.compareTo(pedido.calcularTotal()));
    }

    @Test
    @DisplayName("Subtotal = suma de cantidad por precio de cada detalle")
    void subtotal() {
        Pedido pedido = new Pedido(3);
        pedido.agregarDetalle(detalle(1, 2, "28000.00"));
        pedido.agregarDetalle(detalle(2, 3, "6500.00"));
        assertEquals(0, new BigDecimal("75500").compareTo(pedido.calcularSubtotal()));
    }

    @Test
    @DisplayName("El IVA del 19 % se redondea a pesos enteros y el total suma subtotal e IVA")
    void ivaRedondeado() {
        Pedido pedido = new Pedido(3);
        pedido.agregarDetalle(detalle(3, 1, "9850.00"));
        // 9 850 × 0,19 = 1 871,5 → 1 872
        assertEquals(0, new BigDecimal("1872").compareTo(pedido.calcularIva()));
        assertEquals(0, new BigDecimal("11722").compareTo(pedido.calcularTotal()));
    }

    @Test
    @DisplayName("Un detalle sin precio aporta cero al subtotal")
    void detalleSinPrecio() {
        assertEquals(0, BigDecimal.ZERO.compareTo(new DetallePedido(1, 5).calcularSubtotal()));
    }

    @Test
    @DisplayName("Transiciones permitidas: PENDIENTE → PAGADO → ENVIADO, y cancelar antes del envío")
    void transicionesPermitidas() {
        assertTrue(EstadoPedido.PENDIENTE.puedeCambiarA(EstadoPedido.PAGADO));
        assertTrue(EstadoPedido.PENDIENTE.puedeCambiarA(EstadoPedido.CANCELADO));
        assertTrue(EstadoPedido.PAGADO.puedeCambiarA(EstadoPedido.ENVIADO));
        assertTrue(EstadoPedido.PAGADO.puedeCambiarA(EstadoPedido.CANCELADO));
    }

    @Test
    @DisplayName("Transiciones prohibidas: volver atrás o cambiar un pedido enviado o cancelado")
    void transicionesProhibidas() {
        assertFalse(EstadoPedido.PAGADO.puedeCambiarA(EstadoPedido.PENDIENTE));
        assertFalse(EstadoPedido.PENDIENTE.puedeCambiarA(EstadoPedido.ENVIADO));
        assertFalse(EstadoPedido.ENVIADO.puedeCambiarA(EstadoPedido.CANCELADO));
        assertFalse(EstadoPedido.CANCELADO.puedeCambiarA(EstadoPedido.PAGADO));
    }

    @Test
    @DisplayName("Convierte el código guardado en la base de datos al estado, sin importar mayúsculas")
    void desdeCodigo() {
        assertEquals(EstadoPedido.ENVIADO, EstadoPedido.desdeCodigo("Enviado"));
    }
}
