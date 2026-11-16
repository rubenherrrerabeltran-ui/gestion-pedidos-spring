package co.sena.gestionpedidos.web.api.dto;

/**
 * Cuerpo JSON para cambiar el estado de un pedido. Ejemplo: {@code {"estado":"PAGADO"}}
 *
 * @param estado nombre del estado destino (PENDIENTE, PAGADO, ENVIADO o CANCELADO)
 * @author Rubén Darío Herrera Beltrán
 */
public record CambioEstadoSolicitud(String estado) {
}
