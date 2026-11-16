package co.sena.gestionpedidos.movil.ui.comun;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.TextView;

import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;

/**
 * Utilidades visuales reutilizables entre pantallas.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Estilos {

    private Estilos() {
    }

    /**
     * Pinta una etiqueta (fondo redondeado) con el color del estado del pedido.
     *
     * @param etiqueta TextView con fondo {@code fondo_estado}
     * @param estado   estado del pedido
     */
    public static void pintarEstado(TextView etiqueta, EstadoPedido estado) {
        etiqueta.setText(estado.getEtiqueta());
        etiqueta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(estado.getColorHex())));
    }
}
