package co.sena.gestionpedidos.web.controlador;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.servicio.ProductoServicio;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;

/**
 * Controlador MVC del módulo de productos (CRUD con vistas Thymeleaf).
 * GET se usa para consultar y mostrar formularios; POST para guardar y eliminar,
 * seguido de redirección (patrón Post/Redirect/Get).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Controller
@RequestMapping("/productos")
public class ProductoControlador {

    private static final String VISTA_FORMULARIO = "productos/formulario";

    private final ProductoServicio productoServicio;

    /**
     * @param productoServicio servicio inyectado por Spring
     */
    public ProductoControlador(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    /**
     * GET /productos?texto=...&amp;filtro=...: lista, busca y filtra productos (todos los roles).
     *
     * @param texto  filtro opcional por nombre
     * @param filtro todos, activos, inactivos o stock-bajo
     * @param modelo modelo para la vista
     * @return vista de la lista
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String texto,
                         @RequestParam(required = false, defaultValue = "todos") String filtro, Model modelo) {
        try {
            modelo.addAttribute("productos", productoServicio.listar(texto, filtro));
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            modelo.addAttribute("productos", List.of());
        }
        modelo.addAttribute("texto", texto);
        modelo.addAttribute("filtro", filtro);
        modelo.addAttribute("umbralStockBajo", ProductoServicio.UMBRAL_STOCK_BAJO);
        return "productos/lista";
    }

    /**
     * GET /productos/nuevo: formulario vacío.
     *
     * @param modelo modelo para la vista
     * @return vista del formulario
     */
    @GetMapping("/nuevo")
    @RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
    public String nuevo(Model modelo) {
        modelo.addAttribute("producto", new Producto());
        return VISTA_FORMULARIO;
    }

    /**
     * GET /productos/{id}/editar: formulario con los datos del producto.
     *
     * @param id     identificador tomado de la ruta
     * @param modelo modelo para la vista
     * @return vista del formulario
     */
    @GetMapping("/{id}/editar")
    @RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
    public String editar(@PathVariable int id, Model modelo) {
        modelo.addAttribute("producto", productoServicio.obtener(id));
        return VISTA_FORMULARIO;
    }

    /**
     * POST /productos/guardar: Spring enlaza los campos del formulario al objeto {@link Producto}.
     *
     * @param producto   datos enviados
     * @param enlace     resultado del enlace de datos (errores de conversión, p. ej. texto en el precio)
     * @param modelo     modelo para volver a mostrar el formulario si hay errores
     * @param atributos  mensajes flash
     * @return redirección a la lista o el formulario con el error
     */
    @PostMapping("/guardar")
    @RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
    public String guardar(@ModelAttribute Producto producto, BindingResult enlace, Model modelo,
                          RedirectAttributes atributos) {
        if (enlace.hasErrors()) {
            modelo.addAttribute("error", "Revise el precio y el stock: deben ser valores numéricos");
            return VISTA_FORMULARIO;
        }
        try {
            boolean esNuevo = producto.getIdProducto() == null;
            Producto guardado = productoServicio.guardar(producto);
            atributos.addFlashAttribute("exito", "Producto \"" + guardado.getNombreProducto() + "\" "
                    + (esNuevo ? "registrado con id " + guardado.getIdProducto() : "actualizado"));
            return "redirect:/productos";
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            return VISTA_FORMULARIO;
        }
    }

    /**
     * POST /productos/{id}/eliminar: solo el administrador elimina productos.
     *
     * @param id        identificador
     * @param atributos mensajes flash
     * @return redirección a la lista
     */
    @PostMapping("/{id}/eliminar")
    @RequiereRol(Rol.ADMINISTRADOR)
    public String eliminar(@PathVariable int id, RedirectAttributes atributos) {
        try {
            productoServicio.eliminar(id);
            atributos.addFlashAttribute("exito", "Producto " + id + " eliminado");
        } catch (NegocioException e) {
            atributos.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }
}
