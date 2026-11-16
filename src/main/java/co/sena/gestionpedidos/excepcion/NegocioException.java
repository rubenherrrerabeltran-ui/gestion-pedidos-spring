package co.sena.gestionpedidos.excepcion;

/**
 * Error causado por una regla de negocio no cumplida (datos inválidos, stock insuficiente,
 * transición de estado no permitida, correo repetido, etc.). Se muestra al usuario tal cual.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class NegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje explicación comprensible para el usuario final
     */
    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
