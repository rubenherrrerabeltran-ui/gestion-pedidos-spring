package co.sena.gestionpedidos.web.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import co.sena.gestionpedidos.excepcion.AccesoDenegadoException;
import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.servicio.PedidoServicio;
import co.sena.gestionpedidos.web.api.dto.CambioEstadoSolicitud;
import co.sena.gestionpedidos.web.api.dto.PedidoRespuesta;
import co.sena.gestionpedidos.web.api.dto.PedidoSolicitud;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;
import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * API REST de pedidos (JSON). Requiere sesión; un CLIENTE solo accede a sus propios pedidos.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoApiControlador {

    private final PedidoServicio pedidoServicio;

    /**
     * @param pedidoServicio servicio de pedidos
     */
    public PedidoApiControlador(PedidoServicio pedidoServicio) {
        this.pedidoServicio = pedidoServicio;
    }

    /**
     * GET /api/pedidos
     *
     * @param usuario usuario autenticado
     * @return pedidos visibles para el usuario
     */
    @GetMapping
    public List<PedidoRespuesta> listar(@SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario) {
        return pedidoServicio.listar(usuario.esCliente() ? usuario.idUsuario() : null).stream()
                .map(PedidoRespuesta::desde)
                .toList();
    }

    /**
     * GET /api/pedidos/{id}
     *
     * @param id      pedido
     * @param usuario usuario autenticado
     * @return el pedido con detalles y total
     */
    @GetMapping("/{id}")
    public PedidoRespuesta obtener(@PathVariable int id,
                                   @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario) {
        Pedido pedido = pedidoServicio.obtener(id);
        if (usuario.esCliente() && pedido.getIdUsuario() != usuario.idUsuario()) {
            throw new AccesoDenegadoException("El pedido #" + id + " pertenece a otro cliente");
        }
        return PedidoRespuesta.desde(pedido);
    }

    /**
     * POST /api/pedidos
     *
     * @param solicitud cliente e ítems en JSON
     * @param usuario   usuario autenticado
     * @return 201 Created con el pedido registrado
     */
    @PostMapping
    public ResponseEntity<PedidoRespuesta> crear(@RequestBody PedidoSolicitud solicitud,
                                                 @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario) {
        Integer idCliente = usuario.esCliente() ? Integer.valueOf(usuario.idUsuario()) : solicitud.idCliente();
        if (idCliente == null) {
            throw new NegocioException("Indique idCliente");
        }
        Pedido pedido = pedidoServicio.registrar(idCliente, solicitud.cantidadesPorProducto());
        return ResponseEntity.created(URI.create("/api/pedidos/" + pedido.getIdPedido()))
                .body(PedidoRespuesta.desde(pedido));
    }

    /**
     * PATCH /api/pedidos/{id}/estado
     *
     * @param id        pedido
     * @param solicitud estado destino en JSON
     * @return el pedido actualizado
     */
    @PatchMapping("/{id}/estado")
    @RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
    public PedidoRespuesta cambiarEstado(@PathVariable int id, @RequestBody CambioEstadoSolicitud solicitud) {
        EstadoPedido nuevoEstado;
        try {
            nuevoEstado = EstadoPedido.valueOf(String.valueOf(solicitud.estado()).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NegocioException("Estado no válido: " + solicitud.estado());
        }
        return PedidoRespuesta.desde(pedidoServicio.cambiarEstado(id, nuevoEstado));
    }
}
