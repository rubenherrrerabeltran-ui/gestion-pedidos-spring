package co.sena.gestionpedidos.web.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import co.sena.gestionpedidos.modelo.Pedido;

/**
 * Representación JSON de un pedido con su total calculado.
 *
 * @param idPedido      identificador
 * @param idCliente     cliente
 * @param nombreCliente nombre del cliente
 * @param fechaPedido   fecha y hora de registro
 * @param estado        estado actual
 * @param detalles      productos del pedido
 * @param total         suma de los subtotales
 * @author Rubén Darío Herrera Beltrán
 */
public record PedidoRespuesta(int idPedido, int idCliente, String nombreCliente, LocalDateTime fechaPedido,
                              String estado, List<Detalle> detalles, BigDecimal total) {

    /**
     * Línea del pedido.
     *
     * @param idProducto     producto
     * @param nombreProducto nombre
     * @param cantidad       unidades
     * @param precioUnitario precio al momento de la venta
     * @param subtotal       cantidad por precio
     */
    public record Detalle(int idProducto, String nombreProducto, int cantidad, BigDecimal precioUnitario,
                          BigDecimal subtotal) {
    }

    /**
     * @param pedido entidad leída de la base de datos
     * @return la respuesta para la API
     */
    public static PedidoRespuesta desde(Pedido pedido) {
        List<Detalle> detalles = pedido.getDetalles().stream()
                .map(d -> new Detalle(d.getIdProducto(), d.getNombreProducto(), d.getCantidad(),
                        d.getPrecioUnitario(), d.calcularSubtotal()))
                .toList();
        return new PedidoRespuesta(pedido.getIdPedido(), pedido.getIdUsuario(), pedido.getNombreCliente(),
                pedido.getFechaPedido(), pedido.getEstado().name(), detalles, pedido.calcularTotal());
    }
}
