package co.sena.gestionpedidos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.excepcion.RecursoNoEncontradoException;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.soporte.AplicacionPrueba;

/**
 * Pruebas del servicio de productos con el bean real de Spring y la base H2 en modo MySQL.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("ProductoServicio (bean de Spring + JdbcClient)")
class ProductoServicioTest {

    private ProductoServicio productoServicio;

    @BeforeEach
    void obtenerBean() {
        productoServicio = AplicacionPrueba.bean(ProductoServicio.class);
    }

    @Test
    @DisplayName("Spring inyecta el servicio y lista los productos cargados por data.sql")
    void listaLosProductosIniciales() {
        assertNotNull(productoServicio);
        assertTrue(productoServicio.listar(null).size() >= 5, "data.sql carga al menos 5 productos");
    }

    @Test
    @DisplayName("Guardar un producto nuevo le asigna el id generado por la base de datos")
    void guardarAsignaId() {
        Producto producto = productoServicio.guardar(new Producto("Bocadillo veleño x 12", new BigDecimal("8900"), 20));
        assertNotNull(producto.getIdProducto());
        assertEquals("Bocadillo veleño x 12", productoServicio.obtener(producto.getIdProducto()).getNombreProducto());
    }

    @Test
    @DisplayName("Actualizar modifica precio y stock")
    void actualizarModificaDatos() {
        Producto producto = productoServicio.guardar(
                new Producto("Avena en hojuelas 500 g", new BigDecimal("5200"), 12));
        producto.setPrecio(new BigDecimal("5500"));
        producto.setStock(30);
        productoServicio.guardar(producto);
        Producto leido = productoServicio.obtener(producto.getIdProducto());
        assertEquals(0, new BigDecimal("5500").compareTo(leido.getPrecio()));
        assertEquals(30, leido.getStock());
    }

    @Test
    @DisplayName("La búsqueda por texto no distingue mayúsculas")
    void buscarPorTexto() {
        assertTrue(productoServicio.listar("CAFÉ").stream()
                .allMatch(p -> p.getNombreProducto().toLowerCase().contains("café")));
        assertEquals(1, productoServicio.listar("panela").size());
    }

    @Test
    @DisplayName("Rechaza nombre vacío y precio negativo (NegocioException)")
    void validaDatos() {
        assertThrows(NegocioException.class, () -> productoServicio.guardar(new Producto(" ", BigDecimal.ONE, 1)));
        assertThrows(NegocioException.class,
                () -> productoServicio.guardar(new Producto("Producto X", new BigDecimal("-1"), 1)));
    }

    @Test
    @DisplayName("Consultar un id inexistente lanza RecursoNoEncontradoException")
    void obtenerInexistente() {
        assertThrows(RecursoNoEncontradoException.class, () -> productoServicio.obtener(99999));
    }

    @Test
    @DisplayName("No elimina un producto que ya está en pedidos (llave foránea)")
    void noEliminaProductoVendido() {
        NegocioException error = assertThrows(NegocioException.class, () -> productoServicio.eliminar(1));
        assertTrue(error.getMessage().contains("inactivo"));
    }

    @Test
    @DisplayName("Elimina un producto sin pedidos")
    void eliminaProductoSinPedidos() {
        Producto producto = productoServicio.guardar(new Producto("Producto temporal", BigDecimal.TEN, 1));
        productoServicio.eliminar(producto.getIdProducto());
        assertThrows(RecursoNoEncontradoException.class, () -> productoServicio.obtener(producto.getIdProducto()));
    }
}
