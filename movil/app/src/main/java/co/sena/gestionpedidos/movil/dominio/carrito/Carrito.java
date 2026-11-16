package co.sena.gestionpedidos.movil.dominio.carrito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.sena.gestionpedidos.movil.dominio.modelo.Producto;

/**
 * Carrito de compras del cliente. Controla que no se pida más de lo que hay en stock y calcula
 * subtotal, IVA (19 %) y total con las mismas reglas de redondeo del servidor.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class Carrito {

    /** Tarifa general del IVA en Colombia. */
    public static final BigDecimal TASA_IVA = new BigDecimal("0.19");

    private final Map<Integer, ItemCarrito> items = new LinkedHashMap<>();

    /**
     * Agrega una unidad del producto.
     *
     * @param producto producto del catálogo
     * @return {@code true} si se agregó; {@code false} si no hay más stock disponible
     */
    public boolean agregar(Producto producto) {
        if (!producto.estaDisponible()) {
            return false;
        }
        ItemCarrito item = items.get(producto.getIdProducto());
        if (item == null) {
            items.put(producto.getIdProducto(), new ItemCarrito(producto, 1));
            return true;
        }
        if (item.getCantidad() >= producto.getStock()) {
            return false;
        }
        item.setCantidad(item.getCantidad() + 1);
        return true;
    }

    /**
     * Suma o resta unidades; si la cantidad llega a cero el producto sale del carrito.
     *
     * @param idProducto producto que se modifica
     * @param cambio     unidades que se suman (positivo) o restan (negativo)
     * @return {@code true} si el cambio se aplicó
     */
    public boolean cambiarCantidad(int idProducto, int cambio) {
        ItemCarrito item = items.get(idProducto);
        if (item == null) {
            return false;
        }
        int nueva = item.getCantidad() + cambio;
        if (nueva > item.getProducto().getStock()) {
            return false;
        }
        if (nueva <= 0) {
            items.remove(idProducto);
        } else {
            item.setCantidad(nueva);
        }
        return true;
    }

    /** Quita todos los productos. */
    public void vaciar() {
        items.clear();
    }

    /** @return copia de los ítems en el orden en que se agregaron */
    public List<ItemCarrito> getItems() {
        return new ArrayList<>(items.values());
    }

    /** @return {@code true} si el carrito no tiene productos */
    public boolean estaVacio() {
        return items.isEmpty();
    }

    /** @return número total de unidades */
    public int getUnidades() {
        int total = 0;
        for (ItemCarrito item : items.values()) {
            total += item.getCantidad();
        }
        return total;
    }

    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemCarrito item : items.values()) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        return subtotal;
    }

    /** @return IVA del 19 % redondeado al peso (HALF_UP) */
    public BigDecimal getIva() {
        return getSubtotal().multiply(TASA_IVA).setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getIva());
    }
}
