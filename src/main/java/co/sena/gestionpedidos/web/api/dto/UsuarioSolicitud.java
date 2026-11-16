package co.sena.gestionpedidos.web.api.dto;

import co.sena.gestionpedidos.servicio.DatosUsuario;
import co.sena.gestionpedidos.util.Validador;

/**
 * Cuerpo JSON para crear o actualizar un usuario por la API, o para registrar un cliente.
 * Ejemplo: {@code {"nombre":"Ana Torres","documento":"1020304050","correo":"ana@correo.com",
 * "telefono":"3201112233","clave":"Segura2026","rol":"VENDEDOR"}}
 *
 * @param nombre    nombre completo
 * @param documento documento de identidad (6 a 10 dígitos)
 * @param correo    correo electrónico único
 * @param telefono  teléfono de 10 dígitos
 * @param clave     contraseña (obligatoria al crear; opcional al actualizar)
 * @param rol       ADMINISTRADOR, VENDEDOR o CLIENTE (se ignora en el registro público)
 * @author Rubén Darío Herrera Beltrán
 */
public record UsuarioSolicitud(String nombre, String documento, String correo, String telefono, String clave,
                               String rol) {

    /**
     * @return los datos para el servicio, con el rol convertido y validado
     */
    public DatosUsuario aDatos() {
        return new DatosUsuario(nombre, documento, correo, telefono, clave, Validador.convertirRol(rol));
    }

    /**
     * @return los datos para el registro público (el rol siempre será CLIENTE)
     */
    public DatosUsuario aDatosCliente() {
        return new DatosUsuario(nombre, documento, correo, telefono, clave, null);
    }
}
