package co.sena.gestionpedidos.movil.util;

import java.util.regex.Pattern;

/**
 * Validaciones de los datos que escribe el usuario, antes de enviarlos a la API.
 * Devuelven el mensaje de error o {@code null} si el dato es válido.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Validador {

    private static final Pattern CORREO =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern URL_SERVIDOR =
            Pattern.compile("^https?://[A-Za-z0-9.-]+(:\\d{1,5})?(/.*)?$");
    private static final Pattern FECHA = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private Validador() {
    }

    /**
     * @param correo correo escrito
     * @return mensaje de error o {@code null}
     */
    public static String validarCorreo(String correo) {
        if (estaVacio(correo)) {
            return "Escriba su correo";
        }
        return CORREO.matcher(correo.trim()).matches() ? null : "El correo no tiene un formato válido";
    }

    /**
     * @param clave contraseña escrita
     * @return mensaje de error o {@code null}
     */
    public static String validarClave(String clave) {
        return estaVacio(clave) ? "Escriba su contraseña" : null;
    }

    /**
     * @param url dirección del servidor, por ejemplo http://10.0.2.2:8080
     * @return mensaje de error o {@code null}
     */
    public static String validarUrlServidor(String url) {
        if (estaVacio(url)) {
            return "Escriba la dirección del servidor";
        }
        return URL_SERVIDOR.matcher(url.trim()).matches()
                ? null : "Use el formato http://servidor:puerto";
    }

    /**
     * Valida un rango de fechas opcional en formato AAAA-MM-DD.
     *
     * @param desde fecha inicial (vacía = por defecto)
     * @param hasta fecha final (vacía = hoy)
     * @return mensaje de error o {@code null}
     */
    public static String validarRangoFechas(String desde, String hasta) {
        if (!estaVacio(desde) && !FECHA.matcher(desde.trim()).matches()
                || !estaVacio(hasta) && !FECHA.matcher(hasta.trim()).matches()) {
            return "Use fechas con el formato AAAA-MM-DD";
        }
        if (!estaVacio(desde) && !estaVacio(hasta) && desde.trim().compareTo(hasta.trim()) > 0) {
            return "La fecha inicial no puede ser posterior a la final";
        }
        return null;
    }

    /**
     * @param texto texto que se revisa
     * @return {@code true} si es nulo o solo tiene espacios
     */
    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /**
     * Quita espacios y la barra final de la URL del servidor.
     *
     * @param url dirección escrita
     * @return dirección normalizada
     */
    public static String normalizarUrl(String url) {
        String limpia = url == null ? "" : url.trim();
        while (limpia.endsWith("/")) {
            limpia = limpia.substring(0, limpia.length() - 1);
        }
        return limpia;
    }
}
