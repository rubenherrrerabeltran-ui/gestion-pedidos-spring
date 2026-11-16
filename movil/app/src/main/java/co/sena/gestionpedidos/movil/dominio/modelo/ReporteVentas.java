package co.sena.gestionpedidos.movil.dominio.modelo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Reporte de ventas de un periodo (GET /api/reportes/ventas), integrado desde el módulo web.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class ReporteVentas {

    /** Producto con sus unidades vendidas. */
    public static final class VentaProducto {
        private final String nombreProducto;
        private final int unidades;
        private final BigDecimal total;

        public VentaProducto(String nombreProducto, int unidades, BigDecimal total) {
            this.nombreProducto = nombreProducto;
            this.unidades = unidades;
            this.total = total;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public int getUnidades() {
            return unidades;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    private final String desde;
    private final String hasta;
    private final int pedidosEnPeriodo;
    private final int pedidosVendidos;
    private final BigDecimal subtotal;
    private final BigDecimal iva;
    private final BigDecimal total;
    private final BigDecimal ticketPromedio;
    private final Map<EstadoPedido, Integer> pedidosPorEstado;
    private final List<VentaProducto> productosMasVendidos;

    public ReporteVentas(String desde, String hasta, int pedidosEnPeriodo, int pedidosVendidos,
                         BigDecimal subtotal, BigDecimal iva, BigDecimal total, BigDecimal ticketPromedio,
                         Map<EstadoPedido, Integer> pedidosPorEstado, List<VentaProducto> productosMasVendidos) {
        this.desde = desde;
        this.hasta = hasta;
        this.pedidosEnPeriodo = pedidosEnPeriodo;
        this.pedidosVendidos = pedidosVendidos;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.ticketPromedio = ticketPromedio;
        this.pedidosPorEstado = Collections.unmodifiableMap(pedidosPorEstado);
        this.productosMasVendidos = Collections.unmodifiableList(productosMasVendidos);
    }

    public String getDesde() {
        return desde;
    }

    public String getHasta() {
        return hasta;
    }

    public int getPedidosEnPeriodo() {
        return pedidosEnPeriodo;
    }

    public int getPedidosVendidos() {
        return pedidosVendidos;
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

    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }

    /**
     * @param estado estado consultado
     * @return pedidos del periodo en ese estado
     */
    public int pedidosEn(EstadoPedido estado) {
        Integer n = pedidosPorEstado.get(estado);
        return n == null ? 0 : n;
    }

    public List<VentaProducto> getProductosMasVendidos() {
        return productosMasVendidos;
    }
}
