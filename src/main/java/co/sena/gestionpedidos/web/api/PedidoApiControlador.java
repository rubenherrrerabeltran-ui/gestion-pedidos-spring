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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import co.sena.gestionpedidos.excepcion.AccesoDenegadoException;
import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.servicio.PedidoServicio;
import co.sena.gestionpedidos.util.Validador;
import co.sena.gestionpedidos.web.api.dto.CambioEstadoSolicitud;
import co.sena.gestionpedidos.web.api.dto.PedidoRespuesta;
import co.sena.gestionpedidos.web.api.dto.PedidoSolicitud;
import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * API REST de pedidos (JSON). Requiere sesión; un CLIENTE solo consulta, registra y cancela sus propios pedidos.
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
     * GET /api/pedidos?cliente=...&amp;estado=...&amp;desde=AAAA-MM-DD&amp;hasta=AAAA-MM-DD
     *
     * @param cliente parte del nombre del cliente (se ignora si quien consulta es un CLIENTE)
     * @param estado  estado buscado (opcional)
     * @param desde   fecha inicial (opcional)
     * @param hasta   fecha final (opcional)
     * @param usuario usuario autenticado
     * @return pedidos visibles para el usuario que cumplen los filtros
     */
    @GetMapping
    public List<PedidoRespuesta> listar(@RequestParam(required = false) String cliente,
                                        @RequestParam(required = false) String estado,
                                        @RequestParam(required = false) String desde,
                                        @RequestParam(required = false) String hasta,
                                        @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario) {
        return pedidoServicio.buscar(usuario.esCliente() ? usuario.idUsuario() : null,
                        usuario.esCliente() ? null : cliente, Validador.convertirEstado(estado),
                        Validador.convertirFecha(desde, "inicial"), Validador.convertirFecha(hasta, "final"))
                .stream()
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
     * PATCH /api/pedidos/{id}/estado. El administrador y el vendedor aplican cualquier transición permitida;
     * el cliente solo puede cancelar sus propios pedidos.
     *
     * @param id        pedido
     * @param solicitud estado destino en JSON
     * @param usuario   usuario autenticado
     * @return el pedido actualizado
     */
    @PatchMapping("/{id}/estado")
    public PedidoRespuesta cambiarEstado(@PathVariable int id, @RequestBody CambioEstadoSolicitud solicitud,
                                         @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario) {
        EstadoPedido nuevoEstado = Validador.convertirEstado(solicitud.estado());
        if (nuevoEstado == null) {
            throw new NegocioException("Indique el estado destino");
        }
        if (usuario.esCliente()) {
            Pedido pedido = pedidoServicio.obtener(id);
            if (pedido.getIdUsuario() != usuario.idUsuario() || nuevoEstado != EstadoPedido.CANCELADO) {
                throw new AccesoDenegadoException("Un cliente solo puede cancelar sus propios pedidos");
            }
        }
        return PedidoRespuesta.desde(pedidoServicio.cambiarEstado(id, nuevoEstado));
    }
}
