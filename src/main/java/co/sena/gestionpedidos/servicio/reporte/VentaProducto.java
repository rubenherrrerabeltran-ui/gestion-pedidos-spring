package co.sena.gestionpedidos.servicio.reporte;

import java.math.BigDecimal;

/**
 * Unidades vendidas y valor vendido (sin IVA) de un producto en el periodo del reporte.
 *
 * @param idProducto     identificador del producto
 * @param nombreProducto nombre del producto al momento de la venta
 * @param unidades       unidades vendidas
 * @param total          valor vendido antes de IVA
 * @author Rubén Darío Herrera Beltrán
 */
public record VentaProducto(Integer idProducto, String nombreProducto, long unidades, BigDecimal total) {
}
