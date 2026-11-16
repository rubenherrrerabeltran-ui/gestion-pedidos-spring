package co.sena.gestionpedidos.servicio.reporte;

import java.math.BigDecimal;

/**
 * Compras de un cliente en el periodo del reporte.
 *
 * @param idCliente     identificador del usuario con rol CLIENTE
 * @param nombreCliente nombre del cliente
 * @param pedidos       pedidos pagados o enviados
 * @param total         valor comprado con IVA incluido
 * @author Rubén Darío Herrera Beltrán
 */
public record VentaCliente(Integer idCliente, String nombreCliente, long pedidos, BigDecimal total) {
}
