package co.sena.gestionpedidos.web.api.dto;

import java.math.BigDecimal;

import co.sena.gestionpedidos.modelo.Producto;

/**
 * Cuerpo JSON para crear o actualizar un producto en la API.
 * Ejemplo: {@code {"nombreProducto":"Café 250 g","precio":15000,"stock":30,"activo":true}}
 *
 * @param nombreProducto nombre del producto
 * @param precio         precio de venta
 * @param stock          unidades disponibles
 * @param activo         si se puede vender (por defecto {@code true})
 * @author Rubén Darío Herrera Beltrán
 */
public record ProductoSolicitud(String nombreProducto, BigDecimal precio, Integer stock, Boolean activo) {

    /**
     * @param idProducto {@code null} para crear o el id para actualizar
     * @return entidad lista para el servicio
     */
    public Producto aProducto(Integer idProducto) {
        Producto producto = new Producto(nombreProducto, precio, stock == null ? 0 : stock);
        producto.setIdProducto(idProducto);
        producto.setActivo(activo == null || activo);
        return producto;
    }
}
