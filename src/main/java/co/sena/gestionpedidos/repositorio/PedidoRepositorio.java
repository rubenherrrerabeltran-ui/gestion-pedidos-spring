package co.sena.gestionpedidos.repositorio;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;

/**
 * Contrato de acceso a datos de las tablas {@code pedido} y {@code detalle_pedido}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public interface PedidoRepositorio {

    /**
     * @param idUsuario si no es {@code null}, solo devuelve los pedidos de ese cliente
     * @return pedidos con sus detalles, del más reciente al más antiguo
     */
    List<Pedido> listar(Integer idUsuario);

    /**
     * @param idPedido identificador del pedido
     * @return el pedido con sus detalles, o vacío si no existe
     */
    Optional<Pedido> buscarPorId(int idPedido);

    /**
     * Inserta el encabezado del pedido y todos sus detalles.
     * Debe llamarse dentro de una transacción para que se guarde todo o nada.
     *
     * @param pedido pedido con cliente, estado y detalles con precio unitario
     * @return el pedido con los identificadores generados
     */
    Pedido insertar(Pedido pedido);

    /**
     * @param idPedido    pedido a modificar
     * @param nuevoEstado estado destino
     * @return {@code true} si se actualizó una fila
     */
    boolean actualizarEstado(int idPedido, EstadoPedido nuevoEstado);

    /**
     * @return cantidad de pedidos agrupados por estado (incluye los estados sin pedidos en cero)
     */
    Map<EstadoPedido, Long> contarPorEstado();
}
