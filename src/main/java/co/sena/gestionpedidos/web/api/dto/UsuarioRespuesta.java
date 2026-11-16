package co.sena.gestionpedidos.web.api.dto;

import java.time.LocalDateTime;

import co.sena.gestionpedidos.modelo.Usuario;

/**
 * Representación JSON de un usuario. Nunca incluye el hash de la contraseña.
 *
 * @param idUsuario     identificador
 * @param nombre        nombre completo
 * @param documento     documento de identidad
 * @param correo        correo electrónico
 * @param telefono      teléfono
 * @param rol           rol del usuario
 * @param fechaRegistro fecha y hora de registro
 * @author Rubén Darío Herrera Beltrán
 */
public record UsuarioRespuesta(int idUsuario, String nombre, String documento, String correo, String telefono,
                               String rol, LocalDateTime fechaRegistro) {

    /**
     * @param usuario entidad leída de la base de datos
     * @return la respuesta para la API
     */
    public static UsuarioRespuesta desde(Usuario usuario) {
        return new UsuarioRespuesta(usuario.getIdUsuario(), usuario.getNombre(), usuario.getDocumento(),
                usuario.getCorreo(), usuario.getTelefono(), usuario.getRol().name(), usuario.getFechaRegistro());
    }
}
