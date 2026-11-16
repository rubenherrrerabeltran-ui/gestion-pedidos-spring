package co.sena.gestionpedidos.web.seguridad;

import java.io.Serializable;
import java.util.Arrays;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;

/**
 * Datos mínimos del usuario autenticado que se guardan en la sesión HTTP.
 * No incluye el hash de la contraseña, por lo que también se puede devolver de forma segura en la API.
 *
 * @param idUsuario identificador del usuario
 * @param nombre    nombre para mostrar
 * @param correo    correo de acceso
 * @param rol       rol que determina los permisos
 * @author Rubén Darío Herrera Beltrán
 */
public record UsuarioSesion(int idUsuario, String nombre, String correo, Rol rol) implements Serializable {

    /** Nombre del atributo de sesión donde se guarda el usuario autenticado. */
    public static final String ATRIBUTO = "usuarioSesion";

    /**
     * @param usuario usuario leído de la base de datos
     * @return la versión para sesión del usuario
     */
    public static UsuarioSesion desde(Usuario usuario) {
        return new UsuarioSesion(usuario.getIdUsuario(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }

    /**
     * @param roles roles aceptados
     * @return {@code true} si el usuario tiene alguno de los roles indicados
     */
    public boolean tieneRol(Rol... roles) {
        return Arrays.asList(roles).contains(rol);
    }

    /**
     * @return {@code true} si es cliente (solo puede ver y crear sus propios pedidos)
     */
    public boolean esCliente() {
        return rol == Rol.CLIENTE;
    }
}
