package co.sena.gestionpedidos.web.controlador;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sena.gestionpedidos.excepcion.AccesoDenegadoException;
import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.servicio.PedidoServicio;
import co.sena.gestionpedidos.servicio.ProductoServicio;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.util.Validador;
import co.sena.gestionpedidos.web.formulario.PedidoFormulario;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;
import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * Controlador MVC del módulo de pedidos.
 * El CLIENTE solo ve y registra sus propios pedidos; ADMINISTRADOR y VENDEDOR gestionan todos.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Controller
@RequestMapping("/pedidos")
public class PedidoControlador {

    private static final String VISTA_FORMULARIO = "pedidos/formulario";

    private final PedidoServicio pedidoServicio;
    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;

    /**
     * @param pedidoServicio   servicio de pedidos
     * @param productoServicio servicio de productos (catálogo disponible)
     * @param usuarioServicio  servicio de usuarios (lista de clientes)
     */
    public PedidoControlador(PedidoServicio pedidoServicio, ProductoServicio productoServicio,
                             UsuarioServicio usuarioServicio) {
        this.pedidoServicio = pedidoServicio;
        this.productoServicio = productoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * GET /pedidos?cliente=...&amp;estado=...&amp;desde=AAAA-MM-DD&amp;hasta=AAAA-MM-DD (requerimiento RF06).
     * Las fechas llegan como texto y se validan aquí para mostrar un mensaje claro si no son válidas.
     *
     * @param cliente parte del nombre del cliente (solo para administrador y vendedor)
     * @param estado  estado buscado (vacío = todos)
     * @param desde   fecha inicial
     * @param hasta   fecha final
     * @param usuario usuario autenticado
     * @param modelo  modelo para la vista
     * @return vista de la lista
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String cliente,
                         @RequestParam(required = false) String estado,
                         @RequestParam(required = false) String desde,
                         @RequestParam(required = false) String hasta,
                         @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario, Model modelo) {
        modelo.addAttribute("estados", EstadoPedido.values());
        modelo.addAttribute("hoy", LocalDate.now());
        try {
            EstadoPedido estadoBuscado = Validador.convertirEstado(estado);
            LocalDate fechaDesde = Validador.convertirFecha(desde, "inicial");
            LocalDate fechaHasta = Validador.convertirFecha(hasta, "final");
            modelo.addAttribute("pedidos", pedidoServicio.buscar(usuario.esCliente() ? usuario.idUsuario() : null,
                    usuario.esCliente() ? null : cliente, estadoBuscado, fechaDesde, fechaHasta));
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            modelo.addAttribute("pedidos", List.of());
        }
        return "pedidos/lista";
    }

    /**
     * GET /pedidos/nuevo: formulario con los productos disponibles.
     *
     * @param modelo modelo para la vista
     * @return vista del formulario
     */
    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        return mostrarFormulario(new PedidoFormulario(), modelo);
    }

    /**
     * POST /pedidos/guardar: registra el pedido (transacción en el servicio).
     *
     * @param formulario cliente y cantidades por producto
     * @param enlace     errores de conversión (cantidades con decimales o texto)
     * @param usuario    usuario autenticado
     * @param modelo     modelo para volver a mostrar el formulario
     * @param atributos  mensajes flash
     * @return redirección al detalle del pedido o el formulario con el error
     */
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("pedido") PedidoFormulario formulario, BindingResult enlace,
                          @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario, Model modelo,
                          RedirectAttributes atributos) {
        if (enlace.hasErrors()) {
            modelo.addAttribute("error", "Las cantidades deben ser números enteros (sin decimales ni letras)");
            return mostrarFormulario(new PedidoFormulario(), modelo);
        }
        // Un cliente siempre registra a su nombre, aunque se altere el campo en el navegador
        Integer idCliente = usuario.esCliente() ? Integer.valueOf(usuario.idUsuario()) : formulario.getIdCliente();
        try {
            if (idCliente == null) {
                throw new NegocioException("Seleccione el cliente del pedido");
            }
            Pedido pedido = pedidoServicio.registrar(idCliente, formulario.getCantidades());
            atributos.addFlashAttribute("exito", "Pedido #" + pedido.getIdPedido() + " registrado");
            return "redirect:/pedidos/" + pedido.getIdPedido();
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            return mostrarFormulario(formulario, modelo);
        }
    }

    /**
     * GET /pedidos/{id}: detalle del pedido.
     *
     * @param id      identificador
     * @param usuario usuario autenticado
     * @param modelo  modelo para la vista
     * @return vista de detalle
     */
    @GetMapping("/{id}")
    public String detalle(@PathVariable int id, @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                          Model modelo) {
        Pedido pedido = obtenerConPermiso(id, usuario);
        modelo.addAttribute("pedido", pedido);
        modelo.addAttribute("estadosSiguientes", Arrays.stream(EstadoPedido.values())
                .filter(pedido.getEstado()::puedeCambiarA).toList());
        return "pedidos/detalle";
    }

    /**
     * POST /pedidos/{id}/estado: cambia el estado (pagado, enviado, cancelado).
     *
     * @param id          pedido
     * @param nuevoEstado estado destino
     * @param atributos   mensajes flash
     * @return redirección al detalle
     */
    @PostMapping("/{id}/estado")
    @RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
    public String cambiarEstado(@PathVariable int id, @RequestParam EstadoPedido nuevoEstado,
                                RedirectAttributes atributos) {
        try {
            pedidoServicio.cambiarEstado(id, nuevoEstado);
            atributos.addFlashAttribute("exito", "El pedido #" + id + " pasó a " + nuevoEstado);
        } catch (NegocioException e) {
            atributos.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    /**
     * POST /pedidos/{id}/cancelar: el cliente puede cancelar su propio pedido pendiente.
     *
     * @param id        pedido
     * @param usuario   usuario autenticado
     * @param atributos mensajes flash
     * @return redirección al detalle
     */
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable int id, @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                           RedirectAttributes atributos) {
        obtenerConPermiso(id, usuario);
        try {
            pedidoServicio.cambiarEstado(id, EstadoPedido.CANCELADO);
            atributos.addFlashAttribute("exito", "Pedido #" + id + " cancelado; las unidades volvieron al inventario");
        } catch (NegocioException e) {
            atributos.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    private String mostrarFormulario(PedidoFormulario formulario, Model modelo) {
        modelo.addAttribute("pedido", formulario);
        modelo.addAttribute("productos", productoServicio.listarDisponibles());
        modelo.addAttribute("clientes", usuarioServicio.listarClientes());
        return VISTA_FORMULARIO;
    }

    /** Un cliente solo puede consultar o cancelar sus propios pedidos. */
    private Pedido obtenerConPermiso(int idPedido, UsuarioSesion usuario) {
        Pedido pedido = pedidoServicio.obtener(idPedido);
        if (usuario.esCliente() && pedido.getIdUsuario() != usuario.idUsuario()) {
            throw new AccesoDenegadoException("El pedido #" + idPedido + " pertenece a otro cliente");
        }
        return pedido;
    }
}
