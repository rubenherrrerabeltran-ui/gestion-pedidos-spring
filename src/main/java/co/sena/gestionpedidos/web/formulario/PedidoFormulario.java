package co.sena.gestionpedidos.web.formulario;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Objeto de formulario para registrar un pedido.
 * Los campos de cantidad se llaman {@code cantidades[ID_PRODUCTO]} y Spring los enlaza en el mapa.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class PedidoFormulario {

    private Integer idCliente;
    private Map<Integer, Integer> cantidades = new LinkedHashMap<>();

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Map<Integer, Integer> getCantidades() {
        return cantidades;
    }

    public void setCantidades(Map<Integer, Integer> cantidades) {
        this.cantidades = cantidades;
    }
}
