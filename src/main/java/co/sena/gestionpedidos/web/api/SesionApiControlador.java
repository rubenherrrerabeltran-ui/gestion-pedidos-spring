package co.sena.gestionpedidos.web.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.api.dto.CredencialesSolicitud;
import co.sena.gestionpedidos.web.seguridad.Publico;
import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * API REST de sesión: permite a un cliente HTTP (Postman, curl, app móvil) autenticarse.
 * La sesión queda en la cookie {@code JSESSIONID} que se envía en las siguientes peticiones.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RestController
@RequestMapping("/api/sesion")
public class SesionApiControlador {

    private final UsuarioServicio usuarioServicio;

    /**
     * @param usuarioServicio servicio de usuarios
     */
    public SesionApiControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * POST /api/sesion: inicia sesión.
     *
     * @param credenciales correo y contraseña en JSON
     * @param solicitud    petición HTTP
     * @return datos del usuario autenticado (sin contraseña)
     */
    @PostMapping
    @Publico
    public UsuarioSesion iniciar(@RequestBody CredencialesSolicitud credenciales, HttpServletRequest solicitud) {
        UsuarioSesion usuario = UsuarioSesion.desde(
                usuarioServicio.autenticar(credenciales.correo(), credenciales.clave()));
        solicitud.getSession(true);
        solicitud.changeSessionId();
        solicitud.getSession().setAttribute(UsuarioSesion.ATRIBUTO, usuario);
        return usuario;
    }

    /**
     * GET /api/sesion: usuario de la sesión actual.
     *
     * @param usuario usuario autenticado
     * @return datos del usuario
     */
    @GetMapping
    public UsuarioSesion actual(@SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario) {
        return usuario;
    }

    /**
     * DELETE /api/sesion: cierra la sesión.
     *
     * @param sesion sesión actual
     * @return 204 sin contenido
     */
    @DeleteMapping
    public ResponseEntity<Void> cerrar(HttpSession sesion) {
        sesion.invalidate();
        return ResponseEntity.noContent().build();
    }
}
