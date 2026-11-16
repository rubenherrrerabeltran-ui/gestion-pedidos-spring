package co.sena.gestionpedidos.servicio;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.excepcion.RecursoNoEncontradoException;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.repositorio.ProductoRepositorio;
import co.sena.gestionpedidos.util.Validador;

/**
 * Reglas de negocio del módulo de productos (inventario).
 * Es un bean de Spring ({@code @Service}) que recibe el repositorio por inyección de dependencias.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Service
@Transactional(readOnly = true)
public class ProductoServicio {

    /** Por debajo de esta cantidad el inventario se resalta como "stock bajo". */
    public static final int UMBRAL_STOCK_BAJO = 10;

    private final ProductoRepositorio productoRepositorio;

    /**
     * @param productoRepositorio repositorio inyectado por Spring
     */
    public ProductoServicio(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    /**
     * @param texto filtro opcional por nombre
     * @return productos que coinciden con el filtro
     */
    public List<Producto> listar(String texto) {
        return productoRepositorio.listar(texto);
    }

    /**
     * Consulta con búsqueda por nombre y filtro por estado del inventario (requerimiento RF04).
     *
     * @param texto  filtro opcional por nombre
     * @param filtro {@code todos}, {@code activos}, {@code inactivos} o {@code stock-bajo}
     * @return productos que cumplen ambos criterios
     * @throws NegocioException si el filtro no existe
     */
    public List<Producto> listar(String texto, String filtro) {
        if (texto != null && texto.length() > 120) {
            throw new NegocioException("El texto de búsqueda admite máximo 120 caracteres");
        }
        Predicate<Producto> condicion = switch (filtro == null || filtro.isBlank() ? "todos" : filtro) {
            case "todos" -> producto -> true;
            case "activos" -> Producto::isActivo;
            case "inactivos" -> producto -> !producto.isActivo();
            case "stock-bajo" -> producto -> producto.isActivo() && producto.getStock() < UMBRAL_STOCK_BAJO;
            default -> throw new NegocioException("El filtro \"" + filtro + "\" no existe");
        };
        return productoRepositorio.listar(texto).stream().filter(condicion).toList();
    }

    /**
     * @return productos activos con stock, para armar un pedido
     */
    public List<Producto> listarDisponibles() {
        return productoRepositorio.listarDisponibles();
    }

    /**
     * @param idProducto identificador
     * @return el producto
     * @throws RecursoNoEncontradoException si no existe
     */
    public Producto obtener(int idProducto) {
        return productoRepositorio.buscarPorId(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", idProducto));
    }

    /**
     * Valida y guarda (crea o actualiza) un producto.
     *
     * @param producto datos capturados en el formulario o recibidos por la API
     * @return el producto guardado con su id
     */
    @Transactional
    public Producto guardar(Producto producto) {
        producto.setNombreProducto(Validador.validarNombreProducto(producto.getNombreProducto()));
        Validador.validarPrecio(producto.getPrecio());
        Validador.validarStock(producto.getStock());
        if (producto.getIdProducto() != null) {
            obtener(producto.getIdProducto()); // verifica que exista antes de actualizar
        }
        return productoRepositorio.guardar(producto);
    }

    /**
     * Elimina un producto. Si ya fue vendido (tiene detalles de pedido) la base de datos lo impide
     * por la llave foránea, y se informa al usuario que lo desactive en su lugar.
     *
     * @param idProducto identificador
     */
    @Transactional
    public void eliminar(int idProducto) {
        obtener(idProducto);
        try {
            productoRepositorio.eliminar(idProducto);
        } catch (DataIntegrityViolationException e) {
            throw new NegocioException("El producto " + idProducto
                    + " ya está en pedidos registrados; en lugar de eliminarlo, márquelo como inactivo");
        }
    }

    /**
     * @return cantidad de productos activos con stock bajo
     */
    public long contarStockBajo() {
        return productoRepositorio.contarStockBajo(UMBRAL_STOCK_BAJO);
    }
}
