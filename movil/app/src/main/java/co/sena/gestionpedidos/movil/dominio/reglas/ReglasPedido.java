package co.sena.gestionpedidos.movil.dominio.reglas;

import java.util.ArrayList;
import java.util.List;

import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Rol;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;

/**
 * Reglas de negocio de la app para decidir qué acciones ve cada rol sobre un pedido.
 * El servidor vuelve a validarlas; aquí se usan para no mostrar botones inútiles.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class ReglasPedido {

    private ReglasPedido() {
    }

    /**
     * @param usuario usuario en sesión
     * @param pedido  pedido consultado
     * @return estados que el usuario puede asignar al pedido, en orden
     */
    public static List<EstadoPedido> accionesPermitidas(Usuario usuario, Pedido pedido) {
        List<EstadoPedido> acciones = new ArrayList<>();
        EstadoPedido actual = pedido.getEstado();
        if (usuario.getRol() == Rol.CLIENTE) {
            if (actual == EstadoPedido.PENDIENTE && pedido.getIdCliente() == usuario.getIdUsuario()) {
                acciones.add(EstadoPedido.CANCELADO);
            }
            return acciones;
        }
        for (EstadoPedido destino : EstadoPedido.values()) {
            if (actual.puedeCambiarA(destino)) {
                acciones.add(destino);
            }
        }
        return acciones;
    }
}
