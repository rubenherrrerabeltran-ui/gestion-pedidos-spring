package co.sena.gestionpedidos.repositorio;

import java.util.List;
import java.util.Optional;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;

/**
 * Contrato de acceso a datos de la tabla {@code usuario}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public interface UsuarioRepositorio {

    /**
     * @return todos los usuarios ordenados por nombre
     */
    List<Usuario> listar();

    /**
     * @param rol rol buscado
     * @return usuarios que tienen ese rol
     */
    List<Usuario> listarPorRol(Rol rol);

    /**
     * @param idUsuario identificador del usuario
     * @return el usuario, o vacío si no existe
     */
    Optional<Usuario> buscarPorId(int idUsuario);

    /**
     * @param correo correo electrónico (único)
     * @return el usuario, o vacío si no existe
     */
    Optional<Usuario> buscarPorCorreo(String correo);

    /**
     * @param documento número de documento (único)
     * @return el usuario, o vacío si no existe
     */
    Optional<Usuario> buscarPorDocumento(String documento);

    /**
     * Inserta el usuario si no tiene id, o actualiza sus datos, rol y hash si ya lo tiene.
     *
     * @param usuario datos a guardar (con la contraseña ya convertida en hash)
     * @return el usuario con su identificador asignado
     */
    Usuario guardar(Usuario usuario);

    /**
     * @param idUsuario identificador del usuario
     * @return {@code true} si se eliminó una fila
     */
    boolean eliminar(int idUsuario);

    /**
     * @return número total de usuarios registrados
     */
    long contar();
}
