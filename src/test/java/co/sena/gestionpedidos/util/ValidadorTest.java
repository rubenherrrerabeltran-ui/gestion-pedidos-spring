package co.sena.gestionpedidos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.excepcion.NegocioException;

/**
 * Pruebas unitarias de las validaciones de datos: textos, caracteres especiales, longitudes, números y fechas.
 * No necesitan Spring ni base de datos.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("Validador (textos, caracteres especiales, longitudes, números y fechas)")
class ValidadorTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);

    private static String mensaje(Runnable accion) {
        return assertThrows(NegocioException.class, accion::run).getMessage();
    }

    @Test
    @DisplayName("Texto: obligatorio, sin espacios sobrantes y con longitud máxima")
    void textoObligatorioYLongitud() {
        assertEquals("El campo nombre es obligatorio", mensaje(() -> Validador.validarNombrePersona("   ")));
        assertEquals("Ana María Paz", Validador.validarNombrePersona("  Ana   María  Paz "));
        assertTrue(mensaje(() -> Validador.validarNombrePersona("A".repeat(101))).contains("máximo 100"));
        assertTrue(mensaje(() -> Validador.validarNombrePersona("Al")).contains("al menos 3"));
    }

    @Test
    @DisplayName("Nombre de persona: acepta tildes, ñ, apóstrofo y guion; rechaza números y símbolos")
    void nombrePersonaCaracteresEspeciales() {
        assertEquals("José Ñúñez O'Neil-Pérez", Validador.validarNombrePersona("José Ñúñez O'Neil-Pérez"));
        assertTrue(mensaje(() -> Validador.validarNombrePersona("Juan123")).contains("solo admite letras"));
        assertTrue(mensaje(() -> Validador.validarNombrePersona("<script>alert(1)</script>")).contains("solo admite"));
        assertTrue(mensaje(() -> Validador.validarNombrePersona("Ana $ Gómez")).contains("solo admite"));
    }

    @Test
    @DisplayName("Nombre de producto: acepta signos comunes y rechaza < > { } ; \" $")
    void nombreProductoCaracteresEspeciales() {
        assertEquals("Café 500 g (x 2) - 10% & más", Validador.validarNombreProducto("Café 500 g (x 2) - 10% & más"));
        assertTrue(mensaje(() -> Validador.validarNombreProducto("<b>Oferta</b>")).contains("no permitidos"));
        assertTrue(mensaje(() -> Validador.validarNombreProducto("Arroz; DROP TABLE producto"))
                .contains("no permitidos"));
        assertTrue(mensaje(() -> Validador.validarNombreProducto("X".repeat(121))).contains("máximo 120"));
    }

    @Test
    @DisplayName("Documento: solo dígitos, entre 6 y 10")
    void documento() {
        assertEquals("1032456789", Validador.validarDocumento("1032456789"));
        assertTrue(mensaje(() -> Validador.validarDocumento("12345")).contains("entre 6 y 10"));
        assertTrue(mensaje(() -> Validador.validarDocumento("10.324.567")).contains("entre 6 y 10"));
        assertTrue(mensaje(() -> Validador.validarDocumento("ABC123456")).contains("entre 6 y 10"));
    }

    @Test
    @DisplayName("Teléfono: 10 dígitos que inician en 3 o 6")
    void telefono() {
        assertEquals("3001234567", Validador.validarTelefono("300 123 4567"));
        assertTrue(mensaje(() -> Validador.validarTelefono("300123456")).contains("10 dígitos"));
        assertTrue(mensaje(() -> Validador.validarTelefono("5001234567")).contains("10 dígitos"));
        assertTrue(mensaje(() -> Validador.validarTelefono("30012345ab")).contains("10 dígitos"));
    }

    @Test
    @DisplayName("Correo: formato válido y conversión a minúsculas")
    void correo() {
        assertEquals("ana.paz@correo.com", Validador.validarCorreo("Ana.Paz@Correo.COM"));
        assertTrue(mensaje(() -> Validador.validarCorreo("ana.paz@correo")).contains("formato válido"));
        assertTrue(mensaje(() -> Validador.validarCorreo("ana paz@correo.com")).contains("formato válido"));
    }

    @Test
    @DisplayName("Contraseña: 8 a 64 caracteres con letras y números")
    void clave() {
        Validador.validarClave("Segura2026");
        assertTrue(mensaje(() -> Validador.validarClave("abc12")).contains("entre 8 y 64"));
        assertTrue(mensaje(() -> Validador.validarClave("solamenteletras")).contains("una letra y un número"));
        assertTrue(mensaje(() -> Validador.validarClave("12345678")).contains("una letra y un número"));
        assertTrue(mensaje(() -> Validador.validarClave("a1".repeat(33))).contains("entre 8 y 64"));
    }

    @Test
    @DisplayName("Precio: mayor que cero, máximo 100 millones y dos decimales")
    void precio() {
        Validador.validarPrecio(new BigDecimal("28000.50"));
        assertEquals("El precio debe ser mayor que cero", mensaje(() -> Validador.validarPrecio(BigDecimal.ZERO)));
        assertEquals("El precio debe ser mayor que cero", mensaje(() -> Validador.validarPrecio(new BigDecimal("-5"))));
        assertTrue(mensaje(() -> Validador.validarPrecio(new BigDecimal("100000000.01"))).contains("superar"));
        assertTrue(mensaje(() -> Validador.validarPrecio(new BigDecimal("10.555"))).contains("dos decimales"));
    }

    @Test
    @DisplayName("Stock: entero entre 0 y 100 000")
    void stock() {
        Validador.validarStock(0);
        Validador.validarStock(100_000);
        assertTrue(mensaje(() -> Validador.validarStock(-1)).contains("entre 0 y 100000"));
        assertTrue(mensaje(() -> Validador.validarStock(100_001)).contains("entre 0 y 100000"));
    }

    @Test
    @DisplayName("Fechas: formato AAAA-MM-DD y días que existen en el calendario")
    void formatoDeFechas() {
        assertEquals(LocalDate.of(2024, 2, 29), Validador.convertirFecha("2024-02-29", "inicial"));
        assertEquals(null, Validador.convertirFecha("", "inicial"));
        assertTrue(mensaje(() -> Validador.convertirFecha("2026-02-30", "inicial")).contains("no es válida"));
        assertTrue(mensaje(() -> Validador.convertirFecha("16/11/2026", "final")).contains("AAAA-MM-DD"));
        assertTrue(mensaje(() -> Validador.convertirFecha("ayer", "final")).contains("no es válida"));
    }

    @Test
    @DisplayName("Rango de fechas: sin fechas futuras y con desde anterior o igual a hasta")
    void rangoDeFechas() {
        Validador.validarRangoFechas(LocalDate.of(2026, 11, 1), HOY, HOY);
        Validador.validarRangoFechas(HOY, HOY, HOY);
        assertTrue(mensaje(() -> Validador.validarRangoFechas(HOY, HOY.minusDays(1), HOY))
                .contains("no puede ser posterior"));
        assertTrue(mensaje(() -> Validador.validarRangoFechas(null, HOY.plusDays(1), HOY))
                .contains("posteriores a hoy"));
    }
}
