package co.sena.gestionpedidos.soporte;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cliente HTTP de prueba que conserva la cookie de sesión (como un navegador) y no sigue
 * redirecciones, para poder verificar los códigos 302 del patrón Post/Redirect/Get.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ClienteHttp {

    private final HttpClient cliente = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * @param ruta ruta relativa, por ejemplo {@code /productos}
     * @return respuesta con el cuerpo como texto
     */
    public HttpResponse<String> get(String ruta) {
        return enviar(HttpRequest.newBuilder(uri(ruta)).GET().build());
    }

    /**
     * Envía un formulario HTML (application/x-www-form-urlencoded) con POST.
     *
     * @param ruta   ruta relativa
     * @param campos nombre y valor de cada campo
     * @return respuesta
     */
    public HttpResponse<String> postFormulario(String ruta, Map<String, String> campos) {
        String cuerpo = campos.entrySet().stream()
                .map(c -> codificar(c.getKey()) + "=" + codificar(c.getValue()))
                .collect(Collectors.joining("&"));
        return enviar(HttpRequest.newBuilder(uri(ruta))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build());
    }

    /**
     * Envía JSON con el método indicado (POST, PUT, PATCH, DELETE).
     *
     * @param metodo verbo HTTP
     * @param ruta   ruta relativa
     * @param json   cuerpo JSON (puede ser vacío)
     * @return respuesta
     */
    public HttpResponse<String> json(String metodo, String ruta, String json) {
        return enviar(HttpRequest.newBuilder(uri(ruta))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(metodo, HttpRequest.BodyPublishers.ofString(json)).build());
    }

    /**
     * Inicia sesión por el formulario web.
     *
     * @param correo correo del usuario
     * @param clave  contraseña
     * @return respuesta (302 si es correcto)
     */
    public HttpResponse<String> iniciarSesion(String correo, String clave) {
        return postFormulario("/login", Map.of("correo", correo, "clave", clave));
    }

    private URI uri(String ruta) {
        return URI.create(AplicacionPrueba.urlBase() + ruta);
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private HttpResponse<String> enviar(HttpRequest solicitud) {
        try {
            return cliente.send(solicitud, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo conectar con " + solicitud.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Petición interrumpida", e);
        }
    }
}
