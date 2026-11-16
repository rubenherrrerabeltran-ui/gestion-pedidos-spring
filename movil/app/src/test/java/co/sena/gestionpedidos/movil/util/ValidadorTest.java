package co.sena.gestionpedidos.movil.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Pruebas unitarias de las validaciones de los formularios.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ValidadorTest {

    /** Verifica la validación del correo. */
    @Test
    public void validaElCorreo() {
        assertNull(Validador.validarCorreo("carlos.cliente@gestionpedidos.local"));
        assertEquals("Escriba su correo", Validador.validarCorreo("  "));
        assertEquals("El correo no tiene un formato válido", Validador.validarCorreo("carlos@"));
    }

    /** Verifica la validación de la contraseña. */
    @Test
    public void validaLaClave() {
        assertNull(Validador.validarClave("Cliente2026*"));
        assertEquals("Escriba su contraseña", Validador.validarClave(""));
    }

    /** Verifica la validación de la dirección del servidor. */
    @Test
    public void validaLaDireccionDelServidor() {
        assertNull(Validador.validarUrlServidor("http://10.0.2.2:8080"));
        assertNull(Validador.validarUrlServidor("https://pedidos.example.com"));
        assertEquals("Use el formato http://servidor:puerto", Validador.validarUrlServidor("10.0.2.2:8080"));
        assertEquals("http://10.0.2.2:8080", Validador.normalizarUrl(" http://10.0.2.2:8080// "));
    }

    /** Verifica la validación del rango de fechas del reporte. */
    @Test
    public void validaElRangoDeFechasDelReporte() {
        assertNull(Validador.validarRangoFechas("", ""));
        assertNull(Validador.validarRangoFechas("2026-11-01", "2026-11-16"));
        assertEquals("La fecha inicial no puede ser posterior a la final",
                Validador.validarRangoFechas("2026-11-20", "2026-11-01"));
        assertEquals("Use fechas con el formato AAAA-MM-DD", Validador.validarRangoFechas("01/11/2026", ""));
    }
}
