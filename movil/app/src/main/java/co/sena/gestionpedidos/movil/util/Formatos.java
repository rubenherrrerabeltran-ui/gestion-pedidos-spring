package co.sena.gestionpedidos.movil.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formatos de presentación para Colombia: pesos sin decimales con punto de miles
 * y fechas DD/MM/AAAA HH:MM.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Formatos {

    private Formatos() {
    }

    /**
     * @param valor valor en pesos
     * @return por ejemplo "$ 46.172"
     */
    public static String moneda(BigDecimal valor) {
        if (valor == null) {
            return "$ 0";
        }
        BigDecimal redondeado = valor.setScale(0, RoundingMode.HALF_UP);
        String digitos = redondeado.abs().toPlainString();
        StringBuilder sb = new StringBuilder();
        int cuenta = 0;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            sb.append(digitos.charAt(i));
            if (++cuenta % 3 == 0 && i > 0) {
                sb.append('.');
            }
        }
        return (redondeado.signum() < 0 ? "-$ " : "$ ") + sb.reverse();
    }

    /**
     * @param iso fecha ISO 8601, por ejemplo 2026-11-12T16:05:00
     * @return por ejemplo "12/11/2026 16:05"; el texto original si no tiene ese formato
     */
    public static String fechaHora(String iso) {
        if (iso == null || iso.length() < 16 || iso.charAt(10) != 'T') {
            return iso == null ? "" : iso;
        }
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4)
                + " " + iso.substring(11, 16);
    }

    /**
     * @param iso fecha AAAA-MM-DD
     * @return fecha DD/MM/AAAA
     */
    public static String fecha(String iso) {
        if (iso == null || iso.length() < 10) {
            return iso == null ? "" : iso;
        }
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4);
    }

    /**
     * @param cantidad número de unidades
     * @return "1 unidad" o "N unidades"
     */
    public static String unidades(int cantidad) {
        return cantidad + (cantidad == 1 ? " unidad" : " unidades");
    }
}
