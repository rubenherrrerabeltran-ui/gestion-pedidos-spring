package co.sena.gestionpedidos.movil.datos.remoto;

import java.io.IOException;

/**
 * Estrategia de transporte HTTP. La aplicación usa {@link TransporteHttpUrlConnection};
 * las pruebas unitarias usan un transporte falso que devuelve respuestas preparadas.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public interface TransporteHttp {

    /**
     * Envía una petición HTTP con cuerpo JSON opcional.
     *
     * @param metodo     GET, POST, PATCH o DELETE
     * @param url        URL completa
     * @param cuerpoJson cuerpo JSON o {@code null}
     * @return respuesta del servidor
     * @throws IOException si no hay conexión con el servidor
     */
    RespuestaHttp enviar(String metodo, String url, String cuerpoJson) throws IOException;

    /** Olvida la cookie de sesión (al cerrar sesión). */
    void limpiarSesion();
}
