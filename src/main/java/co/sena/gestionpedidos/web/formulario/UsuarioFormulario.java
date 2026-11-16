package co.sena.gestionpedidos.web.formulario;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.servicio.DatosUsuario;

/**
 * Objeto de formulario (form backing object) para crear o editar usuarios y para el registro de clientes.
 * Se separa de la entidad {@link Usuario} porque recibe la contraseña en texto plano,
 * que nunca se guarda: el servicio la convierte en hash.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class UsuarioFormulario {

    private Integer idUsuario;
    private String nombre;
    private String documento;
    private String correo;
    private String telefono;
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
        formulario.setDocumento(usuario.getDocumento());
        formulario.setCorreo(usuario.getCorreo());
        formulario.setTelefono(usuario.getTelefono());
        formulario.setRol(usuario.getRol());
        return formulario;
    }

    /**
     * @return los datos del formulario listos para el servicio
     */
    public DatosUsuario aDatos() {
        return new DatosUsuario(nombre, documento, correo, telefono, clave, rol);
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

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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
