package co.sena.gestionpedidos.web.seguridad;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import co.sena.gestionpedidos.excepcion.AccesoDenegadoException;

/**
 * Interceptor de Spring MVC que controla el acceso antes de ejecutar cualquier controlador.
 *
 * <ol>
 *   <li>Si el método o la clase tienen {@link Publico}, deja pasar.</li>
 *   <li>Si no hay usuario en sesión: en la web redirige a {@code /login}; en la API responde 401.</li>
 *   <li>Si hay {@link RequiereRol} y el usuario no tiene ese rol, lanza {@link AccesoDenegadoException} (403).</li>
 * </ol>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest solicitud, HttpServletResponse respuesta, Object manejador)
            throws IOException {
        if (!(manejador instanceof HandlerMethod metodo) || esPublico(metodo)) {
            return true; // recursos estáticos o métodos marcados como públicos
        }
        HttpSession sesion = solicitud.getSession(false);
        UsuarioSesion usuario = sesion == null ? null : (UsuarioSesion) sesion.getAttribute(UsuarioSesion.ATRIBUTO);
        if (usuario == null) {
            responderSinSesion(solicitud, respuesta);
            return false;
        }
        RequiereRol requiereRol = buscarRequiereRol(metodo);
        if (requiereRol != null && !usuario.tieneRol(requiereRol.value())) {
            throw new AccesoDenegadoException("Su rol (" + usuario.rol() + ") no tiene permiso para esta opción");
        }
        return true;
    }

    private boolean esPublico(HandlerMethod metodo) {
        return metodo.hasMethodAnnotation(Publico.class)
                || AnnotatedElementUtils.hasAnnotation(metodo.getBeanType(), Publico.class);
    }

    /** La anotación del método tiene prioridad sobre la de la clase. */
    private RequiereRol buscarRequiereRol(HandlerMethod metodo) {
        RequiereRol enMetodo = metodo.getMethodAnnotation(RequiereRol.class);
        return enMetodo != null ? enMetodo : AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(),
                RequiereRol.class);
    }

    private void responderSinSesion(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        if (solicitud.getRequestURI().startsWith(solicitud.getContextPath() + "/api/")) {
            respuesta.setStatus(HttpStatus.UNAUTHORIZED.value());
            respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            respuesta.setCharacterEncoding("UTF-8");
            respuesta.getWriter().write("{\"status\":401,\"title\":\"Unauthorized\","
                    + "\"detail\":\"Inicie sesión con POST /api/sesion\"}");
        } else {
            respuesta.sendRedirect(solicitud.getContextPath() + "/login");
        }
    }
}
