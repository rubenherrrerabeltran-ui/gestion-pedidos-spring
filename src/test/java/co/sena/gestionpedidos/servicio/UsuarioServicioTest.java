package co.sena.gestionpedidos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.soporte.AplicacionPrueba;

/**
 * Pruebas del servicio de usuarios: autenticación y reglas de registro.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("UsuarioServicio (autenticación y registro)")
class UsuarioServicioTest {

    private UsuarioServicio usuarioServicio;

    @BeforeEach
    void obtenerBean() {
        usuarioServicio = AplicacionPrueba.bean(UsuarioServicio.class);
    }

    @Test
    @DisplayName("Autentica con la contraseña correcta (hash PBKDF2)")
    void autenticaCorrecto() {
        Usuario usuario = usuarioServicio.autenticar("admin@gestionpedidos.local", "Admin2026*");
        assertEquals(Rol.ADMINISTRADOR, usuario.getRol());
    }

    @Test
    @DisplayName("Rechaza contraseña incorrecta con un mensaje genérico")
    void rechazaClaveIncorrecta() {
        NegocioException error = assertThrows(NegocioException.class,
                () -> usuarioServicio.autenticar("admin@gestionpedidos.local", "otra-clave"));
        assertEquals("Correo o contraseña incorrectos", error.getMessage());
    }

    @Test
    @DisplayName("Registra un usuario guardando el hash, nunca la contraseña")
    void registraConHash() {
        Usuario usuario = usuarioServicio.guardar(null, "Ana Torres", "Ana.Torres@Correo.com", "Segura2026*",
                Rol.VENDEDOR);
        assertEquals("ana.torres@correo.com", usuario.getCorreo());
        assertTrue(usuario.getPasswordHash().startsWith("pbkdf2$"));
        assertFalse(usuario.getPasswordHash().contains("Segura2026*"));
        Usuario autenticado = usuarioServicio.autenticar("ana.torres@correo.com", "Segura2026*");
        assertEquals(usuario.getIdUsuario(), autenticado.getIdUsuario());
    }

    @Test
    @DisplayName("No permite correos repetidos ni contraseñas cortas")
    void validaCorreoYClave() {
        assertThrows(NegocioException.class,
                () -> usuarioServicio.guardar(null, "Otro admin", "admin@gestionpedidos.local", "Clave2026*",
                        Rol.CLIENTE));
        assertThrows(NegocioException.class,
                () -> usuarioServicio.guardar(null, "Pedro", "pedro@correo.com", "123", Rol.CLIENTE));
    }

    @Test
    @DisplayName("No elimina al usuario con sesión ni a clientes con pedidos")
    void reglasDeEliminacion() {
        assertThrows(NegocioException.class, () -> usuarioServicio.eliminar(1, 1));
        assertThrows(NegocioException.class, () -> usuarioServicio.eliminar(3, 1));
    }
}
