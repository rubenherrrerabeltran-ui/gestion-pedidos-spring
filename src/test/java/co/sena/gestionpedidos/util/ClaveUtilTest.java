package co.sena.gestionpedidos.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del mecanismo de seguridad de contraseñas (PBKDF2 con sal aleatoria).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("ClaveUtil (hash PBKDF2 de contraseñas)")
class ClaveUtilTest {

    @Test
    @DisplayName("El hash no contiene la contraseña y tiene el formato pbkdf2$iteraciones$sal$hash")
    void formatoDelHash() {
        String hash = ClaveUtil.generarHash("Cliente2026*");
        assertFalse(hash.contains("Cliente2026*"));
        assertTrue(hash.matches("pbkdf2\\$120000\\$[A-Za-z0-9+/=]+\\$[A-Za-z0-9+/=]+"));
    }

    @Test
    @DisplayName("La misma contraseña produce hashes distintos por la sal aleatoria")
    void salAleatoria() {
        assertNotEquals(ClaveUtil.generarHash("Vendedor2026*"), ClaveUtil.generarHash("Vendedor2026*"));
    }

    @Test
    @DisplayName("Verifica la contraseña correcta y rechaza una incorrecta o un hash inválido")
    void verificar() {
        String hash = ClaveUtil.generarHash("Admin2026*");
        assertTrue(ClaveUtil.verificar("Admin2026*", hash));
        assertFalse(ClaveUtil.verificar("admin2026*", hash));
        assertFalse(ClaveUtil.verificar("Admin2026*", "texto-sin-formato"));
        assertFalse(ClaveUtil.verificar("Admin2026*", null));
    }
}
