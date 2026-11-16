package co.sena.gestionpedidos.web.formulario;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;

/**
 * Objeto de formulario (form backing object) para crear o editar usuarios.
 * Se separa de la entidad {@link Usuario} porque recibe la contraseña en texto plano,
 * que nunca se guarda: el servicio la convierte en hash.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class UsuarioFormulario {

    private Integer idUsuario;
    private String nombre;
    private String correo;
    private String clave;
    private Rol rol = Rol.CLIENTE;

    /**
     * @param usuario usuario existente
     * @return formulario con los datos del usuario (sin contraseña)
     */
    public static UsuarioFormulario desde(Usuario usuario) {
        UsuarioFormulario formulario = new UsuarioFormulario();
        formulario.setIdUsuario(usuario.getIdUsuario());
        formulario.setNombre(usuario.getNombre());
        formulario.setCorreo(usuario.getCorreo());
        formulario.setRol(usuario.getRol());
        return formulario;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
