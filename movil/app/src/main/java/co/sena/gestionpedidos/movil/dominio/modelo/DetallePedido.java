package co.sena.gestionpedidos.movil.dominio.modelo;

import java.math.BigDecimal;

/**
 * Línea de un pedido: producto, cantidad y valores.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class DetallePedido {

    private final int idProducto;
    private final String nombreProducto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public DetallePedido(int idProducto, String nombreProducto, int cantidad, BigDecimal precioUnitario,
                         BigDecimal subtotal) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
