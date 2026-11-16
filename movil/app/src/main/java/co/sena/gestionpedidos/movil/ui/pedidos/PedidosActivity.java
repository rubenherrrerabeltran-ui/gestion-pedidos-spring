package co.sena.gestionpedidos.movil.ui.pedidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.ui.comun.BaseActivity;
import co.sena.gestionpedidos.movil.ui.comun.Ejecutor;

/**
 * Módulo de pedidos: lista los pedidos (todos para el vendedor, los propios para el cliente)
 * con filtro por estado (GET /api/pedidos?estado=).
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class PedidosActivity extends BaseActivity {

    private PedidosAdapter adaptador;
    private Spinner filtro;
    private TextView textoVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Usuario usuario = requerirSesion();
        if (usuario == null) {
            return;
        }
        configurarPantalla(R.layout.activity_pedidos, getString(usuario.getRol().gestionaPedidos()
                ? R.string.menu_pedidos : R.string.menu_mis_pedidos), true);
        adaptador = new PedidosAdapter(pedido -> {
            Intent intent = new Intent(this, DetallePedidoActivity.class);
            intent.putExtra(DetallePedidoActivity.EXTRA_ID_PEDIDO, pedido.getIdPedido());
            startActivity(intent);
        });
        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adaptador);
        textoVacio = findViewById(R.id.textoVacio);

        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.todos_los_estados));
        for (EstadoPedido e : EstadoPedido.values()) {
            opciones.add(e.getEtiqueta());
        }
        filtro = findViewById(R.id.filtroEstado);
        ArrayAdapter<String> adaptadorFiltro = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opciones);
        adaptadorFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filtro.setAdapter(adaptadorFiltro);
        filtro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                cargar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> padre) {
                // Sin cambios.
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (filtro != null) {
            cargar();
        }
    }

    private void cargar() {
        int posicion = filtro.getSelectedItemPosition();
        EstadoPedido estado = posicion <= 0 ? null : EstadoPedido.values()[posicion - 1];
        mostrarCargando(true);
        dependencias().getEjecutor().ejecutar(() -> dependencias().getApi().listarPedidos(estado),
                new Ejecutor.Resultado<List<Pedido>>() {
                    @Override
                    public void exito(List<Pedido> pedidos) {
                        mostrarCargando(false);
                        adaptador.mostrar(pedidos);
                        textoVacio.setVisibility(pedidos.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void error(Exception error) {
                        mostrarError(error);
                    }
                });
    }
}
