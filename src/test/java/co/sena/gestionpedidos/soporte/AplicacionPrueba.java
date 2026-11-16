package co.sena.gestionpedidos.soporte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import co.sena.gestionpedidos.GestionPedidosApplication;

/**
 * Arranca una sola vez la aplicación Spring Boot completa para todas las pruebas:
 * perfil {@code demo} (H2 en memoria, modo MySQL) y puerto aleatorio libre.
 * Así las pruebas ejercitan el framework real: inyección de dependencias, transacciones,
 * Spring MVC, Thymeleaf, Jackson y el servidor Tomcat embebido.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class AplicacionPrueba {

    private static ConfigurableApplicationContext contexto;

    private AplicacionPrueba() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * @return el contexto de Spring (se crea en la primera llamada)
     */
    public static synchronized ConfigurableApplicationContext contexto() {
        if (contexto == null) {
            contexto = SpringApplication.run(GestionPedidosApplication.class,
                    "--spring.profiles.active=demo", "--server.port=0", "--spring.main.banner-mode=off",
                    "--spring.datasource.url=jdbc:h2:mem:pruebas;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
            Runtime.getRuntime().addShutdownHook(new Thread(contexto::close));
        }
        return contexto;
    }

    /**
     * @param tipo clase del bean buscado (servicio, repositorio...)
     * @param <T>  tipo del bean
     * @return el bean administrado por Spring
     */
    public static <T> T bean(Class<T> tipo) {
        return contexto().getBean(tipo);
    }

    /**
     * @return URL base del servidor embebido, por ejemplo {@code http://localhost:51234}
     */
    public static String urlBase() {
        int puerto = ((WebServerApplicationContext) contexto()).getWebServer().getPort();
        return "http://localhost:" + puerto;
    }
}
