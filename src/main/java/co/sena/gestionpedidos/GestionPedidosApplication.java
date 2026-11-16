package co.sena.gestionpedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del sistema de gestión de pedidos construido con Spring Boot.
 *
 * <p>{@code @SpringBootApplication} activa la configuración automática (Tomcat embebido, Spring MVC,
 * Thymeleaf, JDBC y transacciones) y el escaneo de componentes del paquete {@code co.sena.gestionpedidos}:
 * controladores ({@code @Controller}, {@code @RestController}), servicios ({@code @Service}) y
 * repositorios ({@code @Repository}).</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@SpringBootApplication
public class GestionPedidosApplication {

    /**
     * Inicia el contenedor de Spring y el servidor web embebido.
     *
     * @param args argumentos de línea de comandos, por ejemplo {@code --spring.profiles.active=demo}
     */
    public static void main(String[] args) {
        SpringApplication.run(GestionPedidosApplication.class, args);
    }
}
