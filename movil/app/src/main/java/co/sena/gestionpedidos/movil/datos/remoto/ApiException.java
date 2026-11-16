package co.sena.gestionpedidos.movil.datos.remoto;

/**
 * Error devuelto por la API REST (formato ProblemDetail) o de conexión.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ApiException extends Exception {

    /** Código usado cuando no hubo respuesta del servidor. */
    public static final int SIN_CONEXION = 0;

    private final int estado;

    /**
     * @param estado  código HTTP del error (0 si no hubo conexión)
     * @param mensaje mensaje para mostrar al usuario
     */
    public ApiException(int estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public int getEstado() {
        return estado;
    }

    /** @return {@code true} si la sesión no existe o expiró (HTTP 401) */
    public boolean esSesionExpirada() {
        return estado == 401;
    }
}
