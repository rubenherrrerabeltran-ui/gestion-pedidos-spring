package co.sena.gestionpedidos.web.controlador;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import co.sena.gestionpedidos.servicio.PedidoServicio;
import co.sena.gestionpedidos.servicio.ProductoServicio;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * Tablero de inicio con indicadores del sistema según el rol del usuario.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Controller
public class InicioControlador {

    private final PedidoServicio pedidoServicio;
    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;

    /**
     * @param pedidoServicio   servicio de pedidos
     * @param productoServicio servicio de productos
     * @param usuarioServicio  servicio de usuarios
     */
    public InicioControlador(PedidoServicio pedidoServicio, ProductoServicio productoServicio,
                             UsuarioServicio usuarioServicio) {
        this.pedidoServicio = pedidoServicio;
        this.productoServicio = productoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * GET /: carga los indicadores y muestra la vista {@code inicio}.
     *
     * @param usuario usuario autenticado (atributo de sesión)
     * @param modelo  modelo para la vista
     * @return nombre de la plantilla Thymeleaf
     */
    @GetMapping({"/", "/inicio"})
    public String inicio(@SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario, Model modelo) {
        // Mapa con el nombre del estado como clave para leerlo fácilmente en Thymeleaf: ${conteoPedidos['PENDIENTE']}
        Map<String, Long> conteoPedidos = new LinkedHashMap<>();
        pedidoServicio.resumenPorEstado().forEach((estado, total) -> conteoPedidos.put(estado.name(), total));
        modelo.addAttribute("conteoPedidos", conteoPedidos);
        modelo.addAttribute("productosStockBajo", productoServicio.contarStockBajo());
        modelo.addAttribute("totalProductos", productoServicio.listar(null).size());
        modelo.addAttribute("totalUsuarios", usuarioServicio.contar());
        modelo.addAttribute("ultimosPedidos",
                pedidoServicio.listar(usuario.esCliente() ? usuario.idUsuario() : null).stream().limit(5).toList());
        return "inicio";
    }
}
