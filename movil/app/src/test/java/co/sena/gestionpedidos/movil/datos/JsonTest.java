package co.sena.gestionpedidos.movil.datos;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.sena.gestionpedidos.movil.datos.json.Json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** Pruebas unitarias del lector y escritor JSON.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class JsonTest {

    /** Verifica que lee objetos anidados con tildes y números. */
    @Test
    public void leeObjetosAnidadosConTildesYNumeros() {
        Map<String, Object> json = Json.leerObjeto(
                "{\"nombre\":\"Panela orgánica\",\"precio\":6500.0,\"activo\":true,\"nota\":null,"
                        + "\"items\":[1,2,{\"x\":\"\\u00f1\"}]}");
        assertEquals("Panela orgánica", json.get("nombre"));
        assertEquals(0, new BigDecimal("6500").compareTo((BigDecimal) json.get("precio")));
        assertEquals(Boolean.TRUE, json.get("activo"));
        assertNull(json.get("nota"));
        List<?> items = (List<?>) json.get("items");
        assertEquals(3, items.size());
        assertEquals("ñ", ((Map<?, ?>) items.get(2)).get("x"));
    }

    /** Verifica que escribe y vuelve a leer el mismo contenido. */
    @Test
    public void escribeYVuelveALeerElMismoContenido() {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("correo", "carlos.cliente@gestionpedidos.local");
        cuerpo.put("texto", "Comillas \" y barra \\");
        cuerpo.put("items", Arrays.asList(1, 2));
        String texto = Json.escribir(cuerpo);
        assertEquals("{\"correo\":\"carlos.cliente@gestionpedidos.local\","
                + "\"texto\":\"Comillas \\\" y barra \\\\\",\"items\":[1,2]}", texto);
        assertEquals("Comillas \" y barra \\", Json.leerObjeto(texto).get("texto"));
    }

    /** Verifica que rechaza JSON mal formado. */
    @Test
    public void rechazaJsonMalFormado() {
        try {
            Json.leer("{\"a\":1,}");
            fail("Debía rechazar la coma sobrante");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("JSON inválido"));
        }
    }
}
