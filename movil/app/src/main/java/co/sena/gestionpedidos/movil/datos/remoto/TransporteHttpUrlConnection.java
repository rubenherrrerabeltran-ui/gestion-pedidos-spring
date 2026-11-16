package co.sena.gestionpedidos.movil.datos.remoto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Transporte HTTP con {@link HttpURLConnection} del SDK de Android (sin librerías externas).
 * Guarda la cookie {@code JSESSIONID} que entrega la API al iniciar sesión y la envía en cada
 * petición, igual que lo hace un navegador.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class TransporteHttpUrlConnection implements TransporteHttp {

    private static final int TIEMPO_ESPERA_MS = 10_000;
    private static final String COOKIE_SESION = "JSESSIONID";

    private volatile String cookieSesion;

    @Override
    public RespuestaHttp enviar(String metodo, String url, String cuerpoJson) throws IOException {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(url).toURL().openConnection();
        try {
            conexion.setRequestMethod(metodo);
            conexion.setConnectTimeout(TIEMPO_ESPERA_MS);
            conexion.setReadTimeout(TIEMPO_ESPERA_MS);
            conexion.setUseCaches(false);
            conexion.setRequestProperty("Accept", "application/json");
            if (cookieSesion != null) {
                conexion.setRequestProperty("Cookie", cookieSesion);
            }
            if (cuerpoJson != null) {
                byte[] bytes = cuerpoJson.getBytes(StandardCharsets.UTF_8);
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conexion.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(bytes);
                }
            }
            int estado = conexion.getResponseCode();
            guardarCookie(conexion.getHeaderFields());
            InputStream entrada = estado >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            return new RespuestaHttp(estado, leerTodo(entrada));
        } finally {
            conexion.disconnect();
        }
    }

    @Override
    public void limpiarSesion() {
        cookieSesion = null;
    }

    private void guardarCookie(Map<String, List<String>> cabeceras) {
        for (Map.Entry<String, List<String>> cabecera : cabeceras.entrySet()) {
            if (cabecera.getKey() == null || !"Set-Cookie".equalsIgnoreCase(cabecera.getKey())) {
                continue;
            }
            for (String valor : cabecera.getValue()) {
                if (valor.startsWith(COOKIE_SESION + "=")) {
                    cookieSesion = valor.split(";", 2)[0];
                }
            }
        }
    }

    private static String leerTodo(InputStream entrada) throws IOException {
        if (entrada == null) {
            return "";
        }
        try (InputStream in = entrada; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int leidos;
            while ((leidos = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, leidos);
            }
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
