package co.sena.gestionpedidos.servicio;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.excepcion.RecursoNoEncontradoException;
import co.sena.gestionpedidos.modelo.DetallePedido;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.repositorio.PedidoRepositorio;
import co.sena.gestionpedidos.repositorio.ProductoRepositorio;
import co.sena.gestionpedidos.repositorio.UsuarioRepositorio;

/**
 * Reglas de negocio de pedidos: registro con control de inventario, consulta y cambio de estado.
 *
 * <p>El registro y la cancelación son <strong>transaccionales</strong> ({@code @Transactional}):
 * si un producto no tiene stock suficiente se lanza una excepción y Spring revierte (rollback)
 * todo lo hecho: no queda el pedido a medias ni stock descontado.</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Service
@Transactional(readOnly = true)
public class PedidoServicio {

    private final PedidoRepositorio pedidoRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    /**
     * @param pedidoRepositorio   repositorio de pedidos
     * @param productoRepositorio repositorio de productos (precios y stock)
     * @param usuarioRepositorio  repositorio de usuarios (validar el cliente)
     */
    public PedidoServicio(PedidoRepositorio pedidoRepositorio, ProductoRepositorio productoRepositorio,
                          UsuarioRepositorio usuarioRepositorio) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    /**
     * @param idCliente si no es {@code null}, limita la consulta a los pedidos de ese cliente
     * @return pedidos con sus detalles
     */
    public List<Pedido> listar(Integer idCliente) {
        return pedidoRepositorio.listar(idCliente);
    }

    /**
     * @param idPedido identificador
     * @return el pedido con sus detalles
     * @throws RecursoNoEncontradoException si no existe
     */
    public Pedido obtener(int idPedido) {
        return pedidoRepositorio.buscarPorId(idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", idPedido));
    }

    /**
     * Registra un pedido en estado PENDIENTE, fija el precio vigente de cada producto y descuenta el stock.
     *
     * @param idCliente           usuario con rol CLIENTE dueño del pedido
     * @param cantidadPorProducto mapa idProducto → cantidad; las cantidades en cero se ignoran
     * @return el pedido registrado
     * @throws NegocioException si el cliente no es válido, no hay productos o falta stock
     */
    @Transactional
    public Pedido registrar(int idCliente, Map<Integer, Integer> cantidadPorProducto) {
        Usuario cliente = usuarioRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", idCliente));
        if (cliente.getRol() != Rol.CLIENTE) {
            throw new NegocioException("Los pedidos se registran a nombre de un usuario con rol CLIENTE");
        }
        Pedido pedido = new Pedido(idCliente);
        cantidadPorProducto.forEach((idProducto, cantidad) -> {
            if (cantidad == null || cantidad == 0) {
                return; // el producto no se seleccionó en el formulario
            }
            if (cantidad < 0) {
                throw new NegocioException("La cantidad no puede ser negativa");
            }
            Producto producto = productoRepositorio.buscarPorId(idProducto)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", idProducto));
            // UPDATE condicionado (stock >= cantidad): evita vender más de lo que hay
            if (!productoRepositorio.descontarStock(idProducto, cantidad)) {
                throw new NegocioException("Stock insuficiente para " + producto.getNombreProducto()
                        + " (disponible: " + producto.getStock() + ")");
            }
            DetallePedido detalle = new DetallePedido(idProducto, cantidad);
            detalle.setNombreProducto(producto.getNombreProducto());
            detalle.setPrecioUnitario(producto.getPrecio());
            pedido.agregarDetalle(detalle);
        });
        if (pedido.getDetalles().isEmpty()) {
            throw new NegocioException("Seleccione al menos un producto con cantidad mayor que cero");
        }
        pedidoRepositorio.insertar(pedido);
        return obtener(pedido.getIdPedido());
    }

    /**
     * Cambia el estado respetando las transiciones de {@link EstadoPedido}.
     * Al cancelar, las unidades vuelven al inventario.
     *
     * @param idPedido    pedido a modificar
     * @param nuevoEstado estado destino
     * @return el pedido actualizado
     */
    @Transactional
    public Pedido cambiarEstado(int idPedido, EstadoPedido nuevoEstado) {
        Pedido pedido = obtener(idPedido);
        if (!pedido.getEstado().puedeCambiarA(nuevoEstado)) {
            throw new NegocioException("No se puede pasar el pedido " + idPedido + " de "
                    + pedido.getEstado() + " a " + nuevoEstado);
        }
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            pedido.getDetalles().forEach(detalle ->
                    productoRepositorio.devolverStock(detalle.getIdProducto(), detalle.getCantidad()));
        }
        pedidoRepositorio.actualizarEstado(idPedido, nuevoEstado);
        return obtener(idPedido);
    }

    /**
     * @return número de pedidos por estado, para el tablero de inicio
     */
    public Map<EstadoPedido, Long> resumenPorEstado() {
        return pedidoRepositorio.contarPorEstado();
    }
}
