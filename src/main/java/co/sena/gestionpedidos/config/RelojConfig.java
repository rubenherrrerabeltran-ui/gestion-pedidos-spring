package co.sena.gestionpedidos.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publica el reloj del sistema como bean para que los servicios que dependen de la fecha
 * (por ejemplo, los reportes) se puedan probar con una fecha fija.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Configuration
public class RelojConfig {

    /** Zona horaria del negocio (Colombia). */
    public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    /**
     * @return reloj en la zona horaria de Colombia
     */
    @Bean
    public Clock reloj() {
        return Clock.system(ZONA_COLOMBIA);
    }
}
