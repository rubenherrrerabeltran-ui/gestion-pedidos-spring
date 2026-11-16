package co.sena.gestionpedidos.movil.ui.carrito;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.carrito.Carrito;
import co.sena.gestionpedidos.movil.dominio.carrito.ItemCarrito;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.ui.comun.BaseActivity;
import co.sena.gestionpedidos.movil.ui.comun.Ejecutor;
import co.sena.gestionpedidos.movil.ui.pedidos.DetallePedidoActivity;
import co.sena.gestionpedidos.movil.util.Formatos;

/**
 * Módulo de carrito y registro de pedidos: muestra los productos elegidos, calcula subtotal,
 * IVA y total, y envía el pedido con POST /api/pedidos.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class CarritoActivity extends BaseActivity {

    private Usuario usuario;
    private CarritoAdapter adaptador;
    private Button botonConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuario = requerirSesion();
        if (usuario == null) {
            return;
        }
        configurarPantalla(R.layout.activity_carrito, getString(R.string.titulo_carrito), true);
        adaptador = new CarritoAdapter(this::cambiarCantidad);
        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adaptador);
        botonConfirmar = findViewById(R.id.botonConfirmar);
        botonConfirmar.setOnClickListener(v -> confirmar());
        refrescar();
    }

    private void cambiarCantidad(ItemCarrito item, int cambio) {
        if (!dependencias().getCarrito().cambiarCantidad(item.getProducto().getIdProducto(), cambio)) {
            Toast.makeText(this, getString(R.string.sin_stock, item.getProducto().getStock()),
                    Toast.LENGTH_SHORT).show();
        }
        refrescar();
    }

    private void refrescar() {
        Carrito carrito = dependencias().getCarrito();
        adaptador.mostrar(carrito.getItems());
        ((TextView) findViewById(R.id.valorSubtotal)).setText(Formatos.moneda(carrito.getSubtotal()));
        ((TextView) findViewById(R.id.valorIva)).setText(Formatos.moneda(carrito.getIva()));
        ((TextView) findViewById(R.id.valorTotal)).setText(Formatos.moneda(carrito.getTotal()));
        ((TextView) findViewById(R.id.textoUnidades)).setText(Formatos.unidades(carrito.getUnidades()));
        findViewById(R.id.textoVacio).setVisibility(carrito.estaVacio() ? View.VISIBLE : View.GONE);
        botonConfirmar.setEnabled(!carrito.estaVacio());
    }

    private void confirmar() {
        botonConfirmar.setEnabled(false);
        mostrarCargando(true);
        Carrito carrito = dependencias().getCarrito();
        dependencias().getEjecutor().ejecutar(
                () -> dependencias().getApi().crearPedido(usuario.getIdUsuario(), carrito),
                new Ejecutor.Resultado<Pedido>() {
                    @Override
                    public void exito(Pedido pedido) {
                        mostrarCargando(false);
                        carrito.vaciar();
                        Toast.makeText(CarritoActivity.this,
                                getString(R.string.pedido_creado, pedido.getIdPedido()), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(CarritoActivity.this, DetallePedidoActivity.class);
                        intent.putExtra(DetallePedidoActivity.EXTRA_ID_PEDIDO, pedido.getIdPedido());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void error(Exception error) {
                        botonConfirmar.setEnabled(true);
                        mostrarError(error);
                    }
                });
    }
}
