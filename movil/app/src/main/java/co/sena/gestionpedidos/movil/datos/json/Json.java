package co.sena.gestionpedidos.movil.datos.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lector y escritor de JSON en Java puro (RFC 8259), reutilizable en cualquier módulo.
 * Convierte el texto en {@link Map} (objetos), {@link List} (arreglos), {@link String},
 * {@link BigDecimal} (números), {@link Boolean} y {@code null}. Al no depender de
 * {@code org.json} de Android, se puede probar con pruebas unitarias locales.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Json {

    private final String texto;
    private int pos;

    private Json(String texto) {
        this.texto = texto;
    }

    /**
     * Convierte un texto JSON en objetos de Java.
     *
     * @param texto documento JSON
     * @return Map, List, String, BigDecimal, Boolean o null
     * @throws IllegalArgumentException si el JSON no es válido
     */
    public static Object leer(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("JSON vacío");
        }
        Json lector = new Json(texto);
        lector.saltarEspacios();
        Object valor = lector.leerValor();
        lector.saltarEspacios();
        if (lector.pos != texto.length()) {
            throw lector.error("Contenido sobrante");
        }
        return valor;
    }

    /**
     * @param texto documento JSON cuya raíz es un objeto
     * @return el objeto como mapa
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> leerObjeto(String texto) {
        Object valor = leer(texto);
        if (!(valor instanceof Map)) {
            throw new IllegalArgumentException("Se esperaba un objeto JSON");
        }
        return (Map<String, Object>) valor;
    }

    /**
     * @param texto documento JSON cuya raíz es un arreglo
     * @return el arreglo como lista
     */
    @SuppressWarnings("unchecked")
    public static List<Object> leerLista(String texto) {
        Object valor = leer(texto);
        if (!(valor instanceof List)) {
            throw new IllegalArgumentException("Se esperaba un arreglo JSON");
        }
        return (List<Object>) valor;
    }

    /**
     * Convierte objetos de Java (Map, List, String, Number, Boolean, null) en texto JSON.
     *
     * @param valor valor que se va a escribir
     * @return texto JSON compacto
     */
    public static String escribir(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribir(valor, sb);
        return sb.toString();
    }

    private static void escribir(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof String) {
            escribirTexto((String) valor, sb);
        } else if (valor instanceof Number || valor instanceof Boolean) {
            sb.append(valor instanceof BigDecimal ? ((BigDecimal) valor).toPlainString() : valor.toString());
        } else if (valor instanceof Map) {
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) valor).entrySet()) {
                if (!primero) {
                    sb.append(',');
                }
                escribirTexto(String.valueOf(e.getKey()), sb);
                sb.append(':');
                escribir(e.getValue(), sb);
                primero = false;
            }
            sb.append('}');
        } else if (valor instanceof Iterable) {
            sb.append('[');
            boolean primero = true;
            for (Object elemento : (Iterable<?>) valor) {
                if (!primero) {
                    sb.append(',');
                }
                escribir(elemento, sb);
                primero = false;
            }
            sb.append(']');
        } else {
            escribirTexto(valor.toString(), sb);
        }
    }

    private static void escribirTexto(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private Object leerValor() {
        if (pos >= texto.length()) {
            throw error("Fin inesperado");
        }
        char c = texto.charAt(pos);
        switch (c) {
            case '{': return leerMapa();
            case '[': return leerArreglo();
            case '"': return leerCadena();
            case 't': return leerLiteral("true", Boolean.TRUE);
            case 'f': return leerLiteral("false", Boolean.FALSE);
            case 'n': return leerLiteral("null", null);
            default:
                if (c == '-' || Character.isDigit(c)) {
                    return leerNumero();
                }
                throw error("Carácter inesperado '" + c + "'");
        }
    }

    private Map<String, Object> leerMapa() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        pos++;
        saltarEspacios();
        if (consumir('}')) {
            return mapa;
        }
        do {
            saltarEspacios();
            if (pos >= texto.length() || texto.charAt(pos) != '"') {
                throw error("Se esperaba el nombre de un campo");
            }
            String clave = leerCadena();
            saltarEspacios();
            if (!consumir(':')) {
                throw error("Se esperaba ':'");
            }
            saltarEspacios();
            mapa.put(clave, leerValor());
            saltarEspacios();
        } while (consumir(','));
        if (!consumir('}')) {
            throw error("Se esperaba '}'");
        }
        return mapa;
    }

    private List<Object> leerArreglo() {
        List<Object> lista = new ArrayList<>();
        pos++;
        saltarEspacios();
        if (consumir(']')) {
            return lista;
        }
        do {
            saltarEspacios();
            lista.add(leerValor());
            saltarEspacios();
        } while (consumir(','));
        if (!consumir(']')) {
            throw error("Se esperaba ']'");
        }
        return lista;
    }

    private String leerCadena() {
        StringBuilder sb = new StringBuilder();
        pos++;
        while (pos < texto.length()) {
            char c = texto.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= texto.length()) {
                break;
            }
            char e = texto.charAt(pos++);
            switch (e) {
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'u':
                    if (pos + 4 > texto.length()) {
                        throw error("Escape unicode incompleto");
                    }
                    sb.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
                    pos += 4;
                    break;
                default: sb.append(e);
            }
        }
        throw error("Cadena sin cerrar");
    }

    private BigDecimal leerNumero() {
        int inicio = pos;
        while (pos < texto.length() && "+-0123456789.eE".indexOf(texto.charAt(pos)) >= 0) {
            pos++;
        }
        try {
            return new BigDecimal(texto.substring(inicio, pos));
        } catch (NumberFormatException e) {
            throw error("Número inválido");
        }
    }

    private Object leerLiteral(String literal, Object valor) {
        if (!texto.startsWith(literal, pos)) {
            throw error("Se esperaba " + literal);
        }
        pos += literal.length();
        return valor;
    }

    private boolean consumir(char c) {
        if (pos < texto.length() && texto.charAt(pos) == c) {
            pos++;
            return true;
        }
        return false;
    }

    private void saltarEspacios() {
        while (pos < texto.length() && Character.isWhitespace(texto.charAt(pos))) {
            pos++;
        }
    }

    private IllegalArgumentException error(String mensaje) {
        return new IllegalArgumentException("JSON inválido en la posición " + pos + ": " + mensaje);
    }
}
