package co.sena.gestionpedidos.web.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.api.dto.UsuarioRespuesta;
import co.sena.gestionpedidos.web.api.dto.UsuarioSolicitud;
import co.sena.gestionpedidos.web.seguridad.Publico;

/**
 * API pública de registro de clientes (requerimiento RF02): una aplicación móvil o web externa
 * puede crear cuentas de cliente sin iniciar sesión.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RestController
@RequestMapping("/api/clientes")
@Publico
public class ClienteApiControlador {

    private final UsuarioServicio usuarioServicio;

    /**
     * @param usuarioServicio servicio de usuarios
     */
    public ClienteApiControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * POST /api/clientes: registra un cliente; el rol siempre es CLIENTE.
     *
     * @param solicitud datos personales y contraseña en JSON
     * @return 201 Created con el cliente registrado
     */
    @PostMapping
    public ResponseEntity<UsuarioRespuesta> registrar(@RequestBody UsuarioSolicitud solicitud) {
        Usuario cliente = usuarioServicio.registrarCliente(solicitud.aDatosCliente());
        return ResponseEntity.created(URI.create("/api/usuarios/" + cliente.getIdUsuario()))
                .body(UsuarioRespuesta.desde(cliente));
    }
}
