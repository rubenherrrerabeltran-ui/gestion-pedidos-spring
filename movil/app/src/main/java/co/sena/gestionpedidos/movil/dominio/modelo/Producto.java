package co.sena.gestionpedidos.movil.dominio.modelo;

import java.math.BigDecimal;

/**
 * Producto del catálogo.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Producto {

    /** Igual que en el servidor: menos de 10 unidades es stock bajo. */
    public static final int UMBRAL_STOCK_BAJO = 10;

    private final int idProducto;
    private final String nombre;
    private final BigDecimal precio;
    private final int stock;
    private final boolean activo;

    public Producto(int idProducto, String nombre, BigDecimal precio, int stock, boolean activo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.activo = activo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public boolean isActivo() {
        return activo;
    }

    /** @return {@code true} si está activo y tiene menos unidades que el umbral */
    public boolean tieneStockBajo() {
        return activo && stock < UMBRAL_STOCK_BAJO;
    }

    /** @return {@code true} si se puede vender (activo y con existencias) */
    public boolean estaDisponible() {
        return activo && stock > 0;
    }
}
