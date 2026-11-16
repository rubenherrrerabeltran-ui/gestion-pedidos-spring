package co.sena.gestionpedidos.movil.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/** Pruebas unitarias de los formatos de moneda y fecha para Colombia.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class FormatosTest {

    /** Verifica el formato de pesos con punto de miles. */
    @Test
    public void formateaPesosConPuntoDeMiles() {
        assertEquals("$ 46.172", Formatos.moneda(new BigDecimal("46172.0")));
        assertEquals("$ 1.234.568", Formatos.moneda(new BigDecimal("1234567.5")));
        assertEquals("$ 950", Formatos.moneda(new BigDecimal("950")));
        assertEquals("$ 0", Formatos.moneda(null));
    }

    /** Verifica el formato de fechas y unidades. */
    @Test
    public void formateaFechas() {
        assertEquals("12/11/2026 16:05", Formatos.fechaHora("2026-11-12T16:05:00"));
        assertEquals("01/11/2026", Formatos.fecha("2026-11-01"));
        assertEquals("1 unidad", Formatos.unidades(1));
        assertEquals("3 unidades", Formatos.unidades(3));
    }
}
