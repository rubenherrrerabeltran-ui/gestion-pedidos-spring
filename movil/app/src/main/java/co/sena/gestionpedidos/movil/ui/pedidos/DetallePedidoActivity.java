package co.sena.gestionpedidos.movil.ui.pedidos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.modelo.DetallePedido;
import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.dominio.reglas.ReglasPedido;
import co.sena.gestionpedidos.movil.ui.comun.BaseActivity;
import co.sena.gestionpedidos.movil.ui.comun.Ejecutor;
import co.sena.gestionpedidos.movil.ui.comun.Estilos;
import co.sena.gestionpedidos.movil.util.Formatos;

/**
 * Detalle de un pedido (GET /api/pedidos/{id}) con las acciones de cambio de estado que
 * permite el rol del usuario (PATCH /api/pedidos/{id}/estado).
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class DetallePedidoActivity extends BaseActivity {

    /** Extra del Intent con el número del pedido. */
    public static final String EXTRA_ID_PEDIDO = "id_pedido";

    private Usuario usuario;
    private int idPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuario = requerirSesion();
        if (usuario == null) {
            return;
        }
        idPedido = getIntent().getIntExtra(EXTRA_ID_PEDIDO, 0);
        configurarPantalla(R.layout.activity_detalle_pedido, getString(R.string.numero_pedido, idPedido), true);
        cargar();
    }

    private void cargar() {
        mostrarCargando(true);
        dependencias().getEjecutor().ejecutar(() -> dependencias().getApi().obtenerPedido(idPedido),
                new Ejecutor.Resultado<Pedido>() {
                    @Override
                    public void exito(Pedido pedido) {
                        mostrarCargando(false);
                        mostrar(pedido);
                    }

                    @Override
                    public void error(Exception error) {
                        mostrarError(error);
                    }
                });
    }

    private void mostrar(Pedido pedido) {
        ((TextView) findViewById(R.id.textoCliente)).setText(pedido.getNombreCliente());
        ((TextView) findViewById(R.id.textoFecha)).setText(Formatos.fechaHora(pedido.getFechaPedido()));
        Estilos.pintarEstado(findViewById(R.id.textoEstado), pedido.getEstado());
        ((TextView) findViewById(R.id.valorSubtotal)).setText(Formatos.moneda(pedido.getSubtotal()));
        ((TextView) findViewById(R.id.valorIva)).setText(Formatos.moneda(pedido.getIva()));
        ((TextView) findViewById(R.id.valorTotal)).setText(Formatos.moneda(pedido.getTotal()));

        LinearLayout lineas = findViewById(R.id.contenedorDetalles);
        lineas.removeAllViews();
        LayoutInflater inflador = LayoutInflater.from(this);
        for (DetallePedido d : pedido.getDetalles()) {
            View fila = inflador.inflate(R.layout.item_detalle, lineas, false);
            ((TextView) fila.findViewById(R.id.textoProducto)).setText(d.getNombreProducto());
            ((TextView) fila.findViewById(R.id.textoCantidadPrecio)).setText(getString(R.string.cantidad_por_precio,
                    d.getCantidad(), Formatos.moneda(d.getPrecioUnitario())));
            ((TextView) fila.findViewById(R.id.textoSubtotal)).setText(Formatos.moneda(d.getSubtotal()));
            lineas.addView(fila);
        }

        LinearLayout acciones = findViewById(R.id.contenedorAcciones);
        acciones.removeAllViews();
        List<EstadoPedido> permitidas = ReglasPedido.accionesPermitidas(usuario, pedido);
        findViewById(R.id.tituloAcciones).setVisibility(permitidas.isEmpty() ? View.GONE : View.VISIBLE);
        for (EstadoPedido destino : permitidas) {
            MaterialButton boton = (MaterialButton) inflador.inflate(
                    destino == EstadoPedido.CANCELADO ? R.layout.boton_accion_peligro : R.layout.boton_accion,
                    acciones, false);
            boton.setText(getString(R.string.marcar_como, destino.getEtiqueta().toLowerCase()));
            boton.setOnClickListener(v -> confirmarCambio(destino));
            acciones.addView(boton);
        }
    }

    private void confirmarCambio(EstadoPedido destino) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_confirmar)
                .setMessage(getString(R.string.confirmar_estado, idPedido, destino.getEtiqueta().toLowerCase()))
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.si, (d, w) -> cambiarEstado(destino))
                .show();
    }

    private void cambiarEstado(EstadoPedido destino) {
        mostrarCargando(true);
        dependencias().getEjecutor().ejecutar(() -> dependencias().getApi().cambiarEstado(idPedido, destino),
                new Ejecutor.Resultado<Pedido>() {
                    @Override
                    public void exito(Pedido pedido) {
                        mostrarCargando(false);
                        Toast.makeText(DetallePedidoActivity.this, getString(R.string.estado_actualizado,
                                pedido.getEstado().getEtiqueta()), Toast.LENGTH_SHORT).show();
                        mostrar(pedido);
                    }

                    @Override
                    public void error(Exception error) {
                        mostrarError(error);
                    }
                });
    }
}
