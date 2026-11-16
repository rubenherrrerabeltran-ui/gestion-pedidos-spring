package co.sena.gestionpedidos.web.controlador;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.boot.SpringBootVersion;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * Agrega a todas las vistas el usuario autenticado, la ruta actual y la versión del framework
 * (usados por el encabezado para mostrar el menú según el rol y resaltar la opción activa).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@ControllerAdvice(basePackageClasses = AtributosGlobales.class)
public class AtributosGlobales {

    /**
     * @param sesion sesión actual (puede no existir)
     * @return usuario autenticado o {@code null}
     */
    @ModelAttribute("usuarioActual")
    public UsuarioSesion usuarioActual(HttpSession sesion) {
        return (UsuarioSesion) sesion.getAttribute(UsuarioSesion.ATRIBUTO);
    }

    /**
     * @param solicitud petición actual
     * @return ruta solicitada, por ejemplo {@code /productos}
     */
    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest solicitud) {
        return solicitud.getRequestURI().substring(solicitud.getContextPath().length());
    }

    /**
     * @return versión de Spring Boot en ejecución (se muestra en el pie de página)
     */
    @ModelAttribute("versionSpringBoot")
    public String versionSpringBoot() {
        return SpringBootVersion.getVersion();
    }
}
