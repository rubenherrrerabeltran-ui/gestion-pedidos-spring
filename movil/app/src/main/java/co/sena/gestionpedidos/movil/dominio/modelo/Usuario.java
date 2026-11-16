package co.sena.gestionpedidos.movil.dominio.modelo;

/**
 * Usuario que inició sesión en la aplicación móvil.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Usuario {

    private final int idUsuario;
    private final String nombre;
    private final String correo;
    private final Rol rol;

    public Usuario(int idUsuario, String nombre, String correo, Rol rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public Rol getRol() {
        return rol;
    }

    /** @return primer nombre, para saludar en la pantalla de inicio */
    public String getPrimerNombre() {
        return nombre == null ? "" : nombre.trim().split("\\s+")[0];
    }
}
