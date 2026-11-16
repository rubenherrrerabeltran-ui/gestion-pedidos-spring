package co.sena.gestionpedidos.movil.ui.reportes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.ReporteVentas;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.ui.comun.BaseActivity;
import co.sena.gestionpedidos.movil.ui.comun.Ejecutor;
import co.sena.gestionpedidos.movil.util.Formatos;
import co.sena.gestionpedidos.movil.util.Validador;

/**
 * Módulo de reportes: consume el reporte de ventas integrado en la versión 1.3.0 del backend
 * (GET /api/reportes/ventas) con filtro de fechas.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ReporteActivity extends BaseActivity {

    private EditText campoDesde;
    private EditText campoHasta;
    private TextInputLayout capaDesde;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Usuario usuario = requerirSesion();
        if (usuario == null) {
            return;
        }
        configurarPantalla(R.layout.activity_reporte, getString(R.string.menu_reporte), true);
        campoDesde = findViewById(R.id.campoDesde);
        campoHasta = findViewById(R.id.campoHasta);
        capaDesde = findViewById(R.id.capaDesde);
        findViewById(R.id.botonConsultar).setOnClickListener(v -> consultar());
        consultar();
    }

    private void consultar() {
        String desde = campoDesde.getText().toString();
        String hasta = campoHasta.getText().toString();
        String error = Validador.validarRangoFechas(desde, hasta);
        capaDesde.setError(error);
        if (error != null) {
            return;
        }
        mostrarCargando(true);
        dependencias().getEjecutor().ejecutar(() -> dependencias().getApi().obtenerReporte(desde, hasta),
                new Ejecutor.Resultado<ReporteVentas>() {
                    @Override
                    public void exito(ReporteVentas r) {
                        mostrarCargando(false);
                        mostrar(r);
                    }

                    @Override
                    public void error(Exception e) {
                        mostrarError(e);
                    }
                });
    }

    private void mostrar(ReporteVentas r) {
        if (campoDesde.getText().length() == 0) {
            campoDesde.setText(r.getDesde());
        }
        if (campoHasta.getText().length() == 0) {
            campoHasta.setText(r.getHasta());
        }
        ((TextView) findViewById(R.id.textoPeriodo)).setText(getString(R.string.periodo,
                Formatos.fecha(r.getDesde()), Formatos.fecha(r.getHasta())));
        ((TextView) findViewById(R.id.valorTotalVendido)).setText(Formatos.moneda(r.getTotal()));
        ((TextView) findViewById(R.id.valorDetalleVentas)).setText(getString(R.string.detalle_ventas,
                Formatos.moneda(r.getSubtotal()), Formatos.moneda(r.getIva())));
        ((TextView) findViewById(R.id.valorPedidosPeriodo)).setText(String.valueOf(r.getPedidosEnPeriodo()));
        ((TextView) findViewById(R.id.valorPedidosVendidos)).setText(String.valueOf(r.getPedidosVendidos()));
        ((TextView) findViewById(R.id.valorTicket)).setText(Formatos.moneda(r.getTicketPromedio()));
        ((TextView) findViewById(R.id.textoEstados)).setText(getString(R.string.estados_reporte,
                r.pedidosEn(EstadoPedido.PENDIENTE), r.pedidosEn(EstadoPedido.PAGADO),
                r.pedidosEn(EstadoPedido.ENVIADO), r.pedidosEn(EstadoPedido.CANCELADO)));

        LinearLayout contenedor = findViewById(R.id.contenedorProductos);
        contenedor.removeAllViews();
        LayoutInflater inflador = LayoutInflater.from(this);
        int posicion = 1;
        for (ReporteVentas.VentaProducto p : r.getProductosMasVendidos()) {
            View fila = inflador.inflate(R.layout.item_detalle, contenedor, false);
            ((TextView) fila.findViewById(R.id.textoProducto)).setText(posicion++ + ". " + p.getNombreProducto());
            ((TextView) fila.findViewById(R.id.textoCantidadPrecio)).setText(Formatos.unidades(p.getUnidades()));
            ((TextView) fila.findViewById(R.id.textoSubtotal)).setText(Formatos.moneda(p.getTotal()));
            contenedor.addView(fila);
        }
        findViewById(R.id.textoSinVentas).setVisibility(r.getProductosMasVendidos().isEmpty()
                ? View.VISIBLE : View.GONE);
    }
}
