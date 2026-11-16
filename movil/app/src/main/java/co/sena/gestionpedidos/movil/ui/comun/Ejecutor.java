package co.sena.gestionpedidos.movil.ui.comun;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ejecuta las llamadas a la API en hilos de fondo y entrega el resultado en el hilo principal,
 * para que la interfaz nunca se congele (Android no permite red en el hilo principal).
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class Ejecutor {

    /** Trabajo que se hace en segundo plano. */
    public interface Tarea<T> {
        T ejecutar() throws Exception;
    }

    /** Recibe el resultado en el hilo principal (patrón Observer / Callback). */
    public interface Resultado<T> {
        void exito(T valor);

        void error(Exception error);
    }

    private final ExecutorService hilos = Executors.newFixedThreadPool(2);
    private final Handler principal = new Handler(Looper.getMainLooper());

    /**
     * @param tarea     trabajo en segundo plano
     * @param resultado quién recibe el resultado o el error
     * @param <T>       tipo del resultado
     */
    public <T> void ejecutar(Tarea<T> tarea, Resultado<T> resultado) {
        hilos.execute(() -> {
            try {
                T valor = tarea.ejecutar();
                principal.post(() -> resultado.exito(valor));
            } catch (Exception e) {
                principal.post(() -> resultado.error(e));
            }
        });
    }
}
