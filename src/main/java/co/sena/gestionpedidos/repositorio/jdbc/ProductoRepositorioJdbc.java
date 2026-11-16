package co.sena.gestionpedidos.repositorio.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.repositorio.ProductoRepositorio;

/**
 * Implementación JDBC del repositorio de productos usando {@link JdbcClient} de Spring.
 *
 * <p>Spring administra la conexión (pool HikariCP), cierra los recursos y traduce las
 * {@code SQLException} a la jerarquía {@code DataAccessException}. Todas las consultas usan
 * parámetros con nombre ({@code :nombre}), por lo que no hay concatenación de datos en el SQL.</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Repository
public class ProductoRepositorioJdbc implements ProductoRepositorio {

    private static final String SQL_LISTAR = """
            SELECT id_producto, nombre_producto, precio, stock, activo
            FROM producto
            WHERE LOWER(nombre_producto) LIKE :patron ESCAPE '!'
            ORDER BY nombre_producto""";
    private static final String SQL_DISPONIBLES = """
            SELECT id_producto, nombre_producto, precio, stock, activo
            FROM producto
            WHERE activo = TRUE AND stock > 0
            ORDER BY nombre_producto""";
    private static final String SQL_BUSCAR_POR_ID = """
            SELECT id_producto, nombre_producto, precio, stock, activo
            FROM producto WHERE id_producto = :id""";
    private static final String SQL_INSERTAR = """
            INSERT INTO producto (nombre_producto, precio, stock, activo)
            VALUES (:nombre, :precio, :stock, :activo)""";
    private static final String SQL_ACTUALIZAR = """
            UPDATE producto SET nombre_producto = :nombre, precio = :precio, stock = :stock, activo = :activo
            WHERE id_producto = :id""";
    private static final String SQL_ELIMINAR = "DELETE FROM producto WHERE id_producto = :id";
    private static final String SQL_DESCONTAR_STOCK = """
            UPDATE producto SET stock = stock - :cantidad
            WHERE id_producto = :id AND activo = TRUE AND stock >= :cantidad""";
    private static final String SQL_DEVOLVER_STOCK =
            "UPDATE producto SET stock = stock + :cantidad WHERE id_producto = :id";
    private static final String SQL_CONTAR_STOCK_BAJO =
            "SELECT COUNT(*) FROM producto WHERE activo = TRUE AND stock < :umbral";

    private final JdbcClient jdbcClient;

    /**
     * Spring inyecta el {@link JdbcClient} configurado automáticamente con el DataSource.
     *
     * @param jdbcClient cliente JDBC de Spring
     */
    public ProductoRepositorioJdbc(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Producto> listar(String texto) {
        // Los comodines % y _ escritos por el usuario se buscan como texto normal (se escapan con !)
        String buscado = texto == null ? "" : texto.trim().toLowerCase()
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        String patron = "%" + buscado + "%";
        return jdbcClient.sql(SQL_LISTAR).param("patron", patron).query(this::mapear).list();
    }

    @Override
    public List<Producto> listarDisponibles() {
        return jdbcClient.sql(SQL_DISPONIBLES).query(this::mapear).list();
    }

    @Override
    public Optional<Producto> buscarPorId(int idProducto) {
        return jdbcClient.sql(SQL_BUSCAR_POR_ID).param("id", idProducto).query(this::mapear).optional();
    }

    @Override
    public Producto guardar(Producto producto) {
        JdbcClient.StatementSpec sentencia = jdbcClient
                .sql(producto.getIdProducto() == null ? SQL_INSERTAR : SQL_ACTUALIZAR)
                .param("nombre", producto.getNombreProducto())
                .param("precio", producto.getPrecio())
                .param("stock", producto.getStock())
                .param("activo", producto.isActivo());
        if (producto.getIdProducto() == null) {
            KeyHolder llaveGenerada = new GeneratedKeyHolder();
            sentencia.update(llaveGenerada, "id_producto");
            producto.setIdProducto(llaveGenerada.getKey().intValue());
        } else {
            sentencia.param("id", producto.getIdProducto()).update();
        }
        return producto;
    }

    @Override
    public boolean eliminar(int idProducto) {
        return jdbcClient.sql(SQL_ELIMINAR).param("id", idProducto).update() == 1;
    }

    @Override
    public boolean descontarStock(int idProducto, int cantidad) {
        return jdbcClient.sql(SQL_DESCONTAR_STOCK)
                .param("id", idProducto)
                .param("cantidad", cantidad)
                .update() == 1;
    }

    @Override
    public void devolverStock(int idProducto, int cantidad) {
        jdbcClient.sql(SQL_DEVOLVER_STOCK).param("id", idProducto).param("cantidad", cantidad).update();
    }

    @Override
    public long contarStockBajo(int umbral) {
        return jdbcClient.sql(SQL_CONTAR_STOCK_BAJO).param("umbral", umbral).query(Long.class).single();
    }

    /** Convierte una fila del {@link ResultSet} en un objeto {@link Producto} (RowMapper). */
    private Producto mapear(ResultSet fila, int numeroFila) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(fila.getInt("id_producto"));
        producto.setNombreProducto(fila.getString("nombre_producto"));
        producto.setPrecio(fila.getBigDecimal("precio"));
        producto.setStock(fila.getInt("stock"));
        producto.setActivo(fila.getBoolean("activo"));
        return producto;
    }
}
