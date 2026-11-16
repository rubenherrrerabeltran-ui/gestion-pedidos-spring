package co.sena.gestionpedidos.web.api.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo JSON para registrar un pedido en la API.
 * Ejemplo: {@code {"idCliente":3,"items":[{"idProducto":1,"cantidad":2}]}}
 *
 * @param idCliente cliente del pedido (se ignora si quien llama es un CLIENTE: se usa su propio id)
 * @param items     productos y cantidades
 * @author Rubén Darío Herrera Beltrán
 */
public record PedidoSolicitud(Integer idCliente, List<Item> items) {

    /**
     * Producto y cantidad dentro de la solicitud.
     *
     * @param idProducto producto solicitado
     * @param cantidad   unidades
     */
    public record Item(Integer idProducto, Integer cantidad) {
    }

    /**
     * @return mapa idProducto → cantidad (si se repite un producto, se suman las cantidades)
     */
    public Map<Integer, Integer> cantidadesPorProducto() {
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        if (items != null) {
            items.forEach(item -> cantidades.merge(item.idProducto(), item.cantidad(), Integer::sum));
        }
        return cantidades;
    }
}
