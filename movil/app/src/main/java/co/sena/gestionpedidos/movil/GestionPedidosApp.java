package co.sena.gestionpedidos.movil;

import android.app.Application;

/**
 * Clase de aplicación: crea el contenedor de dependencias una sola vez al abrir la app.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class GestionPedidosApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Dependencias.iniciar(this);
    }
}
