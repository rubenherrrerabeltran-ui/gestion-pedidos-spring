package co.sena.gestionpedidos.movil.dominio;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import co.sena.gestionpedidos.movil.dominio.modelo.DetallePedido;
import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Rol;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.dominio.reglas.ReglasPedido;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Pruebas unitarias de las transiciones de estado y de los permisos por rol.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ReglasPedidoTest {

    private static final Usuario VENDEDOR = new Usuario(2, "Laura Gómez", "laura@x.co", Rol.VENDEDOR);
    private static final Usuario CARLOS = new Usuario(3, "Carlos Ramírez", "carlos@x.co", Rol.CLIENTE);
    private static final Usuario OTRO_CLIENTE = new Usuario(7, "Ana", "ana@x.co", Rol.CLIENTE);

    private static Pedido pedido(EstadoPedido estado) {
        return new Pedido(2, 3, "Carlos Ramírez", "2026-11-12T16:05:00", estado,
                Collections.<DetallePedido>emptyList(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /** Verifica que las transiciones son iguales a las del servidor. */
    @Test
    public void transicionesIgualesALasDelServidor() {
        assertTrue(EstadoPedido.PENDIENTE.puedeCambiarA(EstadoPedido.PAGADO));
        assertTrue(EstadoPedido.PAGADO.puedeCambiarA(EstadoPedido.ENVIADO));
        assertFalse(EstadoPedido.PENDIENTE.puedeCambiarA(EstadoPedido.ENVIADO));
        assertTrue(EstadoPedido.ENVIADO.siguientes().isEmpty());
        assertTrue(EstadoPedido.CANCELADO.siguientes().isEmpty());
    }

    /** Verifica que el vendedor ve las transiciones permitidas. */
    @Test
    public void elVendedorVeLasTransicionesPermitidas() {
        assertEquals(Arrays.asList(EstadoPedido.PAGADO, EstadoPedido.CANCELADO),
                ReglasPedido.accionesPermitidas(VENDEDOR, pedido(EstadoPedido.PENDIENTE)));
        assertEquals(Arrays.asList(EstadoPedido.ENVIADO, EstadoPedido.CANCELADO),
                ReglasPedido.accionesPermitidas(VENDEDOR, pedido(EstadoPedido.PAGADO)));
        assertTrue(ReglasPedido.accionesPermitidas(VENDEDOR, pedido(EstadoPedido.ENVIADO)).isEmpty());
    }

    /** Verifica que el cliente solo cancela sus pedidos pendientes. */
    @Test
    public void elClienteSoloCancelaSusPedidosPendientes() {
        assertEquals(Collections.singletonList(EstadoPedido.CANCELADO),
                ReglasPedido.accionesPermitidas(CARLOS, pedido(EstadoPedido.PENDIENTE)));
        assertTrue(ReglasPedido.accionesPermitidas(CARLOS, pedido(EstadoPedido.PAGADO)).isEmpty());
        assertTrue(ReglasPedido.accionesPermitidas(OTRO_CLIENTE, pedido(EstadoPedido.PENDIENTE)).isEmpty());
    }

    /** Verifica los permisos del menú por rol. */
    @Test
    public void permisosDeMenuPorRol() {
        assertTrue(Rol.VENDEDOR.gestionaPedidos());
        assertTrue(Rol.ADMINISTRADOR.gestionaPedidos());
        assertFalse(Rol.CLIENTE.gestionaPedidos());
        assertTrue(Rol.CLIENTE.puedeComprar());
        assertFalse(Rol.VENDEDOR.puedeComprar());
    }
}
