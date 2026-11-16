package co.sena.gestionpedidos.excepcion;

/**
 * Se lanza cuando el usuario autenticado no tiene el rol requerido para una operación.
 * Se traduce en una respuesta HTTP 403.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class AccesoDenegadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje motivo del rechazo
     */
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
