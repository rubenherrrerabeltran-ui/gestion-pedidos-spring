package co.sena.gestionpedidos.web.controlador;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.seguridad.Publico;
import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * Controlador de inicio y cierre de sesión.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Controller
@Publico
public class LoginControlador {

    private final UsuarioServicio usuarioServicio;

    /**
     * @param usuarioServicio servicio de usuarios inyectado por Spring
     */
    public LoginControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * GET /login: muestra el formulario de ingreso (o va al inicio si ya hay sesión).
     *
     * @param sesion sesión actual
     * @return vista {@code login} o redirección al inicio
     */
    @GetMapping("/login")
    public String mostrarLogin(HttpSession sesion) {
        return sesion.getAttribute(UsuarioSesion.ATRIBUTO) != null ? "redirect:/" : "login";
    }

    /**
     * POST /login: valida credenciales y guarda el usuario en la sesión.
     *
     * @param correo    correo digitado
     * @param clave     contraseña digitada (viaja en el cuerpo de la petición POST, no en la URL)
     * @param solicitud petición HTTP
     * @param modelo    modelo para la vista en caso de error
     * @return redirección al inicio o la vista {@code login} con el mensaje de error
     */
    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String correo, @RequestParam String clave,
                                HttpServletRequest solicitud, Model modelo) {
        try {
            UsuarioSesion usuario = UsuarioSesion.desde(usuarioServicio.autenticar(correo, clave));
            solicitud.getSession(true);
            solicitud.changeSessionId(); // nuevo id de sesión tras autenticarse (evita fijación de sesión)
            solicitud.getSession().setAttribute(UsuarioSesion.ATRIBUTO, usuario);
            return "redirect:/";
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            modelo.addAttribute("correo", correo);
            return "login";
        }
    }

    /**
     * POST /logout: invalida la sesión.
     *
     * @param sesion     sesión actual
     * @param atributos  mensajes flash que sobreviven a la redirección
     * @return redirección al formulario de ingreso
     */
    @PostMapping("/logout")
    public String cerrarSesion(HttpSession sesion, RedirectAttributes atributos) {
        sesion.invalidate();
        atributos.addFlashAttribute("exito", "Sesión cerrada correctamente");
        return "redirect:/login";
    }
}
