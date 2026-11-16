package co.sena.gestionpedidos.servicio;

import co.sena.gestionpedidos.modelo.Rol;

/**
 * Datos que se reciben para crear o actualizar un usuario (desde el formulario del administrador
 * o desde el registro público de clientes). La contraseña llega en texto plano y el servicio la
 * convierte en hash; nunca se guarda tal cual.
 *
 * @param nombre    nombre completo
 * @param documento número de documento de identidad (6 a 10 dígitos)
 * @param correo    correo electrónico único
 * @param telefono  teléfono de 10 dígitos
 * @param clave     contraseña; obligatoria al crear, opcional al editar
 * @param rol       rol asignado
 * @author Rubén Darío Herrera Beltrán
 */
public record DatosUsuario(String nombre, String documento, String correo, String telefono, String clave,
                           Rol rol) {
}
