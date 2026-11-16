package co.sena.gestionpedidos.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.excepcion.RecursoNoEncontradoException;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.repositorio.UsuarioRepositorio;
import co.sena.gestionpedidos.util.ClaveUtil;
import co.sena.gestionpedidos.util.Validador;

/**
 * Reglas de negocio del módulo de usuarios: registro, edición, eliminación y autenticación.
 * Las contraseñas nunca se guardan en texto plano: se convierten en hash PBKDF2 con {@link ClaveUtil}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Service
@Transactional(readOnly = true)
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    /**
     * @param usuarioRepositorio repositorio inyectado por Spring
     */
    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    /**
     * Verifica correo y contraseña.
     *
     * @param correo correo digitado
     * @param clave  contraseña digitada
     * @return el usuario autenticado
     * @throws NegocioException si las credenciales no son válidas (mensaje genérico por seguridad)
     */
    public Usuario autenticar(String correo, String clave) {
        if (correo == null || correo.isBlank() || clave == null || clave.isEmpty()) {
            throw new NegocioException("Digite el correo y la contraseña");
        }
        Optional<Usuario> usuario = usuarioRepositorio.buscarPorCorreo(correo);
        if (usuario.isEmpty() || !ClaveUtil.verificar(clave, usuario.get().getPasswordHash())) {
            throw new NegocioException("Correo o contraseña incorrectos");
        }
        return usuario.get();
    }

    /**
     * @return todos los usuarios
     */
    public List<Usuario> listar() {
        return usuarioRepositorio.listar();
    }

    /**
     * @return usuarios con rol CLIENTE (para asignarles pedidos)
     */
    public List<Usuario> listarClientes() {
        return usuarioRepositorio.listarPorRol(Rol.CLIENTE);
    }

    /**
     * @param idUsuario identificador
     * @return el usuario
     * @throws RecursoNoEncontradoException si no existe
     */
    public Usuario obtener(int idUsuario) {
        return usuarioRepositorio.buscarPorId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", idUsuario));
    }

    /**
     * Crea o actualiza un usuario.
     *
     * @param idUsuario {@code null} para crear; el id para editar
     * @param nombre    nombre completo
     * @param correo    correo único
     * @param clave     contraseña en texto plano; obligatoria al crear, opcional al editar
     * @param rol       rol asignado
     * @return el usuario guardado
     */
    @Transactional
    public Usuario guardar(Integer idUsuario, String nombre, String correo, String clave, Rol rol) {
        Usuario usuario = idUsuario == null ? new Usuario() : obtener(idUsuario);
        usuario.setNombre(Validador.validarTextoObligatorio(nombre, "nombre", 100));
        usuario.setCorreo(Validador.validarCorreo(correo));
        if (rol == null) {
            throw new NegocioException("Seleccione el rol del usuario");
        }
        usuario.setRol(rol);
        usuarioRepositorio.buscarPorCorreo(usuario.getCorreo())
                .filter(otro -> !otro.getIdUsuario().equals(idUsuario))
                .ifPresent(otro -> {
                    throw new NegocioException("Ya existe un usuario con el correo " + usuario.getCorreo());
                });
        boolean cambiaClave = idUsuario == null || (clave != null && !clave.isEmpty());
        if (cambiaClave) {
            Validador.validarClave(clave);
            usuario.setPasswordHash(ClaveUtil.generarHash(clave));
        }
        return usuarioRepositorio.guardar(usuario);
    }

    /**
     * Elimina un usuario que no tenga pedidos registrados.
     *
     * @param idUsuario       usuario a eliminar
     * @param idUsuarioActual usuario que tiene la sesión abierta (no puede eliminarse a sí mismo)
     */
    @Transactional
    public void eliminar(int idUsuario, int idUsuarioActual) {
        if (idUsuario == idUsuarioActual) {
            throw new NegocioException("No puede eliminar el usuario con el que inició sesión");
        }
        obtener(idUsuario);
        try {
            usuarioRepositorio.eliminar(idUsuario);
        } catch (DataIntegrityViolationException e) {
            throw new NegocioException("El usuario " + idUsuario + " tiene pedidos registrados y no se puede eliminar");
        }
    }

    /**
     * @return número de usuarios registrados
     */
    public long contar() {
        return usuarioRepositorio.contar();
    }
}
