package co.sena.gestionpedidos.movil.dominio.modelo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Pedido con su encabezado, detalle y totales calculados por el servidor.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Pedido {

    private final int idPedido;
    private final int idCliente;
    private final String nombreCliente;
    private final String fechaPedido;
    private final EstadoPedido estado;
    private final List<DetallePedido> detalles;
    private final BigDecimal subtotal;
    private final BigDecimal iva;
    private final BigDecimal total;

    public Pedido(int idPedido, int idCliente, String nombreCliente, String fechaPedido, EstadoPedido estado,
                  List<DetallePedido> detalles, BigDecimal subtotal, BigDecimal iva, BigDecimal total) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.fechaPedido = fechaPedido;
        this.estado = estado;
        this.detalles = Collections.unmodifiableList(detalles);
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    /** @return fecha en formato ISO 8601 (AAAA-MM-DDTHH:MM:SS) */
    public String getFechaPedido() {
        return fechaPedido;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /** @return número total de unidades del pedido */
    public int getUnidades() {
        int unidades = 0;
        for (DetallePedido d : detalles) {
            unidades += d.getCantidad();
        }
        return unidades;
    }
}
