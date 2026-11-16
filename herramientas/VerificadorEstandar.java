import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Verifica automáticamente las reglas del estándar de codificación del proyecto
 * (docs/estandares-codificacion.md) sobre todos los archivos .java de src/.
 *
 * <p>Uso (desde la carpeta del proyecto, sin compilar): {@code java herramientas/VerificadorEstandar.java}</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class VerificadorEstandar {

    private static final int LONGITUD_MAXIMA = 120;
    private static final Pattern DECLARACION_TIPO = Pattern.compile(
            "^public\\s+(?:final\\s+|abstract\\s+)?(class|interface|enum|record|@interface)\\s+(\\w+)");
    private static final Pattern METODO_PUBLICO = Pattern.compile(
            "^\\s{4}public\\s+(?:static\\s+)?(?:<[^>]+>\\s+)?[\\w.<>\\[\\], ?]+\\s+(\\w+)\\s*\\(");
    private static final Pattern CONSTANTE = Pattern.compile(
            "^\\s+(?:public|private|protected)\\s+static\\s+final\\s+[\\w.<>\\[\\], ?]+\\s+(\\w+)\\s*=");
    private static final Pattern ACCESOR = Pattern.compile("^(get|set|is)[A-Z]\\w*$");

    private final Map<String, List<String>> fallasPorRegla = new LinkedHashMap<>();
    private int archivos;
    private int lineasCodigo;
    private int lineasComentario;

    /**
     * @param args carpeta raíz opcional (por defecto {@code src})
     * @throws IOException si no se puede leer un archivo
     */
    public static void main(String[] args) throws IOException {
        VerificadorEstandar verificador = new VerificadorEstandar();
        Path raiz = Path.of(args.length > 0 ? args[0] : "src");
        try (Stream<Path> rutas = Files.walk(raiz)) {
            for (Path archivo : rutas.filter(r -> r.toString().endsWith(".java")).sorted().toList()) {
                verificador.revisar(archivo);
            }
        }
        System.exit(verificador.imprimirInforme() ? 0 : 1);
    }

    private void registrar(String regla, String detalle) {
        fallasPorRegla.computeIfAbsent(regla, r -> new ArrayList<>());
        if (detalle != null) {
            fallasPorRegla.get(regla).add(detalle);
        }
    }

    private void revisar(Path archivo) throws IOException {
        archivos++;
        String[] reglas = {"R1 Codificación UTF-8 y fin de línea LF", "R2 Sangría con espacios (sin tabuladores)",
            "R3 Líneas de máximo 120 caracteres", "R4 Sin espacios al final de la línea",
            "R5 Importaciones explícitas (sin *)", "R6 Paquete en minúsculas (co.sena.gestionpedidos...)",
            "R7 Tipo público en PascalCase y con el nombre del archivo", "R8 Javadoc con @author en cada tipo público",
            "R9 Javadoc en métodos públicos (excepto @Override, getters y setters)",
            "R10 Constantes static final en MAYÚSCULAS_CON_GUION_BAJO"};
        for (String regla : reglas) {
            registrar(regla, null);
        }
        byte[] bytes = Files.readAllBytes(archivo);
        String texto;
        try {
            texto = StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            registrar(reglas[0], archivo + " no está en UTF-8");
            return;
        }
        if (texto.contains("\r")) {
            registrar(reglas[0], archivo + " usa CRLF");
        }
        String[] lineas = texto.split("\n", -1);
        boolean enComentario = false;
        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            String ubicacion = archivo + ":" + (i + 1);
            String limpia = linea.trim();
            if (limpia.startsWith("/*")) {
                enComentario = true;
            }
            if (enComentario || limpia.startsWith("//")) {
                lineasComentario++;
            } else if (!limpia.isEmpty()) {
                lineasCodigo++;
            }
            if (limpia.endsWith("*/")) {
                enComentario = false;
            }
            if (linea.contains("\t")) {
                registrar(reglas[1], ubicacion);
            }
            if (linea.length() > LONGITUD_MAXIMA) {
                registrar(reglas[2], ubicacion + " (" + linea.length() + ")");
            }
            if (!linea.isEmpty() && Character.isWhitespace(linea.charAt(linea.length() - 1))) {
                registrar(reglas[3], ubicacion);
            }
            if (linea.startsWith("import ") && linea.trim().endsWith(".*;")) {
                registrar(reglas[4], ubicacion);
            }
            if (linea.startsWith("package ") && !linea.matches("package [a-z][a-z0-9.]*;")) {
                registrar(reglas[5], ubicacion);
            }
            Matcher tipo = DECLARACION_TIPO.matcher(linea);
            if (tipo.find()) {
                String nombre = tipo.group(2);
                if (!nombre.matches("[A-Z][A-Za-z0-9]*") || !archivo.getFileName().toString().equals(nombre + ".java")) {
                    registrar(reglas[6], ubicacion + " " + nombre);
                }
                if (!javadocPrevio(lineas, i).contains("@author")) {
                    registrar(reglas[7], ubicacion + " " + nombre);
                }
            }
            Matcher metodo = METODO_PUBLICO.matcher(linea);
            if (metodo.find() && !ACCESOR.matcher(metodo.group(1)).matches()
                    && !anotacionPrevia(lineas, i, "@Override") && !javadocPrevio(lineas, i).startsWith("/**")) {
                registrar(reglas[8], ubicacion + " " + metodo.group(1) + "()");
            }
            Matcher constante = CONSTANTE.matcher(linea);
            if (constante.find() && !constante.group(1).matches("[A-Z][A-Z0-9_]*|serialVersionUID")) {
                registrar(reglas[9], ubicacion + " " + constante.group(1));
            }
        }
    }

    /** Devuelve el bloque Javadoc inmediatamente anterior (saltando anotaciones), o cadena vacía. */
    private static String javadocPrevio(String[] lineas, int indice) {
        int i = indice - 1;
        while (i >= 0 && (lineas[i].trim().startsWith("@") || lineas[i].trim().startsWith("})"))) {
            i--;
        }
        if (i < 0 || !lineas[i].trim().endsWith("*/")) {
            return "";
        }
        StringBuilder bloque = new StringBuilder();
        while (i >= 0 && !lineas[i].trim().startsWith("/**")) {
            bloque.insert(0, lineas[i] + "\n");
            i--;
        }
        return i < 0 ? "" : lineas[i].trim() + "\n" + bloque;
    }

    private static boolean anotacionPrevia(String[] lineas, int indice, String anotacion) {
        for (int i = indice - 1; i >= 0 && lineas[i].trim().startsWith("@"); i--) {
            if (lineas[i].trim().startsWith(anotacion)) {
                return true;
            }
        }
        return false;
    }

    private boolean imprimirInforme() {
        System.out.println("Verificación del estándar de codificación - docs/estandares-codificacion.md");
        System.out.printf("Archivos .java revisados: %d | líneas de código: %d | líneas de comentario: %d (%.0f %%)%n%n",
                archivos, lineasCodigo, lineasComentario, 100.0 * lineasComentario / (lineasCodigo + lineasComentario));
        int totalFallas = 0;
        for (Map.Entry<String, List<String>> regla : fallasPorRegla.entrySet()) {
            List<String> fallas = regla.getValue();
            totalFallas += fallas.size();
            System.out.printf("  [%s] %s%n", fallas.isEmpty() ? "CUMPLE" : "FALLA ", regla.getKey());
            fallas.stream().limit(5).forEach(f -> System.out.println("           - " + f));
        }
        System.out.println();
        System.out.println(totalFallas == 0 ? "RESULTADO: el código cumple todas las reglas del estándar"
                : "RESULTADO: " + totalFallas + " incumplimiento(s)");
        return totalFallas == 0;
    }
}
