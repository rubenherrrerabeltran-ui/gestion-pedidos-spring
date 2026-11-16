package co.sena.gestionpedidos.web.api.dto;

/**
 * Cuerpo JSON para iniciar sesión en la API.
 *
 * @param correo correo del usuario
 * @param clave  contraseña
 * @author Rubén Darío Herrera Beltrán
 */
public record CredencialesSolicitud(String correo, String clave) {
}
