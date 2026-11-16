package co.sena.gestionpedidos.movil.dominio.modelo;

import java.util.Collections;
import java.util.Map;

/**
 * Indicadores del tablero (GET /api/resumen).
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Resumen {

    private final int totalProductos;
    private final int productosStockBajo;
    private final int totalUsuarios;
    private final Map<EstadoPedido, Integer> pedidosPorEstado;

    public Resumen(int totalProductos, int productosStockBajo, int totalUsuarios,
                   Map<EstadoPedido, Integer> pedidosPorEstado) {
        this.totalProductos = totalProductos;
        this.productosStockBajo = productosStockBajo;
        this.totalUsuarios = totalUsuarios;
        this.pedidosPorEstado = Collections.unmodifiableMap(pedidosPorEstado);
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public int getProductosStockBajo() {
        return productosStockBajo;
    }

    public int getTotalUsuarios() {
        return totalUsuarios;
    }

    /**
     * @param estado estado consultado
     * @return número de pedidos en ese estado (0 si no hay)
     */
    public int pedidosEn(EstadoPedido estado) {
        Integer n = pedidosPorEstado.get(estado);
        return n == null ? 0 : n;
    }
}
