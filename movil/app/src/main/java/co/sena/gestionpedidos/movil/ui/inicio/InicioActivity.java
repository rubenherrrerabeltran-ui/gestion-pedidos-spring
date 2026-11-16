package co.sena.gestionpedidos.movil.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Resumen;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.ui.carrito.CarritoActivity;
import co.sena.gestionpedidos.movil.ui.comun.BaseActivity;
import co.sena.gestionpedidos.movil.ui.comun.Ejecutor;
import co.sena.gestionpedidos.movil.ui.login.LoginActivity;
import co.sena.gestionpedidos.movil.ui.pedidos.PedidosActivity;
import co.sena.gestionpedidos.movil.ui.productos.ProductosActivity;
import co.sena.gestionpedidos.movil.ui.reportes.ReporteActivity;

/**
 * Pantalla de inicio: saluda al usuario, muestra los indicadores (si gestiona pedidos)
 * y el menú de módulos según su rol.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class InicioActivity extends BaseActivity {

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuario = requerirSesion();
        if (usuario == null) {
            return;
        }
        configurarPantalla(R.layout.activity_inicio, getString(R.string.app_name), false);
        ((TextView) findViewById(R.id.textoSaludo)).setText(getString(R.string.saludo, usuario.getPrimerNombre()));
        ((TextView) findViewById(R.id.textoRol)).setText(getString(R.string.rol_correo,
                usuario.getRol().getEtiqueta(), usuario.getCorreo()));

        boolean gestor = usuario.getRol().gestionaPedidos();
        findViewById(R.id.panelResumen).setVisibility(gestor ? View.VISIBLE : View.GONE);
        findViewById(R.id.botonReporte).setVisibility(gestor ? View.VISIBLE : View.GONE);
        findViewById(R.id.botonCarrito).setVisibility(usuario.getRol().puedeComprar() ? View.VISIBLE : View.GONE);
        int textoPedidos = gestor ? R.string.menu_pedidos : R.string.menu_mis_pedidos;
        ((TextView) findViewById(R.id.botonPedidos)).setText(textoPedidos);

        findViewById(R.id.botonProductos).setOnClickListener(v -> abrir(ProductosActivity.class));
        findViewById(R.id.botonCarrito).setOnClickListener(v -> abrir(CarritoActivity.class));
        findViewById(R.id.botonPedidos).setOnClickListener(v -> abrir(PedidosActivity.class));
        findViewById(R.id.botonReporte).setOnClickListener(v -> abrir(ReporteActivity.class));
        findViewById(R.id.botonSalir).setOnClickListener(v -> cerrarSesion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usuario == null) {
            return;
        }
        int unidades = dependencias().getCarrito().getUnidades();
        ((TextView) findViewById(R.id.botonCarrito)).setText(getString(R.string.menu_carrito, unidades));
        if (usuario.getRol().gestionaPedidos()) {
            cargarResumen();
        }
    }

    private void cargarResumen() {
        mostrarCargando(true);
        dependencias().getEjecutor().ejecutar(() -> dependencias().getApi().obtenerResumen(),
                new Ejecutor.Resultado<Resumen>() {
                    @Override
                    public void exito(Resumen r) {
                        mostrarCargando(false);
                        ((TextView) findViewById(R.id.valorProductos)).setText(String.valueOf(r.getTotalProductos()));
                        ((TextView) findViewById(R.id.valorStockBajo))
                                .setText(String.valueOf(r.getProductosStockBajo()));
                        ((TextView) findViewById(R.id.valorPendientes))
                                .setText(String.valueOf(r.pedidosEn(EstadoPedido.PENDIENTE)));
                    }

                    @Override
                    public void error(Exception error) {
                        mostrarError(error);
                    }
                });
    }

    private void cerrarSesion() {
        dependencias().getEjecutor().ejecutar(() -> {
            dependencias().getApi().cerrarSesion();
            return Boolean.TRUE;
        }, new Ejecutor.Resultado<Boolean>() {
            @Override
            public void exito(Boolean valor) {
                salir();
            }

            @Override
            public void error(Exception error) {
                salir();
            }
        });
    }

    private void salir() {
        dependencias().cerrarSesionLocal();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void abrir(Class<?> pantalla) {
        startActivity(new Intent(this, pantalla));
    }
}
