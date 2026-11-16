package co.sena.gestionpedidos.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sena.gestionpedidos.modelo.DetallePedido;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.repositorio.PedidoRepositorio;
import co.sena.gestionpedidos.servicio.reporte.ReporteVentas;
import co.sena.gestionpedidos.servicio.reporte.VentaCliente;
import co.sena.gestionpedidos.servicio.reporte.VentaProducto;
import co.sena.gestionpedidos.util.Validador;

/**
 * Módulo de reportes de ventas (versión 1.3.0). Integra los módulos de pedidos, productos y
 * usuarios: a partir de los pedidos y sus detalles calcula las ventas del periodo, los productos
 * más vendidos y las compras por cliente, y exporta los pedidos a un archivo CSV.
 *
 * <p>Depende solo de la interfaz {@link PedidoRepositorio} y de un {@link Clock}, por eso se
 * prueba de forma unitaria con un repositorio en memoria y una fecha fija.</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Service
public class ReporteServicio {

    /** Días que cubre el reporte cuando no se indica la fecha inicial. */
    public static final int DIAS_POR_DEFECTO = 30;
    /** Cantidad de productos que muestra el ranking de más vendidos. */
    public static final int TOP_PRODUCTOS = 5;
    /** Estados que cuentan como venta efectiva. */
    private static final Set<EstadoPedido> ESTADOS_VENDIDOS = EnumSet.of(EstadoPedido.PAGADO, EstadoPedido.ENVIADO);
    private static final String SEPARADOR = ";";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PedidoRepositorio pedidoRepositorio;
    private final Clock reloj;

    /**
     * @param pedidoRepositorio acceso a los pedidos con sus detalles
     * @param reloj             reloj del sistema (en las pruebas, un reloj fijo)
     */
    public ReporteServicio(PedidoRepositorio pedidoRepositorio, Clock reloj) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.reloj = reloj;
    }

    /**
     * Genera el reporte de ventas del periodo.
     *
     * @param desde fecha inicial; si es nula se usan los últimos {@value #DIAS_POR_DEFECTO} días
     * @param hasta fecha final; si es nula se usa la fecha de hoy
     * @return reporte con totales, pedidos por estado, productos más vendidos y ventas por cliente
     */
    @Transactional(readOnly = true)
    public ReporteVentas generar(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDate fin = hasta == null ? hoy : hasta;
        LocalDate inicio = desde == null ? fin.minusDays(DIAS_POR_DEFECTO - 1L) : desde;
        Validador.validarRangoFechas(inicio, fin, hoy);

        List<Pedido> pedidos = pedidosDelPeriodo(inicio, fin);
        List<Pedido> vendidos = pedidos.stream().filter(p -> ESTADOS_VENDIDOS.contains(p.getEstado())).toList();

        BigDecimal subtotal = sumar(vendidos.stream().map(Pedido::calcularSubtotal).toList());
        BigDecimal iva = sumar(vendidos.stream().map(Pedido::calcularIva).toList());
        BigDecimal total = subtotal.add(iva);
        BigDecimal ticket = vendidos.isEmpty() ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(vendidos.size()), 0, RoundingMode.HALF_UP);

        return new ReporteVentas(inicio, fin, pedidos.size(), vendidos.size(), subtotal, iva, total, ticket,
                contarPorEstado(pedidos), productosMasVendidos(vendidos), ventasPorCliente(vendidos));
    }

    /**
     * Exporta los pedidos del periodo en formato CSV (separado por punto y coma, para abrirlo en Excel).
     * Los textos que empiezan por =, +, - o @ se neutralizan para evitar la inyección de fórmulas.
     *
     * @param desde fecha inicial (opcional)
     * @param hasta fecha final (opcional)
     * @return contenido del archivo CSV con encabezado
     */
    @Transactional(readOnly = true)
    public String exportarCsv(LocalDate desde, LocalDate hasta) {
        ReporteVentas periodo = generar(desde, hasta);
        StringBuilder csv = new StringBuilder("id_pedido;fecha;cliente;estado;subtotal;iva;total\n");
        for (Pedido pedido : pedidosDelPeriodo(periodo.desde(), periodo.hasta())) {
            csv.append(String.join(SEPARADOR,
                    String.valueOf(pedido.getIdPedido()),
                    pedido.getFechaPedido().format(FORMATO_FECHA),
                    textoCsv(pedido.getNombreCliente()),
                    pedido.getEstado().name(),
                    pedido.calcularSubtotal().toPlainString(),
                    pedido.calcularIva().toPlainString(),
                    pedido.calcularTotal().toPlainString())).append('\n');
        }
        return csv.toString();
    }

    private List<Pedido> pedidosDelPeriodo(LocalDate inicio, LocalDate fin) {
        return pedidoRepositorio.listar(null).stream()
                .filter(p -> {
                    LocalDate fecha = p.getFechaPedido().toLocalDate();
                    return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
                })
                .sorted(Comparator.comparing(Pedido::getFechaPedido).thenComparing(Pedido::getIdPedido))
                .toList();
    }

    private static Map<EstadoPedido, Long> contarPorEstado(List<Pedido> pedidos) {
        Map<EstadoPedido, Long> conteo = new EnumMap<>(EstadoPedido.class);
        for (EstadoPedido estado : EstadoPedido.values()) {
            conteo.put(estado, 0L);
        }
        pedidos.forEach(p -> conteo.merge(p.getEstado(), 1L, Long::sum));
        return conteo;
    }

    private static List<VentaProducto> productosMasVendidos(List<Pedido> vendidos) {
        Map<Integer, VentaProducto> porProducto = new LinkedHashMap<>();
        for (Pedido pedido : vendidos) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                porProducto.merge(detalle.getIdProducto(),
                        new VentaProducto(detalle.getIdProducto(), detalle.getNombreProducto(), detalle.getCantidad(),
                                detalle.calcularSubtotal()),
                        (a, b) -> new VentaProducto(a.idProducto(), a.nombreProducto(), a.unidades() + b.unidades(),
                                a.total().add(b.total())));
            }
        }
        return porProducto.values().stream()
                .sorted(Comparator.comparingLong(VentaProducto::unidades).reversed()
                        .thenComparing(VentaProducto::total, Comparator.reverseOrder()))
                .limit(TOP_PRODUCTOS)
                .toList();
    }

    private static List<VentaCliente> ventasPorCliente(List<Pedido> vendidos) {
        Map<Integer, VentaCliente> porCliente = new LinkedHashMap<>();
        for (Pedido pedido : vendidos) {
            porCliente.merge(pedido.getIdUsuario(),
                    new VentaCliente(pedido.getIdUsuario(), pedido.getNombreCliente(), 1, pedido.calcularTotal()),
                    (a, b) -> new VentaCliente(a.idCliente(), a.nombreCliente(), a.pedidos() + b.pedidos(),
                            a.total().add(b.total())));
        }
        List<VentaCliente> lista = new ArrayList<>(porCliente.values());
        lista.sort(Comparator.comparing(VentaCliente::total).reversed());
        return lista;
    }

    private static BigDecimal sumar(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Prepara un texto para una celda CSV: neutraliza fórmulas y encierra entre comillas si hace falta.
     */
    static String textoCsv(String texto) {
        String valor = texto == null ? "" : texto;
        if (!valor.isEmpty() && "=+-@".indexOf(valor.charAt(0)) >= 0) {
            valor = "'" + valor;
        }
        if (valor.contains(SEPARADOR) || valor.contains("\"") || valor.contains("\n")) {
            valor = "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
