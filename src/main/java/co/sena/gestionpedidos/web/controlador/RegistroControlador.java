package co.sena.gestionpedidos.web.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.formulario.UsuarioFormulario;
import co.sena.gestionpedidos.web.seguridad.Publico;

/**
 * Registro público de clientes (requerimiento RF02): una persona sin cuenta crea su usuario
 * con rol CLIENTE y luego inicia sesión para hacer pedidos.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Controller
@Publico
@RequestMapping("/registro")
public class RegistroControlador {

    private static final String VISTA = "registro";

    private final UsuarioServicio usuarioServicio;

    /**
     * @param usuarioServicio servicio de usuarios inyectado por Spring
     */
    public RegistroControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * GET /registro: formulario vacío.
     *
     * @param modelo modelo para la vista
     * @return vista {@code registro}
     */
    @GetMapping
    public String mostrar(Model modelo) {
        modelo.addAttribute("cliente", new UsuarioFormulario());
        return VISTA;
    }

    /**
     * POST /registro: valida los datos y crea la cuenta de cliente.
     *
     * @param formulario datos digitados
     * @param modelo     modelo para volver a mostrar el formulario con el error
     * @param atributos  mensaje flash para la pantalla de ingreso
     * @return redirección a /login o el formulario con el error
     */
    @PostMapping
    public String registrar(@ModelAttribute("cliente") UsuarioFormulario formulario, Model modelo,
                            RedirectAttributes atributos) {
        try {
            Usuario cliente = usuarioServicio.registrarCliente(formulario.aDatos());
            atributos.addFlashAttribute("exito", "Cuenta creada para " + cliente.getCorreo() + ". Ya puede ingresar");
            return "redirect:/login";
        } catch (NegocioException e) {
            formulario.setClave(null); // la contraseña nunca se devuelve a la vista
            modelo.addAttribute("error", e.getMessage());
            return VISTA;
        }
    }
}
