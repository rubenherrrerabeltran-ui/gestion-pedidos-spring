package co.sena.gestionpedidos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.servicio.reporte.ReporteVentas;
import co.sena.gestionpedidos.servicio.reporte.VentaCliente;
import co.sena.gestionpedidos.servicio.reporte.VentaProducto;
import co.sena.gestionpedidos.soporte.PedidoRepositorioEnMemoria;

/**
 * Pruebas unitarias del módulo de reportes: el servicio se crea con un repositorio en memoria
 * (doble de prueba) y un reloj fijo, sin base de datos ni contexto de Spring (patrón AAA:
 * preparar, actuar y verificar).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("ReporteServicio (pruebas unitarias con doble de prueba)")
class ReporteServicioTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);

    private PedidoRepositorioEnMemoria repositorio;
    private ReporteServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = new PedidoRepositorioEnMemoria();
        Clock relojFijo = Clock.fixed(HOY.atTime(10, 0).atZone(ZONA).toInstant(), ZONA);
        servicio = new ReporteServicio(repositorio, relojFijo);
        repositorio.agregar(1, "3:Carlos Ramírez", LocalDateTime.of(2026, 11, 10, 9, 30), EstadoPedido.PAGADO,
                1, "Café de origen", 2, "28000", 2, "Panela orgánica", 3, "6500");
        repositorio.agregar(2, "3:Carlos Ramírez", LocalDateTime.of(2026, 11, 12, 16, 5), EstadoPedido.PENDIENTE,
                3, "Chocolate de mesa", 1, "9800");
        repositorio.agregar(3, "7:Sofía Martínez", LocalDateTime.of(2026, 11, 14, 11, 0), EstadoPedido.ENVIADO,
                2, "Panela orgánica", 4, "6500");
        repositorio.agregar(4, "7:Sofía Martínez", LocalDateTime.of(2026, 11, 15, 8, 0), EstadoPedido.CANCELADO,
                1, "Café de origen", 10, "28000");
        repositorio.agregar(5, "3:Carlos Ramírez", LocalDateTime.of(2026, 9, 1, 8, 0), EstadoPedido.PAGADO,
                1, "Café de origen", 1, "28000");
    }

    @Test
    @DisplayName("Sin fechas, el reporte cubre los últimos 30 días hasta hoy")
    void periodoPorDefecto() {
        ReporteVentas reporte = servicio.generar(null, null);
        assertEquals(LocalDate.of(2026, 10, 18), reporte.desde());
        assertEquals(HOY, reporte.hasta());
        assertEquals(4, reporte.pedidosEnPeriodo(), "El pedido de septiembre queda por fuera del periodo");
    }

    @Test
    @DisplayName("Solo los pedidos PAGADO y ENVIADO cuentan como venta, con subtotal, IVA y total")
    void totalesDeVentas() {
        ReporteVentas reporte = servicio.generar(LocalDate.of(2026, 11, 1), HOY);
        // Pedido 1: 56 000 + 19 500 = 75 500; pedido 3: 26 000. Subtotal = 101 500
        assertEquals(2, reporte.pedidosVendidos());
        assertEquals(0, new BigDecimal("101500").compareTo(reporte.subtotal()));
        // IVA por pedido redondeado: 14 345 + 4 940 = 19 285
        assertEquals(0, new BigDecimal("19285").compareTo(reporte.iva()));
        assertEquals(0, new BigDecimal("120785").compareTo(reporte.total()));
        assertEquals(0, new BigDecimal("60393").compareTo(reporte.ticketPromedio()), "120 785 / 2 redondeado");
    }

    @Test
    @DisplayName("Cuenta los pedidos del periodo en cada estado, incluidos los estados sin pedidos")
    void pedidosPorEstado() {
        ReporteVentas reporte = servicio.generar(LocalDate.of(2026, 11, 1), HOY);
        assertEquals(1L, reporte.pedidosPorEstado().get(EstadoPedido.PENDIENTE));
        assertEquals(1L, reporte.pedidosPorEstado().get(EstadoPedido.PAGADO));
        assertEquals(1L, reporte.pedidosPorEstado().get(EstadoPedido.ENVIADO));
        assertEquals(1L, reporte.pedidosPorEstado().get(EstadoPedido.CANCELADO));
        assertEquals(4, reporte.pedidosPorEstado().size());
    }

    @Test
    @DisplayName("Ordena los productos más vendidos por unidades e ignora los pedidos cancelados")
    void productosMasVendidos() {
        ReporteVentas reporte = servicio.generar(LocalDate.of(2026, 11, 1), HOY);
        VentaProducto primero = reporte.productosMasVendidos().get(0);
        assertEquals("Panela orgánica", primero.nombreProducto());
        assertEquals(7, primero.unidades(), "3 unidades del pedido 1 + 4 del pedido 3");
        assertEquals(0, new BigDecimal("45500").compareTo(primero.total()));
        VentaProducto cafe = reporte.productosMasVendidos().get(1);
        assertEquals(2, cafe.unidades(), "Las 10 unidades del pedido cancelado no cuentan");
        assertEquals(2, reporte.productosMasVendidos().size(), "El chocolate solo está en un pedido pendiente");
    }

    @Test
    @DisplayName("Agrupa las compras por cliente y las ordena de mayor a menor valor")
    void ventasPorCliente() {
        ReporteVentas reporte = servicio.generar(LocalDate.of(2026, 11, 1), HOY);
        VentaCliente mayor = reporte.ventasPorCliente().get(0);
        assertEquals("Carlos Ramírez", mayor.nombreCliente());
        assertEquals(1, mayor.pedidos());
        assertEquals(0, new BigDecimal("89845").compareTo(mayor.total()));
        assertEquals("Sofía Martínez", reporte.ventasPorCliente().get(1).nombreCliente());
    }

    @Test
    @DisplayName("Un periodo sin ventas devuelve ceros y listas vacías")
    void periodoSinVentas() {
        ReporteVentas reporte = servicio.generar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        assertEquals(0, reporte.pedidosVendidos());
        assertEquals(0, BigDecimal.ZERO.compareTo(reporte.total()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reporte.ticketPromedio()));
        assertTrue(reporte.productosMasVendidos().isEmpty());
        assertTrue(reporte.ventasPorCliente().isEmpty());
    }

    @Test
    @DisplayName("Rechaza un rango invertido y las fechas futuras")
    void validaFechas() {
        LocalDate inicio = LocalDate.of(2026, 11, 15);
        LocalDate fin = LocalDate.of(2026, 11, 1);
        assertThrows(NegocioException.class, () -> servicio.generar(inicio, fin));
        assertThrows(NegocioException.class, () -> servicio.generar(null, HOY.plusDays(1)));
    }

    @Test
    @DisplayName("Exporta a CSV los pedidos del periodo en orden de fecha")
    void exportaCsv() {
        String[] lineas = servicio.exportarCsv(LocalDate.of(2026, 11, 1), HOY).split("\n");
        assertEquals("id_pedido;fecha;cliente;estado;subtotal;iva;total", lineas[0]);
        assertEquals(5, lineas.length, "Encabezado y cuatro pedidos");
        assertEquals("1;2026-11-10 09:30;Carlos Ramírez;PAGADO;75500;14345;89845", lineas[1]);
        assertTrue(lineas[4].startsWith("4;2026-11-15 08:00;Sofía Martínez;CANCELADO"));
    }

    @Test
    @DisplayName("El CSV neutraliza fórmulas y escapa el separador y las comillas")
    void textoCsvSeguro() {
        assertEquals("'=SUMA(A1)", ReporteServicio.textoCsv("=SUMA(A1)"));
        assertEquals("\"Pérez; Gómez\"", ReporteServicio.textoCsv("Pérez; Gómez"));
        assertEquals("\"Juan \"\"el Rápido\"\"\"", ReporteServicio.textoCsv("Juan \"el Rápido\""));
        assertEquals("", ReporteServicio.textoCsv(null));
    }
}
