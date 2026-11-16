package co.sena.gestionpedidos.movil.dominio;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import co.sena.gestionpedidos.movil.dominio.carrito.Carrito;
import co.sena.gestionpedidos.movil.dominio.modelo.Producto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Pruebas unitarias del carrito: stock, cantidades e IVA.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class CarritoTest {

    private Carrito carrito;
    private Producto queso;
    private Producto chocolate;

    /** Crea los objetos de prueba antes de cada caso. */
    @Before
    public void preparar() {
        carrito = new Carrito();
        queso = new Producto(5, "Queso campesino 500 g", new BigDecimal("14500"), 2, true);
        chocolate = new Producto(3, "Chocolate de mesa 250 g", new BigDecimal("9800"), 25, true);
    }

    /** Verifica que calcula subtotal, IVA y total como el servidor. */
    @Test
    public void calculaSubtotalIvaYTotalComoElServidor() {
        carrito.agregar(chocolate);
        carrito.agregar(queso);
        carrito.agregar(queso);
        assertEquals(3, carrito.getUnidades());
        assertEquals(0, new BigDecimal("38800").compareTo(carrito.getSubtotal()));
        assertEquals(0, new BigDecimal("7372").compareTo(carrito.getIva()));
        assertEquals(0, new BigDecimal("46172").compareTo(carrito.getTotal()));
    }

    /** Verifica que no permite superar el stock. */
    @Test
    public void noPermiteSuperarElStock() {
        assertTrue(carrito.agregar(queso));
        assertTrue(carrito.agregar(queso));
        assertFalse(carrito.agregar(queso));
        assertFalse(carrito.cambiarCantidad(5, 1));
        assertEquals(2, carrito.getUnidades());
    }

    /** Verifica que no agrega productos inactivos o sin existencias. */
    @Test
    public void noAgregaProductosInactivosOSinExistencias() {
        assertFalse(carrito.agregar(new Producto(9, "Inactivo", BigDecimal.TEN, 5, false)));
        assertFalse(carrito.agregar(new Producto(8, "Agotado", BigDecimal.TEN, 0, true)));
        assertTrue(carrito.estaVacio());
    }

    /** Verifica que restar hasta cero quita el producto del carrito. */
    @Test
    public void restarHastaCeroQuitaElProducto() {
        carrito.agregar(chocolate);
        carrito.cambiarCantidad(3, 2);
        assertEquals(3, carrito.getUnidades());
        carrito.cambiarCantidad(3, -3);
        assertTrue(carrito.estaVacio());
    }

    /** Verifica que redondea el IVA al peso más cercano. */
    @Test
    public void redondeaElIvaAlPesoMasCercano() {
        Producto arepas = new Producto(4, "Arepas de maíz x 10", new BigDecimal("7250"), 35, true);
        carrito.agregar(arepas);
        // 7 250 × 0,19 = 1 377,5 → 1 378 (HALF_UP)
        assertEquals(0, new BigDecimal("1378").compareTo(carrito.getIva()));
    }
}
