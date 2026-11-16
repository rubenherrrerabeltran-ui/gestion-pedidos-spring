package co.sena.gestionpedidos.soporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.sena.gestionpedidos.modelo.DetallePedido;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.repositorio.PedidoRepositorio;

/**
 * Doble de prueba (fake) del repositorio de pedidos: guarda los pedidos en una lista en memoria.
 * Permite probar los servicios de forma unitaria, sin base de datos ni contexto de Spring.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class PedidoRepositorioEnMemoria implements PedidoRepositorio {

    private final List<Pedido> pedidos = new ArrayList<>();
    private int consultas;

    /**
     * Agrega un pedido de prueba con sus detalles.
     *
     * @param id      identificador del pedido
     * @param cliente id y nombre del cliente, separados por dos puntos (por ejemplo "3:Carlos Ramírez")
     * @param fecha   fecha y hora del pedido
     * @param estado  estado del pedido
     * @param lineas  detalles en grupos de cuatro: idProducto, nombre, cantidad, precio unitario
     * @return el pedido agregado
     */
    public Pedido agregar(int id, String cliente, LocalDateTime fecha, EstadoPedido estado, Object... lineas) {
        String[] partes = cliente.split(":", 2);
        Pedido pedido = new Pedido(Integer.valueOf(partes[0]));
        pedido.setIdPedido(id);
        pedido.setNombreCliente(partes[1]);
        pedido.setFechaPedido(fecha);
        pedido.setEstado(estado);
        for (int i = 0; i < lineas.length; i += 4) {
            DetallePedido detalle = new DetallePedido((Integer) lineas[i], (Integer) lineas[i + 2]);
            detalle.setNombreProducto((String) lineas[i + 1]);
            detalle.setPrecioUnitario(new BigDecimal(String.valueOf(lineas[i + 3])));
            pedido.agregarDetalle(detalle);
        }
        pedidos.add(pedido);
        return pedido;
    }

    /**
     * @return número de veces que el servicio consultó el repositorio
     */
    public int getConsultas() {
        return consultas;
    }

    @Override
    public List<Pedido> listar(Integer idUsuario) {
        consultas++;
        return pedidos.stream().filter(p -> idUsuario == null || idUsuario.equals(p.getIdUsuario())).toList();
    }

    @Override
    public Optional<Pedido> buscarPorId(int idPedido) {
        return pedidos.stream().filter(p -> p.getIdPedido() == idPedido).findFirst();
    }

    @Override
    public Pedido insertar(Pedido pedido) {
        pedido.setIdPedido(pedidos.size() + 1);
        pedidos.add(pedido);
        return pedido;
    }

    @Override
    public boolean actualizarEstado(int idPedido, EstadoPedido nuevoEstado) {
        Optional<Pedido> pedido = buscarPorId(idPedido);
        pedido.ifPresent(p -> p.setEstado(nuevoEstado));
        return pedido.isPresent();
    }

    @Override
    public Map<EstadoPedido, Long> contarPorEstado() {
        Map<EstadoPedido, Long> conteo = new EnumMap<>(EstadoPedido.class);
        pedidos.forEach(p -> conteo.merge(p.getEstado(), 1L, Long::sum));
        return conteo;
    }
}
