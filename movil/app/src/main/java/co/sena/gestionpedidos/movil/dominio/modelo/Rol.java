package co.sena.gestionpedidos.movil.dominio.modelo;

/**
 * Roles de usuario del sistema; definen qué opciones ve cada persona en la app.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public enum Rol {
    ADMINISTRADOR("Administrador"),
    VENDEDOR("Vendedor"),
    CLIENTE("Cliente");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** @return {@code true} para ADMINISTRADOR y VENDEDOR, que gestionan pedidos y reportes */
    public boolean gestionaPedidos() {
        return this != CLIENTE;
    }

    /** @return {@code true} si el rol puede armar un carrito y hacer pedidos propios */
    public boolean puedeComprar() {
        return this == CLIENTE;
    }
}
