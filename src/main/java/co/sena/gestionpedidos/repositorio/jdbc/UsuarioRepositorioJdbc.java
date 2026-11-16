package co.sena.gestionpedidos.repositorio.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.repositorio.UsuarioRepositorio;

/**
 * Implementación JDBC del repositorio de usuarios usando {@link JdbcClient} de Spring.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Repository
public class UsuarioRepositorioJdbc implements UsuarioRepositorio {

    private static final String COLUMNAS =
            "SELECT id_usuario, nombre, correo, password_hash, rol, fecha_registro FROM usuario ";
    private static final String SQL_INSERTAR = """
            INSERT INTO usuario (nombre, correo, password_hash, rol)
            VALUES (:nombre, :correo, :hash, :rol)""";
    private static final String SQL_ACTUALIZAR = """
            UPDATE usuario SET nombre = :nombre, correo = :correo, password_hash = :hash, rol = :rol
            WHERE id_usuario = :id""";

    private final JdbcClient jdbcClient;

    /**
     * @param jdbcClient cliente JDBC de Spring
     */
    public UsuarioRepositorioJdbc(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Usuario> listar() {
        return jdbcClient.sql(COLUMNAS + "ORDER BY nombre").query(this::mapear).list();
    }

    @Override
    public List<Usuario> listarPorRol(Rol rol) {
        return jdbcClient.sql(COLUMNAS + "WHERE rol = :rol ORDER BY nombre")
                .param("rol", rol.name())
                .query(this::mapear)
                .list();
    }

    @Override
    public Optional<Usuario> buscarPorId(int idUsuario) {
        return jdbcClient.sql(COLUMNAS + "WHERE id_usuario = :id")
                .param("id", idUsuario)
                .query(this::mapear)
                .optional();
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return jdbcClient.sql(COLUMNAS + "WHERE LOWER(correo) = :correo")
                .param("correo", correo.trim().toLowerCase())
                .query(this::mapear)
                .optional();
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        JdbcClient.StatementSpec sentencia = jdbcClient
                .sql(usuario.getIdUsuario() == null ? SQL_INSERTAR : SQL_ACTUALIZAR)
                .param("nombre", usuario.getNombre())
                .param("correo", usuario.getCorreo())
                .param("hash", usuario.getPasswordHash())
                .param("rol", usuario.getRol().name());
        if (usuario.getIdUsuario() == null) {
            KeyHolder llaveGenerada = new GeneratedKeyHolder();
            sentencia.update(llaveGenerada, "id_usuario");
            usuario.setIdUsuario(llaveGenerada.getKey().intValue());
        } else {
            sentencia.param("id", usuario.getIdUsuario()).update();
        }
        return usuario;
    }

    @Override
    public boolean eliminar(int idUsuario) {
        return jdbcClient.sql("DELETE FROM usuario WHERE id_usuario = :id").param("id", idUsuario).update() == 1;
    }

    @Override
    public long contar() {
        return jdbcClient.sql("SELECT COUNT(*) FROM usuario").query(Long.class).single();
    }

    /** Convierte una fila del {@link ResultSet} en un objeto {@link Usuario}. */
    private Usuario mapear(ResultSet fila, int numeroFila) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(fila.getInt("id_usuario"));
        usuario.setNombre(fila.getString("nombre"));
        usuario.setCorreo(fila.getString("correo"));
        usuario.setPasswordHash(fila.getString("password_hash"));
        usuario.setRol(Rol.valueOf(fila.getString("rol")));
        Timestamp fechaRegistro = fila.getTimestamp("fecha_registro");
        usuario.setFechaRegistro(fechaRegistro == null ? null : fechaRegistro.toLocalDateTime());
        return usuario;
    }
}
