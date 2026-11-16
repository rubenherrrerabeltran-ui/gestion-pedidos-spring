package co.sena.gestionpedidos.web.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.api.dto.UsuarioRespuesta;
import co.sena.gestionpedidos.web.api.dto.UsuarioSolicitud;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;
import co.sena.gestionpedidos.web.seguridad.UsuarioSesion;

/**
 * API REST de usuarios y roles (requerimiento RF09). Toda la clase está restringida al ADMINISTRADOR.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiereRol(Rol.ADMINISTRADOR)
public class UsuarioApiControlador {

    private final UsuarioServicio usuarioServicio;

    /**
     * @param usuarioServicio servicio de usuarios (la misma lógica que usa la vista web)
     */
    public UsuarioApiControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * GET /api/usuarios
     *
     * @return todos los usuarios sin sus contraseñas
     */
    @GetMapping
    public List<UsuarioRespuesta> listar() {
        return usuarioServicio.listar().stream().map(UsuarioRespuesta::desde).toList();
    }

    /**
     * GET /api/usuarios/{id}
     *
     * @param id identificador
     * @return el usuario (404 si no existe)
     */
    @GetMapping("/{id}")
    public UsuarioRespuesta obtener(@PathVariable int id) {
        return UsuarioRespuesta.desde(usuarioServicio.obtener(id));
    }

    /**
     * POST /api/usuarios
     *
     * @param solicitud datos del usuario en JSON
     * @return 201 Created con la cabecera Location
     */
    @PostMapping
    public ResponseEntity<UsuarioRespuesta> crear(@RequestBody UsuarioSolicitud solicitud) {
        Usuario creado = usuarioServicio.guardar(null, solicitud.aDatos());
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.getIdUsuario()))
                .body(UsuarioRespuesta.desde(creado));
    }

    /**
     * PUT /api/usuarios/{id} (la clave es opcional: si se omite, se conserva la actual)
     *
     * @param id        usuario a actualizar
     * @param solicitud nuevos datos
     * @return el usuario actualizado
     */
    @PutMapping("/{id}")
    public UsuarioRespuesta actualizar(@PathVariable int id, @RequestBody UsuarioSolicitud solicitud) {
        return UsuarioRespuesta.desde(usuarioServicio.guardar(id, solicitud.aDatos()));
    }

    /**
     * DELETE /api/usuarios/{id}
     *
     * @param id     usuario a eliminar
     * @param actual administrador con la sesión abierta (no puede eliminarse a sí mismo)
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id,
                                         @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion actual) {
        usuarioServicio.eliminar(id, actual.idUsuario());
        return ResponseEntity.noContent().build();
    }
}
