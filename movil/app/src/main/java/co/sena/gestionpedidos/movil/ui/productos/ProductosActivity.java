package co.sena.gestionpedidos.movil.ui.productos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.modelo.Producto;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.ui.carrito.CarritoActivity;
import co.sena.gestionpedidos.movil.ui.comun.BaseActivity;
import co.sena.gestionpedidos.movil.ui.comun.Ejecutor;

/**
 * Módulo de catálogo: consulta y búsqueda de productos (GET /api/productos). El cliente puede
 * agregarlos al carrito; el sistema no permite superar el stock.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ProductosActivity extends BaseActivity {

    private ProductosAdapter adaptador;
    private EditText campoBuscar;
    private TextView textoVacio;
    private Button botonCarrito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Usuario usuario = requerirSesion();
        if (usuario == null) {
            return;
        }
        configurarPantalla(R.layout.activity_productos, getString(R.string.menu_productos), true);
        boolean compra = usuario.getRol().puedeComprar();
        adaptador = new ProductosAdapter(compra, this::agregarAlCarrito);
        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adaptador);
        campoBuscar = findViewById(R.id.campoBuscar);
        textoVacio = findViewById(R.id.textoVacio);
        botonCarrito = findViewById(R.id.botonVerCarrito);
        botonCarrito.setVisibility(compra ? View.VISIBLE : View.GONE);
        botonCarrito.setOnClickListener(v -> startActivity(new Intent(this, CarritoActivity.class)));
        findViewById(R.id.botonBuscar).setOnClickListener(v -> cargar());
        campoBuscar.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                cargar();
                return true;
            }
            return false;
        });
        cargar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarBotonCarrito();
    }

    private void cargar() {
        String texto = campoBuscar.getText().toString();
        mostrarCargando(true);
        dependencias().getEjecutor().ejecutar(() -> dependencias().getApi().listarProductos(texto),
                new Ejecutor.Resultado<List<Producto>>() {
                    @Override
                    public void exito(List<Producto> productos) {
                        mostrarCargando(false);
                        adaptador.mostrar(productos);
                        textoVacio.setVisibility(productos.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void error(Exception error) {
                        mostrarError(error);
                    }
                });
    }

    private void agregarAlCarrito(Producto producto) {
        boolean agregado = dependencias().getCarrito().agregar(producto);
        Toast.makeText(this, agregado ? getString(R.string.agregado, producto.getNombre())
                : getString(R.string.sin_stock, producto.getStock()), Toast.LENGTH_SHORT).show();
        actualizarBotonCarrito();
    }

    private void actualizarBotonCarrito() {
        if (botonCarrito != null) {
            botonCarrito.setText(getString(R.string.menu_carrito, dependencias().getCarrito().getUnidades()));
        }
    }
}
