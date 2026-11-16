package co.sena.gestionpedidos.movil;

import android.content.Context;

import co.sena.gestionpedidos.movil.datos.local.PreferenciasApp;
import co.sena.gestionpedidos.movil.datos.remoto.ApiGestionPedidos;
import co.sena.gestionpedidos.movil.datos.remoto.TransporteHttp;
import co.sena.gestionpedidos.movil.datos.remoto.TransporteHttpUrlConnection;
import co.sena.gestionpedidos.movil.dominio.carrito.Carrito;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.ui.comun.Ejecutor;

/**
 * Contenedor de dependencias (patrón Service Locator / Singleton): crea y comparte la API,
 * el carrito, la sesión y el ejecutor de tareas entre todas las pantallas.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Dependencias {

    private static Dependencias instancia;

    private final PreferenciasApp preferencias;
    private final TransporteHttp transporte = new TransporteHttpUrlConnection();
    private final Carrito carrito = new Carrito();
    private final Ejecutor ejecutor = new Ejecutor();
    private ApiGestionPedidos api;
    private Usuario usuario;

    private Dependencias(Context contexto) {
        preferencias = new PreferenciasApp(contexto);
        api = new ApiGestionPedidos(transporte, preferencias.getUrlServidor());
    }

    static synchronized void iniciar(Context contexto) {
        if (instancia == null) {
            instancia = new Dependencias(contexto.getApplicationContext());
        }
    }

    /** @return la instancia única creada por {@link GestionPedidosApp} */
    public static Dependencias get() {
        return instancia;
    }

    public PreferenciasApp getPreferencias() {
        return preferencias;
    }

    public synchronized ApiGestionPedidos getApi() {
        return api;
    }

    /**
     * Cambia la dirección del servidor (si es distinta) y la guarda para la próxima vez.
     *
     * @param url nueva dirección
     */
    public synchronized void configurarServidor(String url) {
        preferencias.setUrlServidor(url);
        if (!api.getUrlBase().equals(preferencias.getUrlServidor())) {
            transporte.limpiarSesion();
            api = new ApiGestionPedidos(transporte, preferencias.getUrlServidor());
        }
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public Ejecutor getEjecutor() {
        return ejecutor;
    }

    public synchronized Usuario getUsuario() {
        return usuario;
    }

    public synchronized void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    /** Cierra la sesión local: olvida el usuario y vacía el carrito. */
    public synchronized void cerrarSesionLocal() {
        usuario = null;
        carrito.vaciar();
    }
}
