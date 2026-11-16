package co.sena.gestionpedidos.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Rol;

/**
 * Validaciones de datos de entrada antes de enviarlos a la base de datos (textos, caracteres especiales,
 * longitudes, números y fechas). Cada método lanza {@link NegocioException} con un mensaje claro que el
 * controlador muestra al usuario. Se reutiliza de la evidencia AA2-EV01 y se amplía en AA3-EV02.
 *
 * <p>Estas reglas se aplican en el servidor aunque el navegador ya valide los campos con atributos HTML5
 * ({@code required}, {@code maxlength}, {@code min}, {@code pattern}), porque una petición se puede enviar
 * sin pasar por el formulario (por ejemplo, con curl o desde la API).</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Validador {

    /** Longitud mínima de nombres de personas y productos. */
    public static final int LONGITUD_MINIMA_NOMBRE = 3;
    /** Precio máximo admitido para un producto (COP). */
    public static final BigDecimal PRECIO_MAXIMO = new BigDecimal("100000000");
    /** Stock máximo admitido para un producto. */
    public static final int STOCK_MAXIMO = 100_000;
    /** Cantidad máxima de un producto dentro de un pedido. */
    public static final int CANTIDAD_MAXIMA_POR_PRODUCTO = 1_000;

    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    /** Letras (con tildes y ñ), espacios, apóstrofo, guion y punto: "María José O'Neil-Pérez". */
    private static final Pattern PATRON_NOMBRE_PERSONA = Pattern.compile("^[\\p{L}][\\p{L} .'-]*$");
    /** Letras, dígitos, espacios y signos habituales en productos: "Café 500 g (x 2) - 10% & más". */
    private static final Pattern PATRON_NOMBRE_PRODUCTO =
            Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N} .,/()%&'°+-]*$");
    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("^\\d{6,10}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[36]\\d{9}$");
    private static final Pattern PATRON_LETRA = Pattern.compile("\\p{L}");
    private static final Pattern PATRON_DIGITO = Pattern.compile("\\d");
    private static final int LONGITUD_MINIMA_CLAVE = 8;
    private static final int LONGITUD_MAXIMA_CLAVE = 64;

    private Validador() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * @param valor          texto a validar
     * @param nombreCampo    nombre que se muestra en el mensaje de error
     * @param longitudMaxima tamaño máximo permitido por la columna
     * @return el texto sin espacios al inicio ni al final
     */
    public static String validarTextoObligatorio(String valor, String nombreCampo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("El campo " + nombreCampo + " es obligatorio");
        }
        String valorLimpio = valor.trim().replaceAll("\\s+", " ");
        if (valorLimpio.length() > longitudMaxima) {
            throw new NegocioException("El campo " + nombreCampo + " admite máximo "
                    + longitudMaxima + " caracteres (escribió " + valorLimpio.length() + ")");
        }
        return valorLimpio;
    }

    /**
     * Nombre de una persona: 3 a 100 caracteres, solo letras, espacios, apóstrofo, guion y punto.
     *
     * @param nombre texto digitado
     * @return el nombre limpio
     */
    public static String validarNombrePersona(String nombre) {
        String limpio = validarTextoObligatorio(nombre, "nombre", 100);
        validarLongitudMinima(limpio, "nombre");
        if (!PATRON_NOMBRE_PERSONA.matcher(limpio).matches()) {
            throw new NegocioException("El nombre solo admite letras, espacios, apóstrofo y guion "
                    + "(no números ni caracteres especiales como < > $ # @)");
        }
        return limpio;
    }

    /**
     * Nombre de un producto: 3 a 120 caracteres; admite letras, números y signos comunes, pero no
     * caracteres que se usan para inyectar código, como {@code < > { } ; " $}.
     *
     * @param nombre texto digitado
     * @return el nombre limpio
     */
    public static String validarNombreProducto(String nombre) {
        String limpio = validarTextoObligatorio(nombre, "nombre del producto", 120);
        validarLongitudMinima(limpio, "nombre del producto");
        if (!PATRON_NOMBRE_PRODUCTO.matcher(limpio).matches()) {
            throw new NegocioException("El nombre del producto contiene caracteres no permitidos; "
                    + "use letras, números, espacios y . , / ( ) % & ' ° + -");
        }
        return limpio;
    }

    /**
     * @param documento número de documento de identidad
     * @return el documento sin espacios
     */
    public static String validarDocumento(String documento) {
        String limpio = validarTextoObligatorio(documento, "documento", 10).replace(" ", "");
        if (!PATRON_DOCUMENTO.matcher(limpio).matches()) {
            throw new NegocioException("El documento debe tener entre 6 y 10 dígitos, sin puntos ni letras");
        }
        return limpio;
    }

    /**
     * @param telefono número de celular o fijo de Colombia
     * @return el teléfono sin espacios
     */
    public static String validarTelefono(String telefono) {
        String limpio = validarTextoObligatorio(telefono, "teléfono", 14).replace(" ", "");
        if (!PATRON_TELEFONO.matcher(limpio).matches()) {
            throw new NegocioException("El teléfono debe tener 10 dígitos y empezar por 3 (celular) o 6 (fijo)");
        }
        return limpio;
    }

    /**
     * @param correo correo electrónico
     * @return el correo en minúsculas y sin espacios
     */
    public static String validarCorreo(String correo) {
        String correoLimpio = validarTextoObligatorio(correo, "correo", 150).toLowerCase(Locale.ROOT);
        if (!PATRON_CORREO.matcher(correoLimpio).matches()) {
            throw new NegocioException("El correo " + correoLimpio + " no tiene un formato válido");
        }
        return correoLimpio;
    }

    /**
     * Contraseña de 8 a 64 caracteres con al menos una letra y un número.
     *
     * @param clave contraseña en texto plano
     */
    public static void validarClave(String clave) {
        if (clave == null || clave.length() < LONGITUD_MINIMA_CLAVE || clave.length() > LONGITUD_MAXIMA_CLAVE) {
            throw new NegocioException("La contraseña debe tener entre " + LONGITUD_MINIMA_CLAVE + " y "
                    + LONGITUD_MAXIMA_CLAVE + " caracteres");
        }
        if (!PATRON_LETRA.matcher(clave).find() || !PATRON_DIGITO.matcher(clave).find()) {
            throw new NegocioException("La contraseña debe incluir al menos una letra y un número");
        }
    }

    /**
     * Precio mayor que cero, máximo {@link #PRECIO_MAXIMO} y con máximo dos decimales.
     *
     * @param precio precio del producto
     */
    public static void validarPrecio(BigDecimal precio) {
        if (precio == null) {
            throw new NegocioException("El precio es obligatorio");
        }
        if (precio.signum() <= 0) {
            throw new NegocioException("El precio debe ser mayor que cero");
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new NegocioException("El precio no puede superar $ 100.000.000");
        }
        if (precio.stripTrailingZeros().scale() > 2) {
            throw new NegocioException("El precio admite máximo dos decimales");
        }
    }

    /**
     * @param stock unidades disponibles
     */
    public static void validarStock(int stock) {
        if (stock < 0 || stock > STOCK_MAXIMO) {
            throw new NegocioException("El stock debe estar entre 0 y " + STOCK_MAXIMO);
        }
    }

    /**
     * @param cantidad    valor numérico
     * @param nombreCampo nombre que se muestra en el mensaje de error
     * @param minimo      valor mínimo permitido
     */
    public static void validarMinimo(int cantidad, String nombreCampo, int minimo) {
        if (cantidad < minimo) {
            throw new NegocioException("El campo " + nombreCampo + " debe ser mayor o igual a " + minimo);
        }
    }

    /**
     * Convierte un texto con formato AAAA-MM-DD (el que envía un campo {@code <input type="date">}) en fecha.
     *
     * @param texto       fecha escrita
     * @param nombreCampo nombre que se muestra en el mensaje de error
     * @return la fecha, o {@code null} si el campo está vacío
     */
    public static LocalDate convertirFecha(String texto, String nombreCampo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new NegocioException("La fecha " + nombreCampo + " no es válida: use el formato AAAA-MM-DD "
                    + "con un día que exista en el calendario");
        }
    }

    /**
     * Valida un rango de fechas de consulta: ninguna puede ser futura y "desde" no puede ser posterior a "hasta".
     *
     * @param desde fecha inicial (puede ser {@code null})
     * @param hasta fecha final (puede ser {@code null})
     * @param hoy   fecha actual
     */
    public static void validarRangoFechas(LocalDate desde, LocalDate hasta, LocalDate hoy) {
        if ((desde != null && desde.isAfter(hoy)) || (hasta != null && hasta.isAfter(hoy))) {
            throw new NegocioException("Las fechas de consulta no pueden ser posteriores a hoy (" + hoy + ")");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new NegocioException("La fecha inicial (" + desde + ") no puede ser posterior a la fecha final ("
                    + hasta + ")");
        }
    }

    /**
     * Convierte el nombre de un estado (sin distinguir mayúsculas) en {@link EstadoPedido}.
     *
     * @param texto nombre del estado, por ejemplo "pagado"
     * @return el estado, o {@code null} si el texto está vacío
     */
    public static EstadoPedido convertirEstado(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return EstadoPedido.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new NegocioException("El estado \"" + texto
                    + "\" no existe; use PENDIENTE, PAGADO, ENVIADO o CANCELADO");
        }
    }

    /**
     * Convierte el nombre de un rol (sin distinguir mayúsculas) en {@link Rol}.
     *
     * @param texto nombre del rol, por ejemplo "vendedor"
     * @return el rol
     */
    public static Rol convertirRol(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new NegocioException("El rol es obligatorio");
        }
        try {
            return Rol.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new NegocioException("El rol \"" + texto + "\" no existe; use ADMINISTRADOR, VENDEDOR o CLIENTE");
        }
    }

    private static void validarLongitudMinima(String valor, String nombreCampo) {
        if (valor.length() < LONGITUD_MINIMA_NOMBRE) {
            throw new NegocioException("El campo " + nombreCampo + " debe tener al menos "
                    + LONGITUD_MINIMA_NOMBRE + " caracteres");
        }
    }
}
