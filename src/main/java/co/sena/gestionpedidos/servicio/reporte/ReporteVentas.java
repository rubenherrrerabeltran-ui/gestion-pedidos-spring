package co.sena.gestionpedidos.servicio.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import co.sena.gestionpedidos.modelo.EstadoPedido;

/**
 * Resultado del módulo de reportes: integra la información de pedidos, productos y clientes
 * de un periodo. Solo los pedidos PAGADO y ENVIADO cuentan como ventas.
 *
 * @param desde                 fecha inicial del periodo (incluida)
 * @param hasta                 fecha final del periodo (incluida)
 * @param pedidosEnPeriodo      pedidos registrados en el periodo, en cualquier estado
 * @param pedidosVendidos       pedidos pagados o enviados
 * @param subtotal              valor vendido antes de IVA
 * @param iva                   IVA del 19 % de las ventas
 * @param total                 valor vendido con IVA
 * @param ticketPromedio        total dividido entre los pedidos vendidos
 * @param pedidosPorEstado      cantidad de pedidos del periodo por estado
 * @param productosMasVendidos  productos ordenados por unidades vendidas
 * @param ventasPorCliente      clientes ordenados por valor comprado
 * @author Rubén Darío Herrera Beltrán
 */
public record ReporteVentas(LocalDate desde, LocalDate hasta, int pedidosEnPeriodo, int pedidosVendidos,
                            BigDecimal subtotal, BigDecimal iva, BigDecimal total, BigDecimal ticketPromedio,
                            Map<EstadoPedido, Long> pedidosPorEstado, List<VentaProducto> productosMasVendidos,
                            List<VentaCliente> ventasPorCliente) {
}
