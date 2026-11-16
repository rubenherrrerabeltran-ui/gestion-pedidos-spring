package co.sena.gestionpedidos.web.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.servicio.PedidoServicio;
import co.sena.gestionpedidos.servicio.ProductoServicio;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;

/**
 * API de indicadores del tablero (requerimiento RF03) para el administrador y el vendedor.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RestController
@RequestMapping("/api/resumen")
@RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
public class ResumenApiControlador {

    private final ProductoServicio productoServicio;
    private final PedidoServicio pedidoServicio;
    private final UsuarioServicio usuarioServicio;

    /**
     * @param productoServicio servicio de productos
     * @param pedidoServicio   servicio de pedidos
     * @param usuarioServicio  servicio de usuarios
     */
    public ResumenApiControlador(ProductoServicio productoServicio, PedidoServicio pedidoServicio,
                                 UsuarioServicio usuarioServicio) {
        this.productoServicio = productoServicio;
        this.pedidoServicio = pedidoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * GET /api/resumen
     *
     * @return total de productos, productos con stock bajo, usuarios y pedidos por estado
     */
    @GetMapping
    public Map<String, Object> resumen() {
        Map<String, Long> pedidos = new LinkedHashMap<>();
        pedidoServicio.resumenPorEstado().forEach((estado, total) -> pedidos.put(estado.name(), total));
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("totalProductos", productoServicio.listar(null).size());
        respuesta.put("productosStockBajo", productoServicio.contarStockBajo());
        respuesta.put("totalUsuarios", usuarioServicio.contar());
        respuesta.put("pedidosPorEstado", pedidos);
        return respuesta;
    }
}
