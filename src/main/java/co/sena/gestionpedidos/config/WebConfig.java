package co.sena.gestionpedidos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.sena.gestionpedidos.web.seguridad.SesionInterceptor;

/**
 * Configuración de Spring MVC del proyecto: registra el interceptor de sesión y roles
 * para todas las rutas, excepto los recursos estáticos y la página de error.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SesionInterceptor sesionInterceptor;

    /**
     * @param sesionInterceptor interceptor inyectado por Spring
     */
    public WebConfig(SesionInterceptor sesionInterceptor) {
        this.sesionInterceptor = sesionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(sesionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/img/**", "/favicon.ico", "/error");
    }
}
