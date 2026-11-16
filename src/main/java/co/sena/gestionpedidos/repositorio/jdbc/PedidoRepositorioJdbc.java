package co.sena.gestionpedidos.repositorio.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import co.sena.gestionpedidos.modelo.DetallePedido;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.repositorio.PedidoRepositorio;

/**
 * Implementación JDBC del repositorio de pedidos usando {@link JdbcClient} de Spring.
 *
 * <p>Los pedidos y sus detalles se leen con una sola consulta (JOIN) y se agrupan en memoria,
 * evitando una consulta adicional por cada pedido.</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Repository
public class PedidoRepositorioJdbc implements PedidoRepositorio {

    private static final String SQL_CONSULTA_BASE = """
            SELECT p.id_pedido, p.id_usuario, u.nombre AS nombre_cliente, p.fecha_pedido, p.estado,
                   d.id_detalle, d.id_producto, pr.nombre_producto, d.cantidad, d.precio_unitario
            FROM pedido p
            JOIN usuario u ON u.id_usuario = p.id_usuario
            LEFT JOIN detalle_pedido d ON d.id_pedido = p.id_pedido
            LEFT JOIN producto pr ON pr.id_producto = d.id_producto
            """;
    private static final String SQL_INSERTAR_PEDIDO =
            "INSERT INTO pedido (id_usuario, estado) VALUES (:idUsuario, :estado)";
    private static final String SQL_INSERTAR_DETALLE = """
            INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario)
            VALUES (:idPedido, :idProducto, :cantidad, :precio)""";
    private static final String SQL_ACTUALIZAR_ESTADO =
            "UPDATE pedido SET estado = :estado WHERE id_pedido = :id";
    private static final String SQL_CONTAR_POR_ESTADO =
            "SELECT estado, COUNT(*) AS total FROM pedido GROUP BY estado";

    private final JdbcClient jdbcClient;

    /**
     * @param jdbcClient cliente JDBC de Spring
     */
    public PedidoRepositorioJdbc(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Pedido> listar(Integer idUsuario) {
        String filtro = idUsuario == null ? "" : "WHERE p.id_usuario = :idUsuario ";
        JdbcClient.StatementSpec sentencia = jdbcClient
                .sql(SQL_CONSULTA_BASE + filtro + "ORDER BY p.fecha_pedido DESC, p.id_pedido DESC, d.id_detalle");
        if (idUsuario != null) {
            sentencia.param("idUsuario", idUsuario);
        }
        return agrupar(sentencia);
    }

    @Override
    public Optional<Pedido> buscarPorId(int idPedido) {
        JdbcClient.StatementSpec sentencia = jdbcClient
                .sql(SQL_CONSULTA_BASE + "WHERE p.id_pedido = :id ORDER BY d.id_detalle")
                .param("id", idPedido);
        return agrupar(sentencia).stream().findFirst();
    }

    @Override
    public Pedido insertar(Pedido pedido) {
        KeyHolder llaveGenerada = new GeneratedKeyHolder();
        jdbcClient.sql(SQL_INSERTAR_PEDIDO)
                .param("idUsuario", pedido.getIdUsuario())
                .param("estado", pedido.getEstado().getCodigoBd())
                .update(llaveGenerada, "id_pedido");
        pedido.setIdPedido(llaveGenerada.getKey().intValue());
        for (DetallePedido detalle : pedido.getDetalles()) {
            detalle.setIdPedido(pedido.getIdPedido());
            KeyHolder llaveDetalle = new GeneratedKeyHolder();
            jdbcClient.sql(SQL_INSERTAR_DETALLE)
                    .param("idPedido", detalle.getIdPedido())
                    .param("idProducto", detalle.getIdProducto())
                    .param("cantidad", detalle.getCantidad())
                    .param("precio", detalle.getPrecioUnitario())
                    .update(llaveDetalle, "id_detalle");
            detalle.setIdDetalle(llaveDetalle.getKey().intValue());
        }
        return pedido;
    }

    @Override
    public boolean actualizarEstado(int idPedido, EstadoPedido nuevoEstado) {
        return jdbcClient.sql(SQL_ACTUALIZAR_ESTADO)
                .param("estado", nuevoEstado.getCodigoBd())
                .param("id", idPedido)
                .update() == 1;
    }

    @Override
    public Map<EstadoPedido, Long> contarPorEstado() {
        Map<EstadoPedido, Long> conteo = new EnumMap<>(EstadoPedido.class);
        for (EstadoPedido estado : EstadoPedido.values()) {
            conteo.put(estado, 0L);
        }
        jdbcClient.sql(SQL_CONTAR_POR_ESTADO).query((ResultSet fila) -> {
            conteo.put(EstadoPedido.desdeCodigo(fila.getString("estado")), fila.getLong("total"));
        });
        return conteo;
    }

    /**
     * Recorre el resultado del JOIN y arma cada pedido con su lista de detalles.
     * Se usa un {@link LinkedHashMap} para conservar el orden de la consulta.
     */
    private List<Pedido> agrupar(JdbcClient.StatementSpec sentencia) {
        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        sentencia.query((ResultSet fila) -> {
            Pedido pedido = pedidos.computeIfAbsent(fila.getInt("id_pedido"), id -> mapearPedido(fila));
            if (fila.getObject("id_detalle") != null) {
                pedido.agregarDetalle(mapearDetalle(fila, pedido.getIdPedido()));
            }
        });
        return List.copyOf(pedidos.values());
    }

    private Pedido mapearPedido(ResultSet fila) {
        try {
            Pedido pedido = new Pedido(fila.getInt("id_usuario"));
            pedido.setIdPedido(fila.getInt("id_pedido"));
            pedido.setNombreCliente(fila.getString("nombre_cliente"));
            pedido.setFechaPedido(fila.getTimestamp("fecha_pedido").toLocalDateTime());
            pedido.setEstado(EstadoPedido.desdeCodigo(fila.getString("estado")));
            return pedido;
        } catch (SQLException e) {
            // computeIfAbsent no admite excepciones comprobadas: se envuelve y Spring la traduce
            throw new IllegalStateException("No se pudo leer el pedido", e);
        }
    }

    private DetallePedido mapearDetalle(ResultSet fila, int idPedido) throws SQLException {
        DetallePedido detalle = new DetallePedido(fila.getInt("id_producto"), fila.getInt("cantidad"));
        detalle.setIdDetalle(fila.getInt("id_detalle"));
        detalle.setIdPedido(idPedido);
        detalle.setNombreProducto(fila.getString("nombre_producto"));
        detalle.setPrecioUnitario(fila.getBigDecimal("precio_unitario"));
        return detalle;
    }
}
