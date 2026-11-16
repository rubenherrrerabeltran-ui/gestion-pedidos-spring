package co.sena.gestionpedidos.movil.datos.local;

import android.content.Context;
import android.content.SharedPreferences;

import co.sena.gestionpedidos.movil.BuildConfig;
import co.sena.gestionpedidos.movil.util.Validador;

/**
 * Datos locales guardados con SharedPreferences: la dirección del servidor y el último correo.
 * La contraseña nunca se guarda en el teléfono.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class PreferenciasApp {

    private static final String ARCHIVO = "gestion_pedidos_preferencias";
    private static final String URL_SERVIDOR = "url_servidor";
    private static final String ULTIMO_CORREO = "ultimo_correo";

    private final SharedPreferences preferencias;

    public PreferenciasApp(Context contexto) {
        preferencias = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    /** @return la URL guardada o la definida en el build (http://10.0.2.2:8080 en el emulador) */
    public String getUrlServidor() {
        return preferencias.getString(URL_SERVIDOR, BuildConfig.URL_SERVIDOR);
    }

    public void setUrlServidor(String url) {
        preferencias.edit().putString(URL_SERVIDOR, Validador.normalizarUrl(url)).apply();
    }

    public String getUltimoCorreo() {
        return preferencias.getString(ULTIMO_CORREO, "");
    }

    public void setUltimoCorreo(String correo) {
        preferencias.edit().putString(ULTIMO_CORREO, correo.trim()).apply();
    }
}
