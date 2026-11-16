package co.sena.gestionpedidos.repositorio;

import java.util.List;
import java.util.Optional;

import co.sena.gestionpedidos.modelo.Producto;

/**
 * Contrato de acceso a datos de la tabla {@code producto}.
 * La capa de servicio depende de esta interfaz y no de la tecnología concreta (JDBC).
 *
 * @author Rubén Darío Herrera Beltrán
 */
public interface ProductoRepositorio {

    /**
     * @param texto filtro por nombre (contiene, sin distinguir mayúsculas); vacío o {@code null} lista todos
     * @return productos ordenados por nombre
     */
    List<Producto> listar(String texto);

    /**
     * @return solo los productos activos con unidades disponibles (catálogo para pedidos)
     */
    List<Producto> listarDisponibles();

    /**
     * @param idProducto identificador del producto
     * @return el producto, o vacío si no existe
     */
    Optional<Producto> buscarPorId(int idProducto);

    /**
     * Inserta el producto si no tiene id, o lo actualiza si ya lo tiene.
     *
     * @param producto datos a guardar
     * @return el producto con su identificador asignado
     */
    Producto guardar(Producto producto);

    /**
     * @param idProducto identificador del producto
     * @return {@code true} si se eliminó una fila
     */
    boolean eliminar(int idProducto);

    /**
     * Resta unidades solo si hay stock suficiente (operación atómica en una sola sentencia UPDATE).
     *
     * @param idProducto producto a descontar
     * @param cantidad   unidades vendidas
     * @return {@code true} si había stock y se descontó
     */
    boolean descontarStock(int idProducto, int cantidad);

    /**
     * @param idProducto producto al que se devuelven unidades (pedido cancelado)
     * @param cantidad   unidades devueltas
     */
    void devolverStock(int idProducto, int cantidad);

    /**
     * @param umbral cantidad mínima esperada
     * @return número de productos activos con stock menor al umbral
     */
    long contarStockBajo(int umbral);
}
