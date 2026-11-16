package co.sena.gestionpedidos.movil.datos;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import co.sena.gestionpedidos.movil.datos.remoto.RespuestaHttp;
import co.sena.gestionpedidos.movil.datos.remoto.TransporteHttp;

/**
 * Doble de prueba (Fake) del transporte HTTP: devuelve respuestas preparadas y registra
 * las peticiones para verificarlas, sin necesidad de servidor ni de red.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class TransporteFalso implements TransporteHttp {

    /** Petición registrada. */
    public static final class Peticion {
        public final String metodo;
        public final String url;
        public final String cuerpo;

        Peticion(String metodo, String url, String cuerpo) {
            this.metodo = metodo;
            this.url = url;
            this.cuerpo = cuerpo;
        }
    }

    private final Deque<RespuestaHttp> respuestas = new ArrayDeque<>();
    public final List<Peticion> peticiones = new ArrayList<>();
    public boolean sinConexion;
    public boolean sesionLimpiada;

    /** Encola una respuesta HTTP preparada. */
    public TransporteFalso responder(int estado, String cuerpo) {
        respuestas.add(new RespuestaHttp(estado, cuerpo));
        return this;
    }

    @Override
    public RespuestaHttp enviar(String metodo, String url, String cuerpoJson) throws IOException {
        peticiones.add(new Peticion(metodo, url, cuerpoJson));
        if (sinConexion) {
            throw new IOException("Connection refused");
        }
        return respuestas.isEmpty() ? new RespuestaHttp(204, "") : respuestas.poll();
    }

    @Override
    public void limpiarSesion() {
        sesionLimpiada = true;
    }

    /** @return la última petición enviada */
    public Peticion ultima() {
        return peticiones.get(peticiones.size() - 1);
    }
}
