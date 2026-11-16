package co.sena.gestionpedidos.movil.datos.remoto;

/**
 * Respuesta de una petición HTTP: código de estado y cuerpo en texto.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class RespuestaHttp {

    private final int estado;
    private final String cuerpo;

    /**
     * @param estado código HTTP (200, 201, 401...)
     * @param cuerpo cuerpo de la respuesta (puede estar vacío)
     */
    public RespuestaHttp(int estado, String cuerpo) {
        this.estado = estado;
        this.cuerpo = cuerpo == null ? "" : cuerpo;
    }

    public int getEstado() {
        return estado;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    /** @return {@code true} si el código está entre 200 y 299 */
    public boolean esExitosa() {
        return estado >= 200 && estado < 300;
    }
}
