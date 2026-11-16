package co.sena.gestionpedidos.movil.datos.mapeo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import co.sena.gestionpedidos.movil.dominio.modelo.DetallePedido;
import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Producto;
import co.sena.gestionpedidos.movil.dominio.modelo.ReporteVentas;
import co.sena.gestionpedidos.movil.dominio.modelo.Resumen;
import co.sena.gestionpedidos.movil.dominio.modelo.Rol;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;

/**
 * Convierte los objetos JSON de la API (DTO) en objetos del dominio de la app.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Mapeador {

    private Mapeador() {
    }

    /** Convierte el JSON de la sesión en un {@link Usuario}. */
    public static Usuario usuario(Map<String, Object> json) {
        return new Usuario(entero(json, "idUsuario"), texto(json, "nombre"), texto(json, "correo"),
                Rol.valueOf(texto(json, "rol")));
    }

    /** Convierte el JSON de un producto en un {@link Producto}. */
    public static Producto producto(Map<String, Object> json) {
        Object activo = json.get("activo");
        return new Producto(entero(json, "idProducto"), texto(json, "nombreProducto"), decimal(json, "precio"),
                entero(json, "stock"), !Boolean.FALSE.equals(activo));
    }

    /** Convierte un arreglo JSON de productos en una lista. */
    public static List<Producto> productos(List<Object> json) {
        List<Producto> lista = new ArrayList<>();
        for (Object o : json) {
            lista.add(producto(objeto(o)));
        }
        return lista;
    }

    /** Convierte el JSON de un pedido (con su detalle) en un {@link Pedido}. */
    public static Pedido pedido(Map<String, Object> json) {
        List<DetallePedido> detalles = new ArrayList<>();
        Object lista = json.get("detalles");
        if (lista instanceof List) {
            for (Object o : (List<?>) lista) {
                Map<String, Object> d = objeto(o);
                detalles.add(new DetallePedido(entero(d, "idProducto"), texto(d, "nombreProducto"),
                        entero(d, "cantidad"), decimal(d, "precioUnitario"), decimal(d, "subtotal")));
            }
        }
        return new Pedido(entero(json, "idPedido"), entero(json, "idCliente"), texto(json, "nombreCliente"),
                texto(json, "fechaPedido"), EstadoPedido.valueOf(texto(json, "estado")), detalles,
                decimal(json, "subtotal"), decimal(json, "iva"), decimal(json, "total"));
    }

    /** Convierte un arreglo JSON de pedidos en una lista. */
    public static List<Pedido> pedidos(List<Object> json) {
        List<Pedido> lista = new ArrayList<>();
        for (Object o : json) {
            lista.add(pedido(objeto(o)));
        }
        return lista;
    }

    /** Convierte el JSON de GET /api/resumen en un {@link Resumen}. */
    public static Resumen resumen(Map<String, Object> json) {
        return new Resumen(entero(json, "totalProductos"), entero(json, "productosStockBajo"),
                entero(json, "totalUsuarios"), conteoPorEstado(json.get("pedidosPorEstado")));
    }

    /** Convierte el JSON de GET /api/reportes/ventas en un {@link ReporteVentas}. */
    public static ReporteVentas reporte(Map<String, Object> json) {
        List<ReporteVentas.VentaProducto> productos = new ArrayList<>();
        Object lista = json.get("productosMasVendidos");
        if (lista instanceof List) {
            for (Object o : (List<?>) lista) {
                Map<String, Object> p = objeto(o);
                productos.add(new ReporteVentas.VentaProducto(texto(p, "nombreProducto"), entero(p, "unidades"),
                        decimal(p, "total")));
            }
        }
        return new ReporteVentas(texto(json, "desde"), texto(json, "hasta"), entero(json, "pedidosEnPeriodo"),
                entero(json, "pedidosVendidos"), decimal(json, "subtotal"), decimal(json, "iva"),
                decimal(json, "total"), decimal(json, "ticketPromedio"),
                conteoPorEstado(json.get("pedidosPorEstado")), productos);
    }

    private static Map<EstadoPedido, Integer> conteoPorEstado(Object valor) {
        Map<EstadoPedido, Integer> conteo = new EnumMap<>(EstadoPedido.class);
        if (valor instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) valor).entrySet()) {
                conteo.put(EstadoPedido.valueOf(String.valueOf(e.getKey())), ((BigDecimal) e.getValue()).intValue());
            }
        }
        return conteo;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objeto(Object o) {
        if (!(o instanceof Map)) {
            throw new IllegalArgumentException("Se esperaba un objeto JSON");
        }
        return (Map<String, Object>) o;
    }

    private static String texto(Map<String, Object> json, String campo) {
        Object v = json.get(campo);
        return v == null ? null : v.toString();
    }

    private static int entero(Map<String, Object> json, String campo) {
        Object v = json.get(campo);
        return v instanceof BigDecimal ? ((BigDecimal) v).intValue() : 0;
    }

    private static BigDecimal decimal(Map<String, Object> json, String campo) {
        Object v = json.get(campo);
        return v instanceof BigDecimal ? (BigDecimal) v : BigDecimal.ZERO;
    }
}
