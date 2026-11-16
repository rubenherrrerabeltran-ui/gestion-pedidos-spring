package co.sena.gestionpedidos.web.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.formulario.UsuarioFormulario;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;
import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * Controlador MVC del módulo de usuarios. Toda la clase está restringida al ADMINISTRADOR.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Controller
@RequestMapping("/usuarios")
@RequiereRol(Rol.ADMINISTRADOR)
public class UsuarioControlador {

    private static final String VISTA_FORMULARIO = "usuarios/formulario";

    private final UsuarioServicio usuarioServicio;

    /**
     * @param usuarioServicio servicio inyectado por Spring
     */
    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * Agrega la lista de roles a todos los modelos de este controlador (para el campo select).
     *
     * @return roles disponibles
     */
    @ModelAttribute("roles")
    public Rol[] roles() {
        return Rol.values();
    }

    /**
     * GET /usuarios.
     *
     * @param modelo modelo para la vista
     * @return vista de la lista
     */
    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("usuarios", usuarioServicio.listar());
        return "usuarios/lista";
    }

    /**
     * GET /usuarios/nuevo.
     *
     * @param modelo modelo para la vista
     * @return vista del formulario
     */
    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("usuario", new UsuarioFormulario());
        return VISTA_FORMULARIO;
    }

    /**
     * GET /usuarios/{id}/editar.
     *
     * @param id     identificador
     * @param modelo modelo para la vista
     * @return vista del formulario
     */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model modelo) {
        modelo.addAttribute("usuario", UsuarioFormulario.desde(usuarioServicio.obtener(id)));
        return VISTA_FORMULARIO;
    }

    /**
     * POST /usuarios/guardar.
     *
     * @param formulario datos enviados (la contraseña viaja en el cuerpo POST)
     * @param enlace     errores de conversión (por ejemplo, un rol que no existe)
     * @param modelo     modelo para volver a mostrar el formulario
     * @param atributos  mensajes flash
     * @return redirección a la lista o el formulario con el error
     */
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("usuario") UsuarioFormulario formulario, BindingResult enlace,
                          Model modelo, RedirectAttributes atributos) {
        if (enlace.hasErrors()) {
            formulario.setClave(null);
            modelo.addAttribute("error", "El rol seleccionado no es válido");
            return VISTA_FORMULARIO;
        }
        try {
            Usuario guardado = usuarioServicio.guardar(formulario.getIdUsuario(), formulario.aDatos());
            atributos.addFlashAttribute("exito", "Usuario " + guardado.getCorreo() + " guardado");
            return "redirect:/usuarios";
        } catch (NegocioException e) {
            formulario.setClave(null); // la contraseña no se devuelve a la vista
            modelo.addAttribute("error", e.getMessage());
            return VISTA_FORMULARIO;
        }
    }

    /**
     * POST /usuarios/{id}/eliminar.
     *
     * @param id        usuario a eliminar
     * @param actual    usuario con la sesión abierta
     * @param atributos mensajes flash
     * @return redirección a la lista
     */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion actual,
                           RedirectAttributes atributos) {
        try {
            usuarioServicio.eliminar(id, actual.idUsuario());
            atributos.addFlashAttribute("exito", "Usuario " + id + " eliminado");
        } catch (NegocioException e) {
            atributos.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
