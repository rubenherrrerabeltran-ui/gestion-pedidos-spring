package co.sena.gestionpedidos.movil.dominio.carrito;

import java.math.BigDecimal;

import co.sena.gestionpedidos.movil.dominio.modelo.Producto;

/**
 * Producto agregado al carrito con la cantidad elegida.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class ItemCarrito {

    private final Producto producto;
    private int cantidad;

    ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    /** @return precio unitario por cantidad */
    public BigDecimal getSubtotal() {
        return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}
