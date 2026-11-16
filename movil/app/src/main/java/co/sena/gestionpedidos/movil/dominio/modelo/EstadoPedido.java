package co.sena.gestionpedidos.movil.dominio.modelo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de un pedido y transiciones permitidas (las mismas reglas del servidor).
 * PENDIENTE → PAGADO | CANCELADO; PAGADO → ENVIADO | CANCELADO; ENVIADO y CANCELADO son finales.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public enum EstadoPedido {
    PENDIENTE("Pendiente", "#B26A00"),
    PAGADO("Pagado", "#0B6FB5"),
    ENVIADO("Enviado", "#2E8700"),
    CANCELADO("Cancelado", "#A32020");

    private final String etiqueta;
    private final String colorHex;

    EstadoPedido(String etiqueta, String colorHex) {
        this.etiqueta = etiqueta;
        this.colorHex = colorHex;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** @return color de la etiqueta del estado en formato #RRGGBB */
    public String getColorHex() {
        return colorHex;
    }

    /** @return estados a los que puede pasar el pedido desde este estado */
    public Set<EstadoPedido> siguientes() {
        switch (this) {
            case PENDIENTE:
                return Collections.unmodifiableSet(EnumSet.of(PAGADO, CANCELADO));
            case PAGADO:
                return Collections.unmodifiableSet(EnumSet.of(ENVIADO, CANCELADO));
            default:
                return Collections.unmodifiableSet(EnumSet.noneOf(EstadoPedido.class));
        }
    }

    /**
     * @param destino estado al que se quiere pasar
     * @return {@code true} si la transición está permitida
     */
    public boolean puedeCambiarA(EstadoPedido destino) {
        return siguientes().contains(destino);
    }
}
